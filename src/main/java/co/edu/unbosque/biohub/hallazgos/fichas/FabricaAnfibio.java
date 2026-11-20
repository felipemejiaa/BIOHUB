package co.edu.unbosque.biohub.hallazgos.fichas;

public class FabricaAnfibio extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaAnfibio();
    }
}