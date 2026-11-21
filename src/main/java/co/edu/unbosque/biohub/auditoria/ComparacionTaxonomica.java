package co.edu.unbosque.biohub.auditoria;

import java.time.LocalDateTime;

import co.edu.unbosque.biohub.hallazgos.dominio.Especie;

public record ComparacionTaxonomica(
        Long hallazgoId,
        int versionAnterior,
        LocalDateTime fechaAnterior,
        Especie especieAnterior,
        int versionPosterior,
        LocalDateTime fechaPosterior,
        Especie especiePosterior,
        String autorizadoPorPosterior,
        String motivoCambio,
        boolean huboCambioDeEspecie
) {
}