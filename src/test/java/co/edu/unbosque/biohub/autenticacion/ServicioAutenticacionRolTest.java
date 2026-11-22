package co.edu.unbosque.biohub.autenticacion;

import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ServicioAutenticacionRolTest {

    private ServicioAutenticacionRol servicio;

    @BeforeEach
    void setUp() {
        servicio = new ServicioAutenticacionRol();
        servicio.registrarUsuario("U-01", "director.ana", "ClaveSegura123", RolEnum.DIRECTOR_CIENTIFICO);
        servicio.registrarUsuario("U-02", "investigador.carlos", "Campo2026", RolEnum.INVESTIGADOR_CAMPO);
    }

    @Test
    void criterio1DebeIdentificarUsuarioConSuRolAlIniciarSesion() {
        SesionUsuario sesion = servicio.iniciarSesion("director.ana", "ClaveSegura123");

        assertTrue(sesion.isAutenticada());
        assertEquals(RolEnum.DIRECTOR_CIENTIFICO, sesion.getRol());
        assertEquals("director.ana", sesion.getUsername());
    }

    @Test
    void criterio2DebeRechazarAccesoConCredencialesIncorrectas() {
        SecurityException errorClave = assertThrows(
                SecurityException.class,
                () -> servicio.iniciarSesion("director.ana", "ClaveErronea")
        );
        assertTrue(errorClave.getMessage().contains("Credenciales incorrectas"));

        SecurityException errorUsuario = assertThrows(
                SecurityException.class,
                () -> servicio.iniciarSesion("no.existe", "ClaveSegura123")
        );
        assertTrue(errorUsuario.getMessage().contains("Credenciales incorrectas"));
    }

    @Test
    void criterio3DebeRechazarFuncionQueSuRolNoPermite() {
        SesionUsuario sesionInvestigador = servicio.iniciarSesion("investigador.carlos", "Campo2026");

        assertDoesNotThrow(() -> servicio.autorizar(
                sesionInvestigador.getToken(),
                PermisoOperacion.REGISTRAR_HALLAZGO
        ));

        SecurityException denegado = assertThrows(
                SecurityException.class,
                () -> servicio.autorizar(
                        sesionInvestigador.getToken(),
                        PermisoOperacion.CONSULTAR_COORDENADAS_EXACTAS
                )
        );
        assertTrue(denegado.getMessage().contains("INVESTIGADOR_CAMPO"));
    }

    @Test
    void criterio4DebeTratarComoVisitantePublicoCuandoNoIniciaSesion() {
        SesionUsuario sesionAnonima = servicio.resolverSesion(null);

        assertFalse(sesionAnonima.isAutenticada());
        assertEquals(RolEnum.VISITANTE_PUBLICO, sesionAnonima.getRol());

        assertDoesNotThrow(() -> servicio.autorizar(null, PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO));

        SecurityException denegado = assertThrows(
                SecurityException.class,
                () -> servicio.autorizar(null, PermisoOperacion.REGISTRAR_HALLAZGO)
        );
        assertTrue(denegado.getMessage().contains("VISITANTE_PUBLICO"));
    }
}