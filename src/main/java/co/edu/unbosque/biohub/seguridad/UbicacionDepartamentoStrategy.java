package co.edu.unbosque.biohub.seguridad;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;

public class UbicacionDepartamentoStrategy implements EstrategiaUbicacion {

    @Override
    public boolean revelaCoordenadas() {
        return false;
    }

    @Override
    public String obtenerUbicacion(Hallazgo hallazgo) {
        return hallazgo.getDepartamento();
    }
}