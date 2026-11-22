package co.edu.unbosque.biohub.auditoria;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

class ServicioReclasificacionTaxonomicaTest {

    private ServicioReclasificacionTaxonomica servicio;
    private Especie ranaInicial;
    private Especie ranaReclasificada;

    @BeforeEach
    void setUp() {
        servicio = new ServicioReclasificacionTaxonomica(new HistorialTaxonomico());
        ranaInicial = new Especie(10L, "Dendrobates truncatus", "Rana venenosa", "LC", false);
        ranaReclasificada = new Especie(25L, "Andinobates viridis", "Rana verde del Chocó", "EN", true);

        Hallazgo hallazgoEnero = new Hallazgo(
                100L,
                ranaInicial,
                5.6922,
                -76.6581,
                "Chocó",
                LocalDateTime.of(2026, 1, 15, 9, 30),
                "INV-001",
                List.of(new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"))
        );

        servicio.registrarHallazgoInicial(hallazgoEnero, LocalDateTime.of(2026, 1, 16, 10, 0));
    }

    @Test
    void debeReconstruirHistoriaYCompararEneroContraDiciembre() {
        LocalDateTime fechaReclasificacion = LocalDateTime.of(2026, 12, 10, 14, 0);

        Hallazgo actualizado = servicio.reclasificar(
                100L,
                ranaReclasificada,
                "DIR-900",
                RolEnum.DIRECTOR_CIENTIFICO,
                "Secuenciación mitocondrial confirma nueva especie endémica",
                fechaReclasificacion
        );

        assertEquals("Andinobates viridis", actualizado.getEspecie().getNombreCientifico());

        Hallazgo estadoEnFebrero = servicio.consultarEstadoEnFecha(
                100L,
                LocalDateTime.of(2026, 2, 20, 12, 0),
                RolEnum.AUDITOR_CIENTIFICO
        );
        assertEquals("Dendrobates truncatus", estadoEnFebrero.getEspecie().getNombreCientifico());

        ComparacionTaxonomica comparacion = servicio.compararEneroVsDiciembre(
                100L,
                LocalDateTime.of(2026, 1, 31, 23, 59),
                LocalDateTime.of(2026, 12, 31, 23, 59),
                RolEnum.AUDITOR_CIENTIFICO
        );

        assertTrue(comparacion.huboCambioDeEspecie());
        assertEquals("Dendrobates truncatus", comparacion.especieAnterior().getNombreCientifico());
        assertEquals("Andinobates viridis", comparacion.especiePosterior().getNombreCientifico());
        assertEquals("DIR-900", comparacion.autorizadoPorPosterior());
        assertEquals("Secuenciación mitocondrial confirma nueva especie endémica", comparacion.motivoCambio());
    }

    @Test
    void debeBloquearReclasificacionPorVisitantePublico() {
        SecurityException excepcion = assertThrows(SecurityException.class, () -> servicio.reclasificar(
                100L,
                ranaReclasificada,
                "VISITANTE-1",
                RolEnum.VISITANTE_PUBLICO,
                "Intento no autorizado",
                LocalDateTime.of(2026, 6, 1, 10, 0)
        ));
        assertTrue(excepcion.getMessage().contains("VISITANTE_PUBLICO"));
    }

    @Test
    void debeBloquearAuditoriaAConsumidorExterno() {
        SecurityException excepcion = assertThrows(SecurityException.class, () -> servicio.auditarHistorialCompleto(
                100L,
                RolEnum.CONSUMIDOR_EXTERNO
        ));
        assertTrue(excepcion.getMessage().contains("CONSUMIDOR_EXTERNO"));
    }
}