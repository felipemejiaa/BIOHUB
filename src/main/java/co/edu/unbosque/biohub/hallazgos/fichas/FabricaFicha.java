package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;

import java.util.List;

public abstract class FabricaFicha {

    protected abstract FichaTaxonomica crearFicha();

    public FichaTaxonomica registrar(List<AtributoDinamico> atributos) {
        FichaTaxonomica ficha = crearFicha();              // 1. pide la ficha

        List<String> errores = ficha.validar(atributos);   // 2. valida los atributos
        if (!errores.isEmpty()) {
            throw new IllegalArgumentException(
                    "Ficha inválida: " + String.join(", ", errores));
        }

        return ficha;                                       // 3. devuelve la ficha válida
    }
}