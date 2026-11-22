package co.edu.unbosque.biohub.web;

import co.edu.unbosque.biohub.auditoria.HistorialTaxonomico;
import co.edu.unbosque.biohub.auditoria.ServicioReclasificacionTaxonomica;
import co.edu.unbosque.biohub.autenticacion.ServicioAutenticacionRol;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.seguridad.AutorizacionUbicacion;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Configuration
public class BioHubConfiguracionDemo {

    @Bean
    public ServicioAutenticacionRol servicioAutenticacionRol() {
        ServicioAutenticacionRol auth = new ServicioAutenticacionRol();
        auth.registrarUsuario("U-DIR-01", "director", "Director2026*", RolEnum.DIRECTOR_CIENTIFICO);
        auth.registrarUsuario("U-INV-01", "investigador", "Campo2026*", RolEnum.INVESTIGADOR_CAMPO);
        auth.registrarUsuario("U-AUD-01", "auditor", "Auditor2026*", RolEnum.AUDITOR_CIENTIFICO);
        auth.registrarUsuario("U-EXT-01", "externo", "Externo2026*", RolEnum.CONSUMIDOR_EXTERNO);
        return auth;
    }

    @Bean
    public List<Hallazgo> repositorioHallazgosMemoria(ServicioReclasificacionTaxonomica servicioAuditoria) {
        List<Hallazgo> hallazgos = new CopyOnWriteArrayList<>();

        Especie ranaChoco = new Especie(10L, "Dendrobates truncatus", "Rana venenosa", "EN", true);
        Hallazgo hallazgo1 = new Hallazgo(
                100L,
                ranaChoco,
                5.6922,
                -76.6581,
                "Chocó",
                LocalDateTime.of(2026, 1, 15, 9, 30),
                "U-INV-01",
                List.of(
                        new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                        new AtributoDinamico("grabacionAcustica", TipoDato.TEXTO, "canto_nocturno_01.wav")
                )
        );

        Especie condorAndino = new Especie(20L, "Vultur gryphus", "Cóndor de los Andes", "VU", true);
        Hallazgo hallazgo2 = new Hallazgo(
                101L,
                condorAndino,
                4.6097,
                -74.0817,
                "Cundinamarca",
                LocalDateTime.of(2026, 3, 10, 7, 15),
                "U-INV-01",
                List.of(
                        new AtributoDinamico("rutaGps", TipoDato.TEXTO, "Páramo de Chingaza - Sector Norte"),
                        new AtributoDinamico("muestraAdn", TipoDato.TEXTO, "ADN-VG-2026-88")
                )
        );

        hallazgos.add(hallazgo1);
        hallazgos.add(hallazgo2);
        servicioAuditoria.registrarHallazgoInicial(hallazgo1, LocalDateTime.of(2026, 1, 16, 10, 0));
        servicioAuditoria.registrarHallazgoInicial(hallazgo2, LocalDateTime.of(2026, 3, 11, 8, 0));
        return hallazgos;
    }

    @Bean
    public ServicioReclasificacionTaxonomica servicioReclasificacionTaxonomica() {
        return new ServicioReclasificacionTaxonomica(new HistorialTaxonomico());
    }

    @Bean
    public AutorizacionUbicacion autorizacionUbicacion() {
        return AutorizacionUbicacion.conAsignacionPorDefecto();
    }
}