package co.edu.unbosque.biohub.autenticacion;

import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

public class ServicioAutenticacionRol {

    private final Map<String, Usuario> usuariosPorUsername = new HashMap<>();
    private final Map<String, SesionUsuario> sesionesActivas = new HashMap<>();

    public static Map<RolEnum, Set<PermisoOperacion>> permisosPorRolPorDefecto() {
        Map<RolEnum, Set<PermisoOperacion>> mapa = new EnumMap<>(RolEnum.class);
        mapa.put(RolEnum.DIRECTOR_CIENTIFICO, Set.of(
                PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO,
                PermisoOperacion.CONSULTAR_COORDENADAS_EXACTAS,
                PermisoOperacion.REGISTRAR_HALLAZGO,
                PermisoOperacion.RECLASIFICAR_ESPECIE,
                PermisoOperacion.AUDITAR_HISTORIAL
        ));
        mapa.put(RolEnum.INVESTIGADOR_CAMPO, Set.of(
                PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO,
                PermisoOperacion.REGISTRAR_HALLAZGO
        ));
        mapa.put(RolEnum.AUDITOR_CIENTIFICO, Set.of(
                PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO,
                PermisoOperacion.AUDITAR_HISTORIAL
        ));
        mapa.put(RolEnum.CONSUMIDOR_EXTERNO, Set.of(
                PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO,
                PermisoOperacion.CONSUMIR_API_EXTERNA
        ));
        mapa.put(RolEnum.VISITANTE_PUBLICO, Set.of(
                PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO
        ));
        return mapa;
    }

    public void registrarUsuario(String id, String username, String password, RolEnum rol) {
        Set<PermisoOperacion> permisos = permisosPorRolPorDefecto().getOrDefault(
                rol,
                Set.of(PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO)
        );
        usuariosPorUsername.put(username, new Usuario(id, username, password, rol, permisos));
    }

    public SesionUsuario iniciarSesion(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new SecurityException("Credenciales inválidas: usuario y contraseña obligatorios");
        }

        Usuario usuario = usuariosPorUsername.get(username);
        if (usuario == null || !usuario.verificarPassword(password)) {
            throw new SecurityException("Credenciales incorrectas para el usuario: " + username);
        }

        String token = UUID.randomUUID().toString();
        SesionUsuario sesion = SesionUsuario.autenticada(token, usuario, LocalDateTime.now());
        sesionesActivas.put(token, sesion);
        return sesion;
    }

    public SesionUsuario resolverSesion(String token) {
        if (token == null || token.isBlank()) {
            return SesionUsuario.visitantePublico();
        }
        SesionUsuario sesion = sesionesActivas.get(token);
        if (sesion == null) {
            return SesionUsuario.visitantePublico();
        }
        return sesion;
    }

    public void autorizar(String token, PermisoOperacion operacionRequerida) {
        SesionUsuario sesion = resolverSesion(token);
        if (!sesion.puedeEjecutar(operacionRequerida)) {
            throw new SecurityException(
                    "Acceso denegado: el rol " + sesion.getRol() + " no tiene permiso para " + operacionRequerida
            );
        }
    }

    public void cerrarSesion(String token) {
        if (token != null) {
            sesionesActivas.remove(token);
        }
    }
}