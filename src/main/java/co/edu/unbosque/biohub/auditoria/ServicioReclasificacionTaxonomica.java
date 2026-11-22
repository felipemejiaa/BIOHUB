package co.edu.unbosque.biohub.auditoria;

import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class ServicioReclasificacionTaxonomica {

    private final HistorialTaxonomico historial;
    private final Map<Long, HallazgoOriginator> originators = new HashMap<>();

    public ServicioReclasificacionTaxonomica(HistorialTaxonomico historial) {
        this.historial = historial;
    }

    public void registrarHallazgoInicial(Hallazgo hallazgo, LocalDateTime fechaRegistro) {
        HallazgoOriginator originator = new HallazgoOriginator(hallazgo);
        originators.put(hallazgo.getId(), originator);
        historial.guardar(originator.crearSnapshotInicial(fechaRegistro));
    }

    public Hallazgo reclasificar(Long hallazgoId, Especie nuevaEspecie, String autorizadoPor,
                                 RolEnum rolAutorizador, String motivo, LocalDateTime fechaReclasificacion) {
        HallazgoOriginator originator = obtenerOriginator(hallazgoId);
        HallazgoMemento snapshot = originator.reclasificarEspecie(
                nuevaEspecie,
                autorizadoPor,
                rolAutorizador,
                motivo,
                fechaReclasificacion
        );
        historial.guardar(snapshot);
        return originator.getHallazgoActual();
    }

    public Hallazgo consultarEstadoEnFecha(Long hallazgoId, LocalDateTime fecha, RolEnum rolSolicitante) {
        return historial.consultarEnFecha(hallazgoId, fecha, rolSolicitante).getEstadoHallazgo();
    }

    public ComparacionTaxonomica compararEneroVsDiciembre(Long hallazgoId,
                                                           LocalDateTime fechaAnterior,
                                                           LocalDateTime fechaPosterior,
                                                           RolEnum rolSolicitante) {
        return historial.compararEntreFechas(hallazgoId, fechaAnterior, fechaPosterior, rolSolicitante);
    }

    public List<HallazgoMemento> auditarHistorialCompleto(Long hallazgoId, RolEnum rolSolicitante) {
        return historial.obtenerLineaTiempo(hallazgoId, rolSolicitante);
    }

    private HallazgoOriginator obtenerOriginator(Long hallazgoId) {
        HallazgoOriginator originator = originators.get(hallazgoId);
        if (originator == null) {
            throw new NoSuchElementException("No existe un hallazgo activo con id " + hallazgoId);
        }
        return originator;
    }
}