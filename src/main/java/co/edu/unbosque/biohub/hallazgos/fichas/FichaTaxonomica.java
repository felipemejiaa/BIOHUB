package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public interface FichaTaxonomica {
    String getGrupo();

    List<String> getAtributosRequeridos();

    default Map<String, TipoDato> getDefinicionAtributos() {
        return CatalogoAtributosTaxonomicos.obtenerAtributos(getGrupo());
    }

    default List<String> validar(List<AtributoDinamico> atributos) {
        List<AtributoDinamico> lista = atributos == null ? List.of() : atributos;
        List<String> errores = new ArrayList<>();
        for (String requerido : getAtributosRequeridos()) {
            boolean encontrado = lista.stream()
                    .anyMatch(a -> a != null
                            && requerido.equals(a.nombre())
                            && a.valor() != null
                            && !a.valor().isBlank());
            if (!encontrado) {
                errores.add("Falta el atributo obligatorio: " + requerido);
            }
        }
        return errores;
    }
}