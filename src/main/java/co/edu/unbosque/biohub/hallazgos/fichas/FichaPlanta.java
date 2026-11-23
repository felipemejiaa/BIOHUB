package co.edu.unbosque.biohub.hallazgos.fichas;

import java.util.List;

public class FichaPlanta implements FichaTaxonomica {

    @Override
    public String getGrupo() {
        return "PLANTA";
    }

    @Override
    public List<String> getAtributosRequeridos() {
        return CatalogoAtributosTaxonomicos.obtenerNombresAtributos(getGrupo());
    }
}