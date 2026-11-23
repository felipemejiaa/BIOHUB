package co.edu.unbosque.biohub.persistencia;

import co.edu.unbosque.biohub.auditoria.HallazgoMemento;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.hallazgos.fichas.CatalogoAtributosTaxonomicos;
import co.edu.unbosque.biohub.hallazgos.fichas.RepositorioAtributosGrupo;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
@Profile("postgres")
public class BioHubRepositorioJdbc implements RepositorioAtributosGrupo {

    private final JdbcTemplate jdbc;
    private final JsonMapper mapper = JsonMapper.builder().build();

    public BioHubRepositorioJdbc(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record UsuarioFila(String id, String username, String passwordHash, RolEnum rol) {}

    public List<UsuarioFila> listarUsuarios() {
        return jdbc.query(
                "SELECT id, username, password_hash, rol FROM usuario",
                (rs, rowNum) -> new UsuarioFila(
                        rs.getString("id"),
                        rs.getString("username"),
                        rs.getString("password_hash"),
                        RolEnum.valueOf(rs.getString("rol"))
                )
        );
    }

    public void guardarUsuario(String id, String username, String passwordHash, RolEnum rol) {
        jdbc.update(
                "INSERT INTO usuario (id, username, password_hash, rol) VALUES (?, ?, ?, ?) ON CONFLICT (username) DO NOTHING",
                id, username, passwordHash, rol.name()
        );
    }

    public List<Hallazgo> listarHallazgos() {
        String sql = """
                SELECT h.id AS h_id, h.latitud, h.longitud, h.departamento, h.fecha_observacion,
                       h.investigador_id, h.grupo_taxonomico, h.atributos::text AS atributos_json,
                       e.id AS e_id, e.nombre_cientifico, e.nombre_comun, e.estado_conservacion, e.es_sensible
                FROM hallazgo h
                JOIN especie e ON h.especie_id = e.id
                ORDER BY h.id
                """;
        return jdbc.query(sql, (rs, rowNum) -> {
            Especie especie = new Especie(
                    rs.getLong("e_id"),
                    rs.getString("nombre_cientifico"),
                    rs.getString("nombre_comun"),
                    rs.getString("estado_conservacion"),
                    rs.getBoolean("es_sensible")
            );
            return new Hallazgo(
                    rs.getLong("h_id"),
                    especie,
                    rs.getDouble("latitud"),
                    rs.getDouble("longitud"),
                    rs.getString("departamento"),
                    rs.getTimestamp("fecha_observacion").toLocalDateTime(),
                    rs.getString("investigador_id"),
                    parsearAtributosJson(rs.getString("atributos_json"), rs.getString("grupo_taxonomico"))
            );
        });
    }

    @Override
    public Map<String, Map<String, TipoDato>> listarAtributosPorGrupo() {
        String sql = """
                SELECT grupo_taxonomico, atributos_requeridos::text AS atributos_json
                FROM configuracion_grupo_taxonomico
                ORDER BY grupo_taxonomico
                """;
        List<Map.Entry<String, Map<String, TipoDato>>> filas = jdbc.query(
                sql,
                (rs, rowNum) -> Map.entry(
                        rs.getString("grupo_taxonomico"),
                        parsearDefinicionAtributosJson(rs.getString("atributos_json"))
                )
        );
        Map<String, Map<String, TipoDato>> resultado = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, TipoDato>> fila : filas) {
            resultado.put(fila.getKey(), fila.getValue());
        }
        return resultado;
    }

    @Override
    public void guardarAtributosGrupo(String grupoTaxonomico, Map<String, TipoDato> atributos) {
        if (grupoTaxonomico == null || grupoTaxonomico.isBlank()) {
            throw new IllegalArgumentException("El grupo taxonómico es obligatorio");
        }
        String grupoNormalizado = grupoTaxonomico.trim().toUpperCase();
        jdbc.update(
                """
                INSERT INTO configuracion_grupo_taxonomico (grupo_taxonomico, atributos_requeridos)
                VALUES (?, ?::jsonb)
                ON CONFLICT (grupo_taxonomico)
                DO UPDATE SET atributos_requeridos = EXCLUDED.atributos_requeridos
                """,
                grupoNormalizado,
                serializarDefinicionAtributosJson(atributos)
        );
    }

    public Hallazgo insertarHallazgo(Especie especie, double latitud, double longitud,
                                     String departamento, LocalDateTime fecha,
                                     String investigadorId, String grupoTaxonomico,
                                     List<AtributoDinamico> atributos) {
        Long especieId = jdbc.queryForObject(
                """
                INSERT INTO especie (nombre_cientifico, nombre_comun, estado_conservacion, es_sensible)
                VALUES (?, ?, ?, ?) RETURNING id
                """,
                Long.class,
                especie.getNombreCientifico(),
                especie.getNombreComun(),
                especie.getEstadoConservacion(),
                especie.esSensible()
        );

        Long hallazgoId = jdbc.queryForObject(
                """
                INSERT INTO hallazgo (especie_id, latitud, longitud, departamento, fecha_observacion, investigador_id, grupo_taxonomico, atributos)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb) RETURNING id
                """,
                Long.class,
                especieId,
                latitud,
                longitud,
                departamento,
                Timestamp.valueOf(fecha),
                investigadorId,
                grupoTaxonomico,
                serializarAtributosJson(atributos)
        );

        Especie especiePersistida = new Especie(
                especieId,
                especie.getNombreCientifico(),
                especie.getNombreComun(),
                especie.getEstadoConservacion(),
                especie.esSensible()
        );
        return new Hallazgo(hallazgoId, especiePersistida, latitud, longitud, departamento, fecha, investigadorId, atributos);
    }

    public void guardarVersionMemento(HallazgoMemento memento) {
        Hallazgo estado = memento.getEstadoHallazgo();
        Especie esp = estado.getEspecie();

        Long especieId = jdbc.queryForObject(
                """
                INSERT INTO especie (nombre_cientifico, nombre_comun, estado_conservacion, es_sensible)
                VALUES (?, ?, ?, ?) RETURNING id
                """,
                Long.class,
                esp.getNombreCientifico(),
                esp.getNombreComun(),
                esp.getEstadoConservacion(),
                esp.esSensible()
        );

        jdbc.update(
                """
                INSERT INTO version_hallazgo (hallazgo_id, numero_version, fecha_version, especie_id, autorizado_por, rol_autorizador, motivo)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                ON CONFLICT (hallazgo_id, numero_version) DO NOTHING
                """,
                estado.getId(),
                memento.getVersion(),
                Timestamp.valueOf(memento.getFechaVersion()),
                especieId,
                memento.getAutorizadoPor(),
                memento.getRolAutorizador().name(),
                memento.getMotivo()
        );

        jdbc.update("UPDATE hallazgo SET especie_id = ? WHERE id = ?", especieId, estado.getId());
    }

    private String serializarAtributosJson(List<AtributoDinamico> atributos) {
        try {
            Map<String, String> mapa = new LinkedHashMap<>();
            if (atributos != null) {
                for (AtributoDinamico a : atributos) {
                    mapa.put(a.nombre(), a.valor());
                }
            }
            return mapper.writeValueAsString(mapa);
        } catch (Exception e) {
            return "{}";
        }
    }

    private List<AtributoDinamico> parsearAtributosJson(String json, String grupoTaxonomico) {
        try {
            Map<String, String> mapa = mapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {});
            Map<String, TipoDato> definicion = CatalogoAtributosTaxonomicos.obtenerAtributos(grupoTaxonomico);
            List<AtributoDinamico> lista = new ArrayList<>();
            mapa.forEach((k, v) -> lista.add(new AtributoDinamico(
                    k,
                    definicion.getOrDefault(k, TipoDato.TEXTO),
                    v
            )));
            return lista;
        } catch (Exception e) {
            return List.of();
        }
    }

    private String serializarDefinicionAtributosJson(Map<String, TipoDato> atributos) {
        try {
            Map<String, String> mapa = new LinkedHashMap<>();
            if (atributos != null) {
                atributos.forEach((nombre, tipo) -> {
                    if (nombre != null && !nombre.isBlank() && tipo != null) {
                        mapa.put(nombre.trim(), tipo.name());
                    }
                });
            }
            return mapper.writeValueAsString(mapa);
        } catch (Exception e) {
            return "{}";
        }
    }

    private Map<String, TipoDato> parsearDefinicionAtributosJson(String json) {
        try {
            Map<String, String> mapa = mapper.readValue(json, new TypeReference<LinkedHashMap<String, String>>() {});
            Map<String, TipoDato> definicion = new LinkedHashMap<>();
            mapa.forEach((nombre, tipoStr) -> {
                TipoDato tipo = TipoDato.TEXTO;
                if (tipoStr != null && !tipoStr.isBlank()) {
                    try {
                        tipo = TipoDato.valueOf(tipoStr.trim().toUpperCase());
                    } catch (IllegalArgumentException ignored) {
                        tipo = TipoDato.TEXTO;
                    }
                }
                definicion.put(nombre, tipo);
            });
            return definicion;
        } catch (Exception e) {
            return Map.of();
        }
    }
}