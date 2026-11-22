package co.edu.unbosque.biohub.auditoria;

import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

public class HistorialTaxonomico {

    private final Map<Long, List<HallazgoMemento>> historialPorHallazgo = new HashMap<>();

    public void guardar(HallazgoMemento memento) {
        Objects.requireNonNull(memento, "El memento no puede ser nulo");
        Long hallazgoId = memento.getEstadoHallazgo().getId();
        historialPorHallazgo
                .computeIfAbsent(hallazgoId, id -> new ArrayList<>())
                .add(memento);
    }

    public List<HallazgoMemento> obtenerLineaTiempo(Long hallazgoId, RolEnum rolSolicitante) {
        validarAccesoAuditoria(rolSolicitante);
        List<HallazgoMemento> registros = historialPorHallazgo.get(hallazgoId);
        if (registros == null || registros.isEmpty()) {
            throw new NoSuchElementException("No existe historial para el hallazgo " + hallazgoId);
        }
        return List.copyOf(registros);
    }

    public HallazgoMemento consultarEnFecha(Long hallazgoId, LocalDateTime fechaConsulta, RolEnum rolSolicitante) {
        validarAccesoAuditoria(rolSolicitante);
        List<HallazgoMemento> registros = obtenerLineaTiempo(hallazgoId, rolSolicitante);

        return registros.stream()
                .filter(m -> !m.getFechaVersion().isAfter(fechaConsulta))
                .max(Comparator.comparingInt(HallazgoMemento::getVersion))
                .orElseThrow(() -> new NoSuchElementException(
                        "El hallazgo " + hallazgoId + " aún no había sido registrado para la fecha " + fechaConsulta));
    }

    public ComparacionTaxonomica compararEntreFechas(Long hallazgoId,
                                                     LocalDateTime fechaInicio,
                                                     LocalDateTime fechaFin,
                                                     RolEnum rolSolicitante) {
        HallazgoMemento mementoInicio = consultarEnFecha(hallazgoId, fechaInicio, rolSolicitante);
        HallazgoMemento mementoFin = consultarEnFecha(hallazgoId, fechaFin, rolSolicitante);

        boolean cambioEspecie = !Objects.equals(
                mementoInicio.getEstadoHallazgo().getEspecie().getId(),
                mementoFin.getEstadoHallazgo().getEspecie().getId()
        );

        return new ComparacionTaxonomica(
                hallazgoId,
                mementoInicio.getVersion(),
                mementoInicio.getFechaVersion(),
                mementoInicio.getEstadoHallazgo().getEspecie(),
                mementoFin.getVersion(),
                mementoFin.getFechaVersion(),
                mementoFin.getEstadoHallazgo().getEspecie(),
                mementoFin.getAutorizadoPor(),
                mementoFin.getMotivo(),
                cambioEspecie
        );
    }

    private void validarAccesoAuditoria(RolEnum rol) {
        if (rol != RolEnum.AUDITOR_CIENTIFICO && rol != RolEnum.DIRECTOR_CIENTIFICO) {
            throw new SecurityException("El rol " + rol + " no tiene permisos de auditoría científica");
        }
    }
}