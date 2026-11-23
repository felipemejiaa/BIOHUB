package co.edu.unbosque.biohub.hallazgos.fichas;

import java.util.List;

public class FichaAnfibio implements FichaTaxonomica {

    @Override
    public String getGrupo() {
        return "ANFIBIO";
    }

    @Override
    public List<String> getAtributosRequeridos() {
        return CatalogoAtributosTaxonomicos.obtenerNombresAtributos(getGrupo());
    }
}