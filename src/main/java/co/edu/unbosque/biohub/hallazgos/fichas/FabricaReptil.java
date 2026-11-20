package co.edu.unbosque.biohub.hallazgos.fichas;

public class FabricaReptil extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaReptil();
    }
}