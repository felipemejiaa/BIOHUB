package co.edu.unbosque.biohub.hallazgos.fichas;

import java.util.List;

public class FichaReptil implements FichaTaxonomica {

    @Override
    public String getGrupo() {
        return "REPTIL";
    }

    @Override
    public List<String> getAtributosRequeridos() {
        return CatalogoAtributosTaxonomicos.obtenerNombresAtributos(getGrupo());
    }
}