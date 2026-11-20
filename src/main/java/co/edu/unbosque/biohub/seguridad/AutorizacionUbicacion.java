package co.edu.unbosque.biohub.seguridad;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

import java.util.EnumMap;
import java.util.Map;

public class AutorizacionUbicacion {

    private final Map<RolEnum, EstrategiaUbicacion> estrategias;
    private final EstrategiaUbicacion porDefecto;

    public AutorizacionUbicacion(Map<RolEnum, EstrategiaUbicacion> estrategias,
                                  EstrategiaUbicacion porDefecto) {
        this.estrategias = new EnumMap<>(estrategias);
        this.porDefecto = porDefecto;
    }

    public static AutorizacionUbicacion conAsignacionPorDefecto() {
        EstrategiaUbicacion exacta = new UbicacionExactaStrategy();
        EstrategiaUbicacion departamento = new UbicacionDepartamentoStrategy();

        Map<RolEnum, EstrategiaUbicacion> mapa = new EnumMap<>(RolEnum.class);
        mapa.put(RolEnum.DIRECTOR_CIENTIFICO, exacta);
        mapa.put(RolEnum.INVESTIGADOR_CAMPO, departamento);
        mapa.put(RolEnum.AUDITOR_CIENTIFICO, departamento);
        mapa.put(RolEnum.CONSUMIDOR_EXTERNO, departamento);
        mapa.put(RolEnum.VISITANTE_PUBLICO, departamento);

        return new AutorizacionUbicacion(mapa, departamento);
    }

    public String decidir(RolEnum rol, Hallazgo hallazgo) {
        EstrategiaUbicacion estrategia = estrategias.getOrDefault(rol, porDefecto);
        return estrategia.obtenerUbicacion(hallazgo);
    }

    public boolean revelaCoordenadas(RolEnum rol) {
        EstrategiaUbicacion estrategia = estrategias.getOrDefault(rol, porDefecto);
        return estrategia.revelaCoordenadas();
    }
}