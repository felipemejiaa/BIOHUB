package co.edu.unbosque.biohub.autenticacion;

import java.util.Objects;
import java.util.Set;

import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

public final class Usuario {

    private final String id;
    private final String username;
    private final String passwordHash;
    private final RolEnum rol;
    private final Set<PermisoOperacion> permisos;

    public Usuario(String id, String username, String passwordHash, RolEnum rol, Set<PermisoOperacion> permisos) {
        this.id = Objects.requireNonNull(id, "El id es obligatorio");
        this.username = Objects.requireNonNull(username, "El usuario es obligatorio");
        this.passwordHash = Objects.requireNonNull(passwordHash, "La contraseña es obligatoria");
        this.rol = Objects.requireNonNull(rol, "El rol es obligatorio");
        this.permisos = Set.copyOf(permisos);
    }

    public boolean verificarPassword(String passwordPlana) {
        return passwordPlana != null && this.passwordHash.equals(passwordPlana);
    }

    public boolean tienePermiso(PermisoOperacion permiso) {
        return permisos.contains(permiso);
    }

    public String getId() { return id; }
    public String getUsername() { return username; }
    public RolEnum getRol() { return rol; }
    public Set<PermisoOperacion> getPermisos() { return permisos; }
}