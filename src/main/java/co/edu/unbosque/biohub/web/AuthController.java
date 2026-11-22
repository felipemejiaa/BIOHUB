package co.edu.unbosque.biohub.web;

import co.edu.unbosque.biohub.autenticacion.ServicioAutenticacionRol;
import co.edu.unbosque.biohub.autenticacion.SesionUsuario;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final ServicioAutenticacionRol servicioAuth;

    public AuthController(ServicioAutenticacionRol servicioAuth) {
        this.servicioAuth = servicioAuth;
    }

    public record LoginRequest(String username, String password) {}
    public record RegistroRequest(String username, String password, RolEnum rol) {}

    @PostMapping("/registro")
    public ResponseEntity<?> registrar(@RequestBody RegistroRequest request) {
        if (request.username() == null || request.username().isBlank()
                || request.password() == null || request.password().isBlank()
                || request.rol() == null) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Usuario, contraseña y rol son obligatorios"));
        }
        String id = "U-" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        servicioAuth.registrarUsuario(id, request.username().trim(), request.password(), request.rol());
        SesionUsuario sesion = servicioAuth.iniciarSesion(request.username().trim(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(sesion);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            SesionUsuario sesion = servicioAuth.iniciarSesion(request.username(), request.password());
            return ResponseEntity.ok(sesion);
        } catch (SecurityException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/sesion")
    public SesionUsuario obtenerSesion(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        return servicioAuth.resolverSesion(token);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        servicioAuth.cerrarSesion(token);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada"));
    }
}