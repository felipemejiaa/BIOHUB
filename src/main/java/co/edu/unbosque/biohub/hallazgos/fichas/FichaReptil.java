package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;

import java.util.ArrayList;
import java.util.List;

public class FichaReptil implements FichaTaxonomica {

    @Override
    public String getGrupo() {
        return "REPTIL";
    }

    @Override
    public List<String> getAtributosRequeridos() {
        return List.of("longitudCorporal", "fotoEscamas");
    }

    @Override
    public List<String> validar(List<AtributoDinamico> atributos) {
        List<String> errores = new ArrayList<>();
        for (String requerido : getAtributosRequeridos()) {
            boolean encontrado = atributos.stream()
                    .anyMatch(a -> a.nombre().equals(requerido));
            if (!encontrado) {
                errores.add("Falta el atributo obligatorio: " + requerido);
            }
        }
        return errores;
    }
}