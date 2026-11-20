package co.edu.unbosque.biohub.hallazgos.fichas;

public class FabricaPlanta extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaPlanta();
    }
}