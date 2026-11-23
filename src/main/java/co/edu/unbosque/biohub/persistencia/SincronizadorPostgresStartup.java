package co.edu.unbosque.biohub.persistencia;

import co.edu.unbosque.biohub.auditoria.ServicioReclasificacionTaxonomica;
import co.edu.unbosque.biohub.autenticacion.ServicioAutenticacionRol;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.hallazgos.fichas.CatalogoAtributosTaxonomicos;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Component
@Profile("postgres")
public class SincronizadorPostgresStartup {

    private final DataSource dataSource;
    private final JdbcTemplate jdbc;
    private final BioHubRepositorioJdbc repo;
    private final ServicioAutenticacionRol servicioAuth;
    private final ServicioReclasificacionTaxonomica servicioAuditoria;
    private final List<Hallazgo> hallazgosEnMemoria;

    public SincronizadorPostgresStartup(DataSource dataSource,
                                        JdbcTemplate jdbc,
                                        BioHubRepositorioJdbc repo,
                                        ServicioAutenticacionRol servicioAuth,
                                        ServicioReclasificacionTaxonomica servicioAuditoria,
                                        List<Hallazgo> hallazgosEnMemoria) {
        this.dataSource = dataSource;
        this.jdbc = jdbc;
        this.repo = repo;
        this.servicioAuth = servicioAuth;
        this.servicioAuditoria = servicioAuditoria;
        this.hallazgosEnMemoria = hallazgosEnMemoria;
    }

    @PostConstruct
    public void sincronizarDesdeBaseDeDatos() {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("db/schema.sql"),
                new ClassPathResource("db/seed.sql")
        );
        populator.execute(dataSource);

        jdbc.execute("SELECT setval(pg_get_serial_sequence('especie', 'id'), COALESCE((SELECT MAX(id) FROM especie), 1))");
        jdbc.execute("SELECT setval(pg_get_serial_sequence('hallazgo', 'id'), COALESCE((SELECT MAX(id) FROM hallazgo), 1))");
        jdbc.execute("SELECT setval(pg_get_serial_sequence('version_hallazgo', 'id'), COALESCE((SELECT MAX(id) FROM version_hallazgo), 1))");

        repo.guardarUsuario("U-DIR-01", "director", "Director2026*", RolEnum.DIRECTOR_CIENTIFICO);
        repo.guardarUsuario("U-INV-01", "investigador", "Campo2026*", RolEnum.INVESTIGADOR_CAMPO);
        repo.guardarUsuario("U-AUD-01", "auditor", "Auditor2026*", RolEnum.AUDITOR_CIENTIFICO);
        repo.guardarUsuario("U-EXT-01", "externo", "Externo2026*", RolEnum.CONSUMIDOR_EXTERNO);

        for (BioHubRepositorioJdbc.UsuarioFila u : repo.listarUsuarios()) {
            servicioAuth.registrarUsuario(u.id(), u.username(), u.passwordHash(), u.rol());
        }

        Map<String, Map<String, TipoDato>> definiciones = repo.listarAtributosPorGrupo();
        definiciones.forEach(CatalogoAtributosTaxonomicos::reemplazarAtributos);

        List<Hallazgo> desdeBd = repo.listarHallazgos();
        if (!desdeBd.isEmpty()) {
            hallazgosEnMemoria.clear();
            hallazgosEnMemoria.addAll(desdeBd);
            for (Hallazgo h : desdeBd) {
                servicioAuditoria.registrarHallazgoInicial(h, h.getFechaObservacion());
            }
        }
    }
}