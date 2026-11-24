package co.edu.unbosque.biohub.web;

import co.edu.unbosque.biohub.auditoria.ComparacionTaxonomica;
import co.edu.unbosque.biohub.auditoria.HallazgoMemento;
import co.edu.unbosque.biohub.auditoria.ServicioReclasificacionTaxonomica;
import co.edu.unbosque.biohub.autenticacion.PermisoOperacion;
import co.edu.unbosque.biohub.autenticacion.ServicioAutenticacionRol;
import co.edu.unbosque.biohub.autenticacion.SesionUsuario;
import co.edu.unbosque.biohub.consulta.EstadoCache;
import co.edu.unbosque.biohub.consulta.FichaPublicaEnmascarada;
import co.edu.unbosque.biohub.consulta.ResultadoConsulta;
import co.edu.unbosque.biohub.consulta.ServicioConsultaPublica;
import co.edu.unbosque.biohub.hallazgos.ServicioHallazgos;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaFicha;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaTaxonomica;
import co.edu.unbosque.biohub.persistencia.BioHubRepositorioJdbc;
import co.edu.unbosque.biohub.seguridad.AutorizacionUbicacion;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/biohub")
public class HallazgosBioHubController {

    public static final String HEADER_CACHE_STATUS = "X-Cache-Status";

    private final List<Hallazgo> hallazgos;
    private final ServicioReclasificacionTaxonomica servicioAuditoria;
    private final ServicioAutenticacionRol servicioAuth;
    private final AutorizacionUbicacion autorizacionUbicacion;
    private final ServicioHallazgos servicioHallazgos;
    private final ServicioConsultaPublica servicioConsultaPublica;
    private final Optional<BioHubRepositorioJdbc> repoJdbc;
    private final AtomicLong secuenciaId = new AtomicLong(102L);

    public HallazgosBioHubController(List<Hallazgo> hallazgos,
                                     ServicioReclasificacionTaxonomica servicioAuditoria,
                                     ServicioAutenticacionRol servicioAuth,
                                     AutorizacionUbicacion autorizacionUbicacion,
                                     ServicioHallazgos servicioHallazgos,
                                     ServicioConsultaPublica servicioConsultaPublica,
                                     Optional<BioHubRepositorioJdbc> repoJdbc) {
        this.hallazgos = hallazgos;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioAuth = servicioAuth;
        this.autorizacionUbicacion = autorizacionUbicacion;
        this.servicioHallazgos = servicioHallazgos;
        this.servicioConsultaPublica = servicioConsultaPublica;
        this.repoJdbc = repoJdbc;
    }

    public record HallazgoVistaDTO(
            Long id,
            String nombreCientifico,
            String nombreComun,
            String estadoConservacion,
            boolean esSensible,
            String ubicacionVisible,
            boolean coordenadasExactas,
            String departamento,
            LocalDateTime fechaObservacion,
            List<AtributoDinamico> atributos
    ) {}

    public record AtributoInput(String nombre, String tipo, String valor) {}

    public record NuevoAtributoGrupoRequest(String nombre, String tipo) {}

    public record CrearHallazgoRequest(
            String grupoTaxonomico,
            String nombreCientifico,
            String nombreComun,
            String estadoConservacion,
            boolean esSensible,
            double latitud,
            double longitud,
            String departamento,
            List<AtributoInput> atributos
    ) {}

    public record ReclasificarRequest(
            Long nuevaEspecieId,
            String nombreCientifico,
            String nombreComun,
            String estadoConservacion,
            boolean esSensible,
            String motivo,
            String fechaReclasificacion
    ) {}

    @GetMapping("/hallazgos")
    public ResponseEntity<List<HallazgoVistaDTO>> listarHallazgos(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        SesionUsuario sesion = servicioAuth.resolverSesion(token);

        if (servicioConsultaPublica.usaCachePublica(sesion.getRol())) {
            ResultadoConsulta<List<FichaPublicaEnmascarada>> resultado =
                    servicioConsultaPublica.consultarCatalogoPublico(() -> hallazgos);
            List<HallazgoVistaDTO> vistas = resultado.datos().stream()
                    .map(this::aVistaPublica)
                    .toList();
            return ResponseEntity.ok()
                    .header(HEADER_CACHE_STATUS, resultado.estadoCache().name())
                    .body(vistas);
        }

        List<HallazgoVistaDTO> vistas = hallazgos.stream()
                .map(h -> aVistaCompleta(h, sesion.getRol()))
                .toList();
        return ResponseEntity.ok()
                .header(HEADER_CACHE_STATUS, EstadoCache.BYPASS.name())
                .body(vistas);
    }

    @GetMapping("/hallazgos/{id}")
    public ResponseEntity<?> obtenerHallazgo(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        SesionUsuario sesion = servicioAuth.resolverSesion(token);

        if (servicioConsultaPublica.usaCachePublica(sesion.getRol())) {
            ResultadoConsulta<Optional<FichaPublicaEnmascarada>> resultado =
                    servicioConsultaPublica.consultarFichaPublica(id, () -> buscarHallazgoPorId(id));
            String estadoCache = resultado.estadoCache().name();
            if (resultado.datos().isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .header(HEADER_CACHE_STATUS, estadoCache)
                        .body(Map.of("error", "Hallazgo no encontrado: " + id));
            }
            return ResponseEntity.ok()
                    .header(HEADER_CACHE_STATUS, estadoCache)
                    .body(aVistaPublica(resultado.datos().get()));
        }

        Optional<Hallazgo> hallazgo = buscarHallazgoPorId(id);
        if (hallazgo.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .header(HEADER_CACHE_STATUS, EstadoCache.BYPASS.name())
                    .body(Map.of("error", "Hallazgo no encontrado: " + id));
        }
        return ResponseEntity.ok()
                .header(HEADER_CACHE_STATUS, EstadoCache.BYPASS.name())
                .body(aVistaCompleta(hallazgo.get(), sesion.getRol()));
    }

    @GetMapping("/fichas/grupos")
    public List<String> listarGruposTaxonomicos() {
        return servicioHallazgos.listarGruposSoportados();
    }

    @GetMapping("/fichas/{grupo}/requeridos")
    public ResponseEntity<?> obtenerAtributosRequeridos(@PathVariable String grupo) {
        try {
            FabricaFicha fabrica = servicioHallazgos.obtenerFabrica(grupo);
            return ResponseEntity.ok(Map.of(
                    "grupo", fabrica.obtenerGrupo(),
                    "atributosRequeridos", fabrica.obtenerDefinicionAtributos()
            ));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/fichas/{grupo}/atributos")
    public ResponseEntity<?> agregarAtributoGrupo(
            @PathVariable String grupo,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody NuevoAtributoGrupoRequest request) {
        try {
            servicioAuth.autorizar(token, PermisoOperacion.REGISTRAR_HALLAZGO);
            if (request == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "El cuerpo de la petición es obligatorio"));
            }
            TipoDato tipoDato = (request.tipo() == null || request.tipo().isBlank())
                    ? TipoDato.TEXTO
                    : TipoDato.valueOf(request.tipo().trim().toUpperCase());

            Map<String, TipoDato> actualizados = servicioHallazgos.agregarAtributoAGrupo(
                    grupo,
                    request.nombre(),
                    tipoDato
            );
            String grupoNormalizado = servicioHallazgos.obtenerFabrica(grupo).obtenerGrupo();

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                    "grupo", grupoNormalizado,
                    "atributosRequeridos", actualizados
            ));
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/hallazgos")
    public ResponseEntity<?> registrarHallazgo(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody CrearHallazgoRequest request) {
        try {
            servicioAuth.autorizar(token, PermisoOperacion.REGISTRAR_HALLAZGO);
            SesionUsuario sesion = servicioAuth.resolverSesion(token);

            Map<String, TipoDato> definicion = servicioHallazgos.obtenerAtributosRequeridos(request.grupoTaxonomico());
            List<AtributoDinamico> atributosRecibidos = new ArrayList<>();
            if (request.atributos() != null) {
                for (AtributoInput item : request.atributos()) {
                    if (item == null || item.nombre() == null || item.nombre().isBlank()) {
                        continue;
                    }
                    String nombre = item.nombre().trim();
                    TipoDato tipo = (item.tipo() != null && !item.tipo().isBlank())
                            ? TipoDato.valueOf(item.tipo().trim().toUpperCase())
                            : definicion.getOrDefault(nombre, TipoDato.TEXTO);
                    atributosRecibidos.add(new AtributoDinamico(nombre, tipo, item.valor()));
                }
            }

            FichaTaxonomica ficha = servicioHallazgos.validarYCrearFicha(
                    request.grupoTaxonomico(),
                    atributosRecibidos
            );
            List<AtributoDinamico> atributos = atributosRecibidos.stream()
                    .filter(a -> a.valor() != null && !a.valor().isBlank())
                    .toList();

            LocalDateTime ahora = LocalDateTime.now();
            Hallazgo nuevo;

            if (repoJdbc.isPresent()) {
                Especie especieBase = new Especie(
                        null,
                        request.nombreCientifico(),
                        request.nombreComun(),
                        request.estadoConservacion(),
                        request.esSensible()
                );
                nuevo = repoJdbc.get().insertarHallazgo(
                        especieBase,
                        request.latitud(),
                        request.longitud(),
                        request.departamento(),
                        ahora,
                        sesion.getUsuarioId(),
                        ficha.getGrupo(),
                        atributos
                );
            } else {
                long nuevoId = secuenciaId.getAndIncrement();
                Especie especie = new Especie(
                        nuevoId,
                        request.nombreCientifico(),
                        request.nombreComun(),
                        request.estadoConservacion(),
                        request.esSensible()
                );
                nuevo = new Hallazgo(
                        nuevoId,
                        especie,
                        request.latitud(),
                        request.longitud(),
                        request.departamento(),
                        ahora,
                        sesion.getUsuarioId(),
                        atributos
                );
            }

            hallazgos.add(nuevo);
            servicioAuditoria.registrarHallazgoInicial(nuevo, ahora);

            if (repoJdbc.isPresent()) {
                List<HallazgoMemento> historial = servicioAuditoria.auditarHistorialCompleto(
                        nuevo.getId(),
                        RolEnum.AUDITOR_CIENTIFICO
                );
                repoJdbc.get().guardarVersionMemento(historial.getLast());
            }

            servicioConsultaPublica.invalidarCache();

            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @PostMapping("/hallazgos/{id}/reclasificar")
    public ResponseEntity<?> reclasificar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody ReclasificarRequest request) {
        try {
            servicioAuth.autorizar(token, PermisoOperacion.RECLASIFICAR_ESPECIE);
            SesionUsuario sesion = servicioAuth.resolverSesion(token);

            Especie nuevaEspecie = new Especie(
                    request.nuevaEspecieId(),
                    request.nombreCientifico(),
                    request.nombreComun(),
                    request.estadoConservacion(),
                    request.esSensible()
            );
            LocalDateTime fechaReclasificacion = LocalDateTime.parse(request.fechaReclasificacion());
            Hallazgo actualizado = servicioAuditoria.reclasificar(
                    id,
                    nuevaEspecie,
                    sesion.getUsername(),
                    sesion.getRol(),
                    request.motivo(),
                    fechaReclasificacion
            );

            for (int i = 0; i < hallazgos.size(); i++) {
                if (hallazgos.get(i).getId().equals(id)) {
                    hallazgos.set(i, actualizado);
                    break;
                }
            }

            if (repoJdbc.isPresent()) {
                List<HallazgoMemento> historial = servicioAuditoria.auditarHistorialCompleto(
                        id,
                        sesion.getRol()
                );
                repoJdbc.get().guardarVersionMemento(historial.getLast());
            }

            servicioConsultaPublica.invalidarFicha(id);

            return ResponseEntity.ok(actualizado);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/hallazgos/{id}/historial")
    public ResponseEntity<?> historial(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        try {
            servicioAuth.autorizar(token, PermisoOperacion.AUDITAR_HISTORIAL);
            SesionUsuario sesion = servicioAuth.resolverSesion(token);
            List<HallazgoMemento> lineaTiempo = servicioAuditoria.auditarHistorialCompleto(id, sesion.getRol());
            return ResponseEntity.ok(lineaTiempo);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/hallazgos/{id}/comparar")
    public ResponseEntity<?> comparar(
            @PathVariable Long id,
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestParam String inicio,
            @RequestParam String fin) {
        try {
            servicioAuth.autorizar(token, PermisoOperacion.AUDITAR_HISTORIAL);
            SesionUsuario sesion = servicioAuth.resolverSesion(token);
            ComparacionTaxonomica comparacion = servicioAuditoria.compararEneroVsDiciembre(
                    id,
                    LocalDateTime.parse(inicio),
                    LocalDateTime.parse(fin),
                    sesion.getRol()
            );
            return ResponseEntity.ok(comparacion);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    private Optional<Hallazgo> buscarHallazgoPorId(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return hallazgos.stream()
                .filter(h -> id.equals(h.getId()))
                .findFirst();
    }

    private HallazgoVistaDTO aVistaPublica(FichaPublicaEnmascarada ficha) {
        return new HallazgoVistaDTO(
                ficha.id(),
                ficha.nombreCientifico(),
                ficha.nombreComun(),
                ficha.estadoConservacion(),
                ficha.esSensible(),
                ficha.ubicacionVisible(),
                false,
                ficha.departamento(),
                ficha.fechaObservacion(),
                ficha.atributos()
        );
    }

    private HallazgoVistaDTO aVistaCompleta(Hallazgo hallazgo, RolEnum rol) {
        return new HallazgoVistaDTO(
                hallazgo.getId(),
                hallazgo.getEspecie().getNombreCientifico(),
                hallazgo.getEspecie().getNombreComun(),
                hallazgo.getEspecie().getEstadoConservacion(),
                hallazgo.getEspecie().esSensible(),
                autorizacionUbicacion.decidir(rol, hallazgo),
                true,
                hallazgo.getDepartamento(),
                hallazgo.getFechaObservacion(),
                hallazgo.getAtributos()
        );
    }
}