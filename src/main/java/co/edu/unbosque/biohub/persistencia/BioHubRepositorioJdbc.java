package co.edu.unbosque.biohub.persistencia;

import co.edu.unbosque.biohub.auditoria.HallazgoMemento;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
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
public class BioHubRepositorioJdbc {

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
                       h.investigador_id, h.atributos::text AS atributos_json,
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
                    parsearAtributosJson(rs.getString("atributos_json"))
            );
        });
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

    private List<AtributoDinamico> parsearAtributosJson(String json) {
        try {
            Map<String, String> mapa = mapper.readValue(json, new TypeReference<>() {});
            List<AtributoDinamico> lista = new ArrayList<>();
            mapa.forEach((k, v) -> lista.add(new AtributoDinamico(k, TipoDato.TEXTO, v)));
            return lista;
        } catch (Exception e) {
            return List.of();
        }
    }
}