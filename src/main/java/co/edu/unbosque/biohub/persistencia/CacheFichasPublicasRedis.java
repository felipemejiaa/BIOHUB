package co.edu.unbosque.biohub.persistencia;

import co.edu.unbosque.biohub.consulta.CacheFichasPublicas;
import co.edu.unbosque.biohub.consulta.FichaPublicaEnmascarada;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptador de {@link CacheFichasPublicas} respaldado por Redis (HU-02 / ASR-02).
 * Solo almacena JSON de {@link FichaPublicaEnmascarada} con TTL configurable.
 */
@Component
@ConditionalOnBean(StringRedisTemplate.class)
public class CacheFichasPublicasRedis implements CacheFichasPublicas {

    public static final String CLAVE_CATALOGO = "biohub:publico:catalogo";
    public static final String PREFIJO_FICHA = "biohub:publico:ficha:";

    private static final Logger log = LoggerFactory.getLogger(CacheFichasPublicasRedis.class);

    private final StringRedisTemplate redis;
    private final JsonMapper mapper = JsonMapper.builder().build();
    private final Duration ttl;

    public CacheFichasPublicasRedis(
            StringRedisTemplate redis,
            @Value("${biohub.cache.ttl-minutos:5}") long ttlMinutos) {
        this.redis = redis;
        this.ttl = Duration.ofMinutes(Math.max(1L, ttlMinutos));
    }

    @Override
    public Optional<List<FichaPublicaEnmascarada>> obtenerCatalogo() {
        String json = redis.opsForValue().get(CLAVE_CATALOGO);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(json, new TypeReference<List<FichaPublicaEnmascarada>>() {}));
        } catch (JacksonException ex) {
            log.warn("No se pudo deserializar el catálogo en caché; se recalculará: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void guardarCatalogo(List<FichaPublicaEnmascarada> catalogo) {
        List<FichaPublicaEnmascarada> lista = catalogo == null ? List.of() : catalogo;
        redis.opsForValue().set(CLAVE_CATALOGO, serializar(lista), ttl);
    }

    @Override
    public Optional<FichaPublicaEnmascarada> obtenerFicha(Long hallazgoId) {
        if (hallazgoId == null) {
            return Optional.empty();
        }
        String json = redis.opsForValue().get(PREFIJO_FICHA + hallazgoId);
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.readValue(json, FichaPublicaEnmascarada.class));
        } catch (JacksonException ex) {
            log.warn("No se pudo deserializar la ficha {} en caché; se recalculará: {}", hallazgoId, ex.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public void guardarFicha(FichaPublicaEnmascarada ficha) {
        if (ficha == null || ficha.id() == null) {
            return;
        }
        redis.opsForValue().set(PREFIJO_FICHA + ficha.id(), serializar(ficha), ttl);
    }

    @Override
    public void invalidarTodo() {
        redis.delete(CLAVE_CATALOGO);
        Set<String> clavesFichas = redis.keys(PREFIJO_FICHA + "*");
        if (clavesFichas != null && !clavesFichas.isEmpty()) {
            redis.delete(clavesFichas);
        }
    }

    @Override
    public void invalidarFicha(Long hallazgoId) {
        if (hallazgoId != null) {
            redis.delete(PREFIJO_FICHA + hallazgoId);
        }
    }

    private String serializar(Object valor) {
        try {
            return mapper.writeValueAsString(valor);
        } catch (JacksonException ex) {
            throw new IllegalStateException("Error serializando ficha pública para Redis", ex);
        }
    }
}
