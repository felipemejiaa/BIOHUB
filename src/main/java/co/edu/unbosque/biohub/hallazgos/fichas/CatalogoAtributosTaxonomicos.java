package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Catálogo en memoria de atributos requeridos por grupo taxonómico.
 * Permite extender atributos en tiempo de ejecución (Defer Binding / ASR-05)
 * sin modificar el código de las clases de ficha.
 */
public final class CatalogoAtributosTaxonomicos {

    private static final Map<String, Map<String, TipoDato>> CATALOGO = new ConcurrentHashMap<>();

    static {
        restablecerPorDefecto();
    }

    private CatalogoAtributosTaxonomicos() {
    }

    public static Map<String, TipoDato> obtenerAtributos(String grupo) {
        if (grupo == null || grupo.isBlank()) {
            return Map.of();
        }
        Map<String, TipoDato> atributosGrupo = CATALOGO.get(normalizarGrupo(grupo));
        if (atributosGrupo == null) {
            return Map.of();
        }
        synchronized (atributosGrupo) {
            return Collections.unmodifiableMap(new LinkedHashMap<>(atributosGrupo));
        }
    }

    public static List<String> obtenerNombresAtributos(String grupo) {
        return List.copyOf(obtenerAtributos(grupo).keySet());
    }

    public static void agregarAtributo(String grupo, String nombreAtributo, TipoDato tipoDato) {
        String grupoNormalizado = validarGrupo(grupo);
        if (nombreAtributo == null || nombreAtributo.isBlank()) {
            throw new IllegalArgumentException("El nombre del atributo es obligatorio");
        }
        if (tipoDato == null) {
            throw new IllegalArgumentException("El tipo de dato del atributo es obligatorio");
        }
        Map<String, TipoDato> atributosGrupo = CATALOGO.computeIfAbsent(
                grupoNormalizado,
                k -> Collections.synchronizedMap(new LinkedHashMap<>())
        );
        atributosGrupo.put(nombreAtributo.trim(), tipoDato);
    }

    public static void reemplazarAtributos(String grupo, Map<String, TipoDato> atributos) {
        String grupoNormalizado = validarGrupo(grupo);
        if (atributos == null || atributos.isEmpty()) {
            return;
        }
        Map<String, TipoDato> copia = Collections.synchronizedMap(new LinkedHashMap<>());
        atributos.forEach((nombre, tipo) -> {
            if (nombre != null && !nombre.isBlank() && tipo != null) {
                copia.put(nombre.trim(), tipo);
            }
        });
        if (!copia.isEmpty()) {
            CATALOGO.put(grupoNormalizado, copia);
        }
    }

    public static Set<String> obtenerGrupos() {
        return Set.copyOf(CATALOGO.keySet());
    }

    public static synchronized void restablecerPorDefecto() {
        CATALOGO.clear();
        reemplazarAtributos("ANFIBIO", mapOfOrdered(
                "temperaturaAgua", TipoDato.DECIMAL,
                "grabacionAcustica", TipoDato.TEXTO
        ));
        reemplazarAtributos("AVE_MIGRATORIA", mapOfOrdered(
                "rutaGps", TipoDato.TEXTO,
                "muestraAdn", TipoDato.TEXTO
        ));
        reemplazarAtributos("PLANTA", mapOfOrdered(
                "alturaTallo", TipoDato.DECIMAL,
                "muestraHerbario", TipoDato.TEXTO
        ));
        reemplazarAtributos("REPTIL", mapOfOrdered(
                "longitudCorporal", TipoDato.DECIMAL,
                "fotoEscamas", TipoDato.TEXTO
        ));
    }

    private static Map<String, TipoDato> mapOfOrdered(String k1, TipoDato v1, String k2, TipoDato v2) {
        Map<String, TipoDato> mapa = new LinkedHashMap<>();
        mapa.put(k1, v1);
        mapa.put(k2, v2);
        return mapa;
    }

    private static String validarGrupo(String grupo) {
        if (grupo == null || grupo.isBlank()) {
            throw new IllegalArgumentException("El grupo taxonómico es obligatorio");
        }
        return normalizarGrupo(grupo);
    }

    private static String normalizarGrupo(String grupo) {
        return grupo.trim().toUpperCase();
    }
}
