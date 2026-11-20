package co.edu.unbosque.biohub.seguridad;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;

public interface EstrategiaUbicacion {

    boolean revelaCoordenadas();

    String obtenerUbicacion(Hallazgo hallazgo);
}