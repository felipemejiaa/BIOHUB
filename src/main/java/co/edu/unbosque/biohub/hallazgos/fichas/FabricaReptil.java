package co.edu.unbosque.biohub.hallazgos.fichas;

import org.springframework.stereotype.Component;

@Component
public class FabricaReptil extends FabricaFicha {

    @Override
    protected FichaTaxonomica crearFicha() {
        return new FichaReptil();
    }
}