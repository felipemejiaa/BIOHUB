package co.edu.unbosque.biohub.hallazgos.fichas;

import org.springframework.stereotype.Component;

@Component
public class FabricaAnfibio extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaAnfibio();
    }
}