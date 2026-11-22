package co.edu.unbosque.biohub.autenticacion;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.Set;

import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

public final class SesionUsuario {

    private final String token;
    private final String usuarioId;
    private final String username;
    private final RolEnum rol;
    private final Set<PermisoOperacion> permisos;
    private final LocalDateTime fechaInicio;
    private final boolean autenticada;

    private SesionUsuario(String token, String usuarioId, String username, RolEnum rol,
                          Set<PermisoOperacion> permisos, LocalDateTime fechaInicio, boolean autenticada) {
        this.token = token;
        this.usuarioId = usuarioId;
        this.username = username;
        this.rol = rol;
        this.permisos = Set.copyOf(permisos);
        this.fechaInicio = fechaInicio;
        this.autenticada = autenticada;
    }

    public static SesionUsuario autenticada(String token, Usuario usuario, LocalDateTime fechaInicio) {
        Objects.requireNonNull(token, "El token es obligatorio");
        Objects.requireNonNull(usuario, "El usuario es obligatorio");
        return new SesionUsuario(
                token,
                usuario.getId(),
                usuario.getUsername(),
                usuario.getRol(),
                usuario.getPermisos(),
                fechaInicio,
                true
        );
    }

    public static SesionUsuario visitantePublico() {
        return new SesionUsuario(
                "PUBLIC-ANONYMOUS",
                "VISITANTE",
                "visitante",
                RolEnum.VISITANTE_PUBLICO,
                Set.of(PermisoOperacion.CONSULTAR_HALLAZGO_PUBLICO),
                LocalDateTime.now(),
                false
        );
    }

    public boolean puedeEjecutar(PermisoOperacion permiso) {
        return permisos.contains(permiso);
    }

    public String getToken() { return token; }
    public String getUsuarioId() { return usuarioId; }
    public String getUsername() { return username; }
    public RolEnum getRol() { return rol; }
    public Set<PermisoOperacion> getPermisos() { return permisos; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public boolean isAutenticada() { return autenticada; }
}