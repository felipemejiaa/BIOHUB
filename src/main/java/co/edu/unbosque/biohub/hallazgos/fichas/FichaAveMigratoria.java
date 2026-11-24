package co.edu.unbosque.biohub.hallazgos.fichas;

import java.util.List;

public class FichaAveMigratoria implements FichaTaxonomica {

    @Override
    public String getGrupo() {
        return "AVE_MIGRATORIA";
    }

    @Override
    public List<String> getAtributosRequeridos() {
        return CatalogoAtributosTaxonomicos.obtenerNombresAtributos(getGrupo());
    }
}