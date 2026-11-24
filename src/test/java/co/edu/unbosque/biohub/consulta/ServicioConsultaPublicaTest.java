package co.edu.unbosque.biohub.consulta;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.seguridad.AutorizacionUbicacion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioConsultaPublicaTest {

    private AutorizacionUbicacion autorizacion;
    private Hallazgo hallazgoSensible;
    private CacheEnMemoriaStub cacheMemoria;
    private ServicioConsultaPublica servicio;

    @BeforeEach
    void setUp() {
        autorizacion = AutorizacionUbicacion.conAsignacionPorDefecto();
        Especie especie = new Especie(
                1L,
                "Dendrobates truncatus",
                "Rana venenosa",
                "EN",
                true
        );
        hallazgoSensible = new Hallazgo(
                100L,
                especie,
                5.6922,
                -76.6581,
                "Chocó",
                LocalDateTime.of(2026, 3, 15, 9, 30),
                "INV-001",
                List.of(
                        new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                        new AtributoDinamico("rutaGps", TipoDato.TEXTO, "5.6922,-76.6581"),
                        new AtributoDinamico("coordenadasExtra", TipoDato.TEXTO, "N 5°41'31\"")
                )
        );
        cacheMemoria = new CacheEnMemoriaStub();
        servicio = new ServicioConsultaPublica(autorizacion, Optional.of(cacheMemoria));
    }

    @Test
    void especieSensibleConsultadaPorVisitanteSoloMuestraDepartamentoYOmiteAtributosGps() {
        FichaPublicaEnmascarada ficha = FichaPublicaEnmascarada.desdeHallazgo(hallazgoSensible, autorizacion);

        assertEquals("Chocó", ficha.ubicacionVisible());
        assertFalse(ficha.ubicacionVisible().contains("5.6922"));
        assertFalse(ficha.ubicacionVisible().contains("-76.6581"));
        assertEquals(1, ficha.atributos().size());
        assertEquals("temperaturaAgua", ficha.atributos().getFirst().nombre());
    }

    @Test
    void fichaAlmacenadaEnCacheNoContieneCoordenadasExactasNiRutaGpsAlSerializar() throws Exception {
        servicio.consultarFichaPublica(100L, () -> Optional.of(hallazgoSensible));

        FichaPublicaEnmascarada enCache = cacheMemoria.obtenerFicha(100L).orElseThrow();
        JsonMapper mapper = JsonMapper.builder().build();
        String json = mapper.writeValueAsString(enCache);

        assertTrue(json.contains("Chocó"));
        assertTrue(json.contains("temperaturaAgua"));
        assertFalse(json.contains("5.6922"));
        assertFalse(json.contains("-76.6581"));
        assertFalse(json.contains("latitud"));
        assertFalse(json.contains("longitud"));
        assertFalse(json.contains("rutaGps"));
        assertFalse(json.contains("coordenadasExtra"));

        FichaPublicaEnmascarada reconstruida = mapper.readValue(json, FichaPublicaEnmascarada.class);
        assertEquals(enCache, reconstruida);
    }

    @Test
    void consultaCatalogoUsaCacheAsideYRespondeHitEnMenosDeUnSegundo() {
        AtomicInteger consultasFuente = new AtomicInteger(0);

        ResultadoConsulta<List<FichaPublicaEnmascarada>> primera = servicio.consultarCatalogoPublico(() -> {
            consultasFuente.incrementAndGet();
            return List.of(hallazgoSensible);
        });
        assertEquals(EstadoCache.MISS, primera.estadoCache());
        assertEquals(1, consultasFuente.get());

        long inicioNs = System.nanoTime();
        ResultadoConsulta<List<FichaPublicaEnmascarada>> segunda = servicio.consultarCatalogoPublico(() -> {
            consultasFuente.incrementAndGet();
            return List.of(hallazgoSensible);
        });
        long duracionNs = System.nanoTime() - inicioNs;

        assertEquals(EstadoCache.HIT, segunda.estadoCache());
        assertEquals(1, consultasFuente.get());
        assertTrue(duracionNs <= 1_000_000_000L);
        assertEquals(primera.datos(), segunda.datos());
    }

    @Test
    void consultaFichaIndividualUsaCacheAsideYRespondeHitSinReconsultarFuente() {
        AtomicInteger consultasFuente = new AtomicInteger(0);

        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> primera = servicio.consultarFichaPublica(100L, () -> {
            consultasFuente.incrementAndGet();
            return Optional.of(hallazgoSensible);
        });
        assertEquals(EstadoCache.MISS, primera.estadoCache());
        assertEquals(1, consultasFuente.get());

        long inicioNs = System.nanoTime();
        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> segunda = servicio.consultarFichaPublica(100L, () -> {
            consultasFuente.incrementAndGet();
            return Optional.of(hallazgoSensible);
        });
        long duracionNs = System.nanoTime() - inicioNs;

        assertEquals(EstadoCache.HIT, segunda.estadoCache());
        assertEquals(1, consultasFuente.get());
        assertTrue(duracionNs <= 1_000_000_000L);
        assertEquals(primera.datos(), segunda.datos());
    }

    @Test
    void degradacionEleganteCuandoCacheFallaEnLecturaCatalogoYFicha() {
        ServicioConsultaPublica servicioConFallo = new ServicioConsultaPublica(
                autorizacion,
                Optional.of(CacheConFalloStub.fallandoTodo())
        );

        ResultadoConsulta<List<FichaPublicaEnmascarada>> catalogo =
                servicioConFallo.consultarCatalogoPublico(() -> List.of(hallazgoSensible));
        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> ficha =
                servicioConFallo.consultarFichaPublica(100L, () -> Optional.of(hallazgoSensible));

        assertEquals(EstadoCache.FALLBACK, catalogo.estadoCache());
        assertEquals("Chocó", catalogo.datos().getFirst().ubicacionVisible());
        assertEquals(1, catalogo.datos().getFirst().atributos().size());

        assertEquals(EstadoCache.FALLBACK, ficha.estadoCache());
        assertTrue(ficha.datos().isPresent());
        assertEquals("Chocó", ficha.datos().get().ubicacionVisible());
        assertEquals(1, ficha.datos().get().atributos().size());

        assertDoesNotThrow(servicioConFallo::invalidarCache);
        assertDoesNotThrow(() -> servicioConFallo.invalidarFicha(100L));
    }

    @Test
    void degradacionEleganteCuandoNoHayCacheConfigurada() {
        ServicioConsultaPublica servicioSinCache = new ServicioConsultaPublica(autorizacion, Optional.empty());

        ResultadoConsulta<List<FichaPublicaEnmascarada>> catalogo =
                servicioSinCache.consultarCatalogoPublico(() -> List.of(hallazgoSensible));
        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> ficha =
                servicioSinCache.consultarFichaPublica(100L, () -> Optional.of(hallazgoSensible));

        assertEquals(EstadoCache.FALLBACK, catalogo.estadoCache());
        assertEquals("Chocó", catalogo.datos().getFirst().ubicacionVisible());
        assertEquals(EstadoCache.FALLBACK, ficha.estadoCache());
        assertEquals("Chocó", ficha.datos().orElseThrow().ubicacionVisible());
    }

    @Test
    void degradacionEleganteCuandoCacheFallaAlGuardarDespuesDeMiss() {
        ServicioConsultaPublica servicioFalloEscritura = new ServicioConsultaPublica(
                autorizacion,
                Optional.of(CacheConFalloStub.fallandoSoloEscritura())
        );

        ResultadoConsulta<List<FichaPublicaEnmascarada>> catalogo =
                servicioFalloEscritura.consultarCatalogoPublico(() -> List.of(hallazgoSensible));
        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> ficha =
                servicioFalloEscritura.consultarFichaPublica(100L, () -> Optional.of(hallazgoSensible));

        assertEquals(EstadoCache.FALLBACK, catalogo.estadoCache());
        assertEquals("Chocó", catalogo.datos().getFirst().ubicacionVisible());
        assertEquals(1, catalogo.datos().getFirst().atributos().size());

        assertEquals(EstadoCache.FALLBACK, ficha.estadoCache());
        assertEquals("Chocó", ficha.datos().orElseThrow().ubicacionVisible());
        assertEquals(1, ficha.datos().orElseThrow().atributos().size());
    }

    @Test
    void directorCientificoNoUsaCachePublicaPeroVisitanteEInvestigadorSi() {
        assertFalse(servicio.usaCachePublica(RolEnum.DIRECTOR_CIENTIFICO));
        assertTrue(servicio.usaCachePublica(RolEnum.VISITANTE_PUBLICO));
        assertTrue(servicio.usaCachePublica(RolEnum.INVESTIGADOR_CAMPO));
        assertTrue(servicio.usaCachePublica(RolEnum.AUDITOR_CIENTIFICO));
    }

    @Test
    void invalidarCacheEInvalidarFichaObliganARefrescarDesdeFuente() {
        AtomicInteger consultasCatalogo = new AtomicInteger(0);
        AtomicInteger consultasFicha = new AtomicInteger(0);

        servicio.consultarCatalogoPublico(() -> {
            consultasCatalogo.incrementAndGet();
            return List.of(hallazgoSensible);
        });
        servicio.consultarFichaPublica(100L, () -> {
            consultasFicha.incrementAndGet();
            return Optional.of(hallazgoSensible);
        });

        assertEquals(EstadoCache.HIT, servicio.consultarCatalogoPublico(() -> List.of(hallazgoSensible)).estadoCache());
        assertEquals(EstadoCache.HIT, servicio.consultarFichaPublica(100L, () -> Optional.of(hallazgoSensible)).estadoCache());

        servicio.invalidarFicha(100L);

        ResultadoConsulta<Optional<FichaPublicaEnmascarada>> fichaTrasInvalidar =
                servicio.consultarFichaPublica(100L, () -> {
                    consultasFicha.incrementAndGet();
                    return Optional.of(hallazgoSensible);
                });
        ResultadoConsulta<List<FichaPublicaEnmascarada>> catalogoTrasInvalidarFicha =
                servicio.consultarCatalogoPublico(() -> {
                    consultasCatalogo.incrementAndGet();
                    return List.of(hallazgoSensible);
                });

        assertEquals(EstadoCache.MISS, fichaTrasInvalidar.estadoCache());
        assertEquals(2, consultasFicha.get());
        assertEquals(EstadoCache.MISS, catalogoTrasInvalidarFicha.estadoCache());
        assertEquals(2, consultasCatalogo.get());

        assertEquals(EstadoCache.HIT, servicio.consultarCatalogoPublico(() -> List.of(hallazgoSensible)).estadoCache());

        servicio.invalidarCache();

        ResultadoConsulta<List<FichaPublicaEnmascarada>> catalogoTrasInvalidarTodo =
                servicio.consultarCatalogoPublico(() -> {
                    consultasCatalogo.incrementAndGet();
                    return List.of(hallazgoSensible);
                });
        assertEquals(EstadoCache.MISS, catalogoTrasInvalidarTodo.estadoCache());
        assertEquals(3, consultasCatalogo.get());
    }

    private static final class CacheEnMemoriaStub implements CacheFichasPublicas {

        private List<FichaPublicaEnmascarada> catalogo;
        private final Map<Long, FichaPublicaEnmascarada> fichas = new HashMap<>();

        @Override
        public Optional<List<FichaPublicaEnmascarada>> obtenerCatalogo() {
            return Optional.ofNullable(catalogo);
        }

        @Override
        public void guardarCatalogo(List<FichaPublicaEnmascarada> catalogo) {
            this.catalogo = catalogo == null ? List.of() : List.copyOf(catalogo);
        }

        @Override
        public Optional<FichaPublicaEnmascarada> obtenerFicha(Long hallazgoId) {
            return Optional.ofNullable(fichas.get(hallazgoId));
        }

        @Override
        public void guardarFicha(FichaPublicaEnmascarada ficha) {
            if (ficha != null && ficha.id() != null) {
                fichas.put(ficha.id(), ficha);
            }
        }

        @Override
        public void invalidarTodo() {
            catalogo = null;
            fichas.clear();
        }

        @Override
        public void invalidarFicha(Long hallazgoId) {
            fichas.remove(hallazgoId);
        }
    }

    private static final class CacheConFalloStub implements CacheFichasPublicas {

        private final boolean fallarEnLectura;
        private final boolean fallarEnEscritura;

        private CacheConFalloStub(boolean fallarEnLectura, boolean fallarEnEscritura) {
            this.fallarEnLectura = fallarEnLectura;
            this.fallarEnEscritura = fallarEnEscritura;
        }

        private static CacheConFalloStub fallandoTodo() {
            return new CacheConFalloStub(true, true);
        }

        private static CacheConFalloStub fallandoSoloEscritura() {
            return new CacheConFalloStub(false, true);
        }

        @Override
        public Optional<List<FichaPublicaEnmascarada>> obtenerCatalogo() {
            if (fallarEnLectura) {
                throw new RuntimeException("Redis no disponible");
            }
            return Optional.empty();
        }

        @Override
        public void guardarCatalogo(List<FichaPublicaEnmascarada> catalogo) {
            if (fallarEnEscritura) {
                throw new RuntimeException("Timeout escribiendo en Redis");
            }
        }

        @Override
        public Optional<FichaPublicaEnmascarada> obtenerFicha(Long hallazgoId) {
            if (fallarEnLectura) {
                throw new RuntimeException("Redis no disponible");
            }
            return Optional.empty();
        }

        @Override
        public void guardarFicha(FichaPublicaEnmascarada ficha) {
            if (fallarEnEscritura) {
                throw new RuntimeException("Timeout escribiendo en Redis");
            }
        }

        @Override
        public void invalidarTodo() {
            throw new RuntimeException("Redis no disponible");
        }

        @Override
        public void invalidarFicha(Long hallazgoId) {
            throw new RuntimeException("Redis no disponible");
        }
    }
}
