package co.edu.unbosque.biohub.web;

import co.edu.unbosque.biohub.auditoria.ComparacionTaxonomica;
import co.edu.unbosque.biohub.auditoria.HallazgoMemento;
import co.edu.unbosque.biohub.auditoria.ServicioReclasificacionTaxonomica;
import co.edu.unbosque.biohub.autenticacion.PermisoOperacion;
import co.edu.unbosque.biohub.autenticacion.ServicioAutenticacionRol;
import co.edu.unbosque.biohub.autenticacion.SesionUsuario;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaAnfibio;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaAveMigratoria;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaPlanta;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaReptil;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaTaxonomica;
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
import java.util.concurrent.atomic.AtomicLong;

@RestController
@RequestMapping("/api/biohub")
public class HallazgosBioHubController {

    private final List<Hallazgo> hallazgos;
    private final ServicioReclasificacionTaxonomica servicioAuditoria;
    private final ServicioAutenticacionRol servicioAuth;
    private final AutorizacionUbicacion autorizacionUbicacion;
    private final AtomicLong secuenciaId = new AtomicLong(102L);

    public HallazgosBioHubController(List<Hallazgo> hallazgos,
                                     ServicioReclasificacionTaxonomica servicioAuditoria,
                                     ServicioAutenticacionRol servicioAuth,
                                     AutorizacionUbicacion autorizacionUbicacion) {
        this.hallazgos = hallazgos;
        this.servicioAuditoria = servicioAuditoria;
        this.servicioAuth = servicioAuth;
        this.autorizacionUbicacion = autorizacionUbicacion;
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
    public List<HallazgoVistaDTO> listarHallazgos(
            @RequestHeader(value = "X-Auth-Token", required = false) String token) {
        SesionUsuario sesion = servicioAuth.resolverSesion(token);
        boolean revelaExactas = autorizacionUbicacion.revelaCoordenadas(sesion.getRol());

        return hallazgos.stream().map(h -> new HallazgoVistaDTO(
                h.getId(),
                h.getEspecie().getNombreCientifico(),
                h.getEspecie().getNombreComun(),
                h.getEspecie().getEstadoConservacion(),
                h.getEspecie().esSensible(),
                autorizacionUbicacion.decidir(sesion.getRol(), h),
                revelaExactas,
                h.getDepartamento(),
                h.getFechaObservacion(),
                h.getAtributos()
        )).toList();
    }

    @GetMapping("/fichas/{grupo}/requeridos")
    public ResponseEntity<?> obtenerAtributosRequeridos(@PathVariable String grupo) {
        try {
            FichaTaxonomica ficha = obtenerFicha(grupo);
            return ResponseEntity.ok(Map.of(
                    "grupo", ficha.getGrupo(),
                    "atributosRequeridos", ficha.getAtributosRequeridos()
            ));
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

            FichaTaxonomica ficha = obtenerFicha(request.grupoTaxonomico());
            List<AtributoDinamico> atributos = new ArrayList<>();
            if (request.atributos() != null) {
                for (AtributoInput item : request.atributos()) {
                    if (item.valor() != null && !item.valor().isBlank()) {
                        atributos.add(new AtributoDinamico(
                                item.nombre(),
                                TipoDato.valueOf(item.tipo()),
                                item.valor()
                        ));
                    }
                }
            }

            List<String> erroresValidacion = ficha.validar(atributos);
            if (!erroresValidacion.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of(
                        "error", "Faltan atributos obligatorios para " + ficha.getGrupo(),
                        "faltantes", erroresValidacion
                ));
            }

            long nuevoId = secuenciaId.getAndIncrement();
            Especie especie = new Especie(
                    nuevoId,
                    request.nombreCientifico(),
                    request.nombreComun(),
                    request.estadoConservacion(),
                    request.esSensible()
            );
            LocalDateTime ahora = LocalDateTime.now();
            Hallazgo nuevo = new Hallazgo(
                    nuevoId,
                    especie,
                    request.latitud(),
                    request.longitud(),
                    request.departamento(),
                    ahora,
                    sesion.getUsuarioId(),
                    atributos
            );

            hallazgos.add(nuevo);
            servicioAuditoria.registrarHallazgoInicial(nuevo, ahora);
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
            Hallazgo actualizado = servicioAuditoria.reclasificar(
                    id,
                    nuevaEspecie,
                    sesion.getUsername(),
                    sesion.getRol(),
                    request.motivo(),
                    LocalDateTime.parse(request.fechaReclasificacion())
            );

            for (int i = 0; i < hallazgos.size(); i++) {
                if (hallazgos.get(i).getId().equals(id)) {
                    hallazgos.set(i, actualizado);
                    break;
                }
            }
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

    private FichaTaxonomica obtenerFicha(String grupo) {
        return switch (grupo.toUpperCase()) {
            case "ANFIBIO" -> new FichaAnfibio();
            case "AVE_MIGRATORIA" -> new FichaAveMigratoria();
            case "PLANTA" -> new FichaPlanta();
            case "REPTIL" -> new FichaReptil();
            default -> throw new IllegalArgumentException("Grupo taxonómico no soportado: " + grupo);
        };
    }
}