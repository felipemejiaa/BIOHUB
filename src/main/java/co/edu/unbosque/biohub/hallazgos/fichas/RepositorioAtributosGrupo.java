package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

import java.util.Map;

/**
 * Contrato de persistencia segregado para consultar y actualizar la configuración
 * dinámica de atributos por grupo taxonómico (ISP + DIP).
 */
public interface RepositorioAtributosGrupo {

    Map<String, Map<String, TipoDato>> listarAtributosPorGrupo();

    void guardarAtributosGrupo(String grupoTaxonomico, Map<String, TipoDato> atributos);
}
