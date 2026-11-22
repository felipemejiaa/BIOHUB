package co.edu.unbosque.biohub.auditoria;

import co.edu.unbosque.biohub.hallazgos.dominio.Especie;

import java.time.LocalDateTime;

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