package co.edu.unbosque.biohub.consulta;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;
import co.edu.unbosque.biohub.seguridad.AutorizacionUbicacion;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Representación pública e inmutable de un hallazgo ya enmascarada (ASR-01 / HU-02).
 * Por diseño omite latitud, longitud y atributos dinámicos de ubicación (como rutaGps)
 * para garantizar que nunca se almacenen coordenadas exactas en la caché.
 */
public record FichaPublicaEnmascarada(
        Long id,
        String nombreCientifico,
        String nombreComun,
        String estadoConservacion,
        boolean esSensible,
        String ubicacionVisible,
        String departamento,
        LocalDateTime fechaObservacion,
        List<AtributoDinamico> atributos
) {

    private static final Pattern PATRON_ATRIBUTO_UBICACION =
            Pattern.compile("(?i).*(gps|coord|latitud|longitud).*");

    public FichaPublicaEnmascarada {
        atributos = atributos == null ? List.of() : List.copyOf(atributos);
    }

    public static FichaPublicaEnmascarada desdeHallazgo(
            Hallazgo hallazgo,
            AutorizacionUbicacion autorizacionUbicacion) {
        Objects.requireNonNull(hallazgo, "El hallazgo es obligatorio");
        Objects.requireNonNull(autorizacionUbicacion, "La autorización de ubicación es obligatoria");

        String ubicacionSegura = autorizacionUbicacion.decidir(RolEnum.VISITANTE_PUBLICO, hallazgo);
        List<AtributoDinamico> atributosSeguros = filtrarAtributosPublicos(hallazgo.getAtributos());

        return new FichaPublicaEnmascarada(
                hallazgo.getId(),
                hallazgo.getEspecie().getNombreCientifico(),
                hallazgo.getEspecie().getNombreComun(),
                hallazgo.getEspecie().getEstadoConservacion(),
                hallazgo.getEspecie().esSensible(),
                ubicacionSegura,
                hallazgo.getDepartamento(),
                hallazgo.getFechaObservacion(),
                atributosSeguros
        );
    }

    public static List<AtributoDinamico> filtrarAtributosPublicos(List<AtributoDinamico> atributos) {
        if (atributos == null || atributos.isEmpty()) {
            return List.of();
        }
        return atributos.stream()
                .filter(a -> a != null && !esAtributoDeUbicacion(a.nombre()))
                .toList();
    }

    public static boolean esAtributoDeUbicacion(String nombreAtributo) {
        return nombreAtributo != null && PATRON_ATRIBUTO_UBICACION.matcher(nombreAtributo).matches();
    }
}
