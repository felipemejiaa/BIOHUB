package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;

import java.util.List;

public interface FichaTaxonomica {
    String getGrupo();
    List<String> getAtributosRequeridos();
    List<String> validar(List<AtributoDinamico> atributos);
}