package co.edu.unbosque.biohub.consulta;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.seguridad.AutorizacionUbicacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Servicio de consulta pública con Cache-Aside y degradación elegante (ASR-01, ASR-02, HU-02).
 * Solo almacena y entrega {@link FichaPublicaEnmascarada} a través de {@link AutorizacionUbicacion}.
 */
@Service
public class ServicioConsultaPublica {

    private static final Logger log = LoggerFactory.getLogger(ServicioConsultaPublica.class);

    private final AutorizacionUbicacion autorizacionUbicacion;
    private final Optional<CacheFichasPublicas> cacheFichas;

    public ServicioConsultaPublica(AutorizacionUbicacion autorizacionUbicacion,
                                   Optional<CacheFichasPublicas> cacheFichas) {
        this.autorizacionUbicacion = Objects.requireNonNull(
                autorizacionUbicacion,
                "La autorización de ubicación es obligatoria"
        );
        this.cacheFichas = cacheFichas == null ? Optional.empty() : cacheFichas;
    }

    public boolean usaCachePublica(RolEnum rol) {
        return !autorizacionUbicacion.revelaCoordenadas(rol);
    }

    public ResultadoConsulta<List<FichaPublicaEnmascarada>> consultarCatalogoPublico(
            Supplier<List<Hallazgo>> proveedorHallazgos) {
        Objects.requireNonNull(proveedorHallazgos, "El proveedor de hallazgos es obligatorio");
        return consultarConCache(
                CacheFichasPublicas::obtenerCatalogo,
                () -> enmascararLista(proveedorHallazgos.get()),
                CacheFichasPublicas::guardarCatalogo
        );
    }

    public ResultadoConsulta<Optional<FichaPublicaEnmascarada>> consultarFichaPublica(
            Long hallazgoId,
            Supplier<Optional<Hallazgo>> proveedorHallazgo) {
        Objects.requireNonNull(hallazgoId, "El identificador del hallazgo es obligatorio");
        Objects.requireNonNull(proveedorHallazgo, "El proveedor del hallazgo es obligatorio");
        return consultarConCache(
                cache -> cache.obtenerFicha(hallazgoId).map(Optional::of),
                () -> proveedorHallazgo.get().map(h -> FichaPublicaEnmascarada.desdeHallazgo(h, autorizacionUbicacion)),
                (cache, resultado) -> resultado.ifPresent(cache::guardarFicha)
        );
    }

    public void invalidarCache() {
        cacheFichas.ifPresent(cache -> {
            try {
                cache.invalidarTodo();
            } catch (RuntimeException ex) {
                log.warn("No se pudo invalidar el catálogo en caché; se continúa normalmente: {}", ex.getMessage());
            }
        });
    }

    public void invalidarFicha(Long hallazgoId) {
        if (hallazgoId == null) {
            return;
        }
        cacheFichas.ifPresent(cache -> {
            try {
                cache.invalidarFicha(hallazgoId);
                cache.invalidarTodo();
            } catch (RuntimeException ex) {
                log.warn("No se pudo invalidar la ficha {} en caché; se continúa normalmente: {}", hallazgoId, ex.getMessage());
            }
        });
    }

    private <T> ResultadoConsulta<T> consultarConCache(
            Function<CacheFichasPublicas, Optional<T>> lectorCache,
            Supplier<T> cargadorFuente,
            BiCacheWriter<T> escritorCache) {
        if (cacheFichas.isEmpty()) {
            return new ResultadoConsulta<>(cargadorFuente.get(), EstadoCache.FALLBACK);
        }

        CacheFichasPublicas cache = cacheFichas.get();
        try {
            Optional<T> enCache = lectorCache.apply(cache);
            if (enCache.isPresent()) {
                return new ResultadoConsulta<>(enCache.get(), EstadoCache.HIT);
            }
        } catch (RuntimeException ex) {
            log.warn("Fallo leyendo la caché pública; aplicando degradación elegante: {}", ex.getMessage());
            return new ResultadoConsulta<>(cargadorFuente.get(), EstadoCache.FALLBACK);
        }

        T resultado = cargadorFuente.get();
        try {
            escritorCache.escribir(cache, resultado);
            return new ResultadoConsulta<>(resultado, EstadoCache.MISS);
        } catch (RuntimeException ex) {
            log.warn("Fallo guardando en la caché pública; aplicando degradación elegante: {}", ex.getMessage());
            return new ResultadoConsulta<>(resultado, EstadoCache.FALLBACK);
        }
    }

    private List<FichaPublicaEnmascarada> enmascararLista(List<Hallazgo> hallazgos) {
        if (hallazgos == null || hallazgos.isEmpty()) {
            return List.of();
        }
        return hallazgos.stream()
                .map(h -> FichaPublicaEnmascarada.desdeHallazgo(h, autorizacionUbicacion))
                .toList();
    }

    @FunctionalInterface
    private interface BiCacheWriter<T> {
        void escribir(CacheFichasPublicas cache, T valor);
    }
}
