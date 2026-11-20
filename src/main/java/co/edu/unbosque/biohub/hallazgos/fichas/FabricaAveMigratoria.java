package co.edu.unbosque.biohub.hallazgos.fichas;

public class FabricaAveMigratoria extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaAveMigratoria();
    }
}