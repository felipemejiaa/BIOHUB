package co.edu.unbosque.biohub.seguridad;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;

public class UbicacionExactaStrategy implements EstrategiaUbicacion {

    @Override
    public boolean revelaCoordenadas() {
        return true;
    }

    @Override
    public String obtenerUbicacion(Hallazgo hallazgo) {
        return hallazgo.getLatitud() + ", " + hallazgo.getLongitud()
                + " — " + hallazgo.getDepartamento();
    }
}