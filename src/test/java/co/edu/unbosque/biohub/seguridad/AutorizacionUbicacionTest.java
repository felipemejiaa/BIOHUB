package co.edu.unbosque.biohub.seguridad;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AutorizacionUbicacionTest {

    private AutorizacionUbicacion autorizacion;
    private Hallazgo hallazgo;

    @BeforeEach
    void setUp() {
        autorizacion = AutorizacionUbicacion.conAsignacionPorDefecto();

        Especie especie = new Especie(
                1L, "Dendrobates truncatus", "Rana venenosa", "EN", true
        );

        hallazgo = new Hallazgo(
                100L,
                especie,
                5.6922,
                -76.6581,
                "Chocó",
                LocalDateTime.of(2026, 3, 15, 9, 30),
                "INV-001",
                List.of(new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"))
        );
    }

    @Test
    void directorCientificoDebeVerCoordenadasExactas() {
        String ubicacion = autorizacion.decidir(RolEnum.DIRECTOR_CIENTIFICO, hallazgo);

        assertTrue(ubicacion.contains("5.6922"));
        assertTrue(ubicacion.contains("-76.6581"));
        assertTrue(ubicacion.contains("Chocó"));
    }

    @Test
    void directorDebeRevelarCoordenadas() {
        boolean revela = autorizacion.revelaCoordenadas(RolEnum.DIRECTOR_CIENTIFICO);

        assertTrue(revela);
    }

    @Test
    void investigadorDebeVerSoloDepartamento() {
        String ubicacion = autorizacion.decidir(RolEnum.INVESTIGADOR_CAMPO, hallazgo);

        assertEquals("Chocó", ubicacion);
        assertFalse(ubicacion.contains("5.6922"));
    }

    @Test
    void visitanteDebeVerSoloDepartamento() {
        String ubicacion = autorizacion.decidir(RolEnum.VISITANTE_PUBLICO, hallazgo);

        assertEquals("Chocó", ubicacion);
    }

    @Test
    void visitanteNoDebeRevelarCoordenadas() {
        boolean revela = autorizacion.revelaCoordenadas(RolEnum.VISITANTE_PUBLICO);

        assertFalse(revela);
    }

    @Test
    void todosLosRolesDebenRecibirUbicacion() {
        for (RolEnum rol : RolEnum.values()) {
            String ubicacion = autorizacion.decidir(rol, hallazgo);

            assertFalse(ubicacion.isEmpty());
        }
    }
}