package co.edu.unbosque.biohub.auditoria;

import co.edu.unbosque.biohub.hallazgos.dominio.Especie;
import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

import java.time.LocalDateTime;
import java.util.Objects;

public class HallazgoOriginator {

    private Hallazgo hallazgoActual;
    private int versionActual;

    public HallazgoOriginator(Hallazgo hallazgoInicial) {
        this.hallazgoActual = Objects.requireNonNull(hallazgoInicial, "El hallazgo inicial no puede ser nulo");
        this.versionActual = 1;
    }

    public HallazgoMemento crearSnapshotInicial(LocalDateTime fechaRegistro) {
        return new HallazgoMemento(
                versionActual,
                hallazgoActual,
                fechaRegistro,
                hallazgoActual.getInvestigadorId(),
                RolEnum.INVESTIGADOR_CAMPO,
                "Registro inicial de campo"
        );
    }

    public HallazgoMemento reclasificarEspecie(Especie nuevaEspecie, String autorizadoPor,
                                               RolEnum rolAutorizador, String motivo,
                                               LocalDateTime fechaReclasificacion) {
        validarPermisoReclasificacion(rolAutorizador);
        Objects.requireNonNull(nuevaEspecie, "La nueva especie no puede ser nula");
        if (motivo == null || motivo.isBlank()) {
            throw new IllegalArgumentException("Debe especificar el motivo científico de la reclasificación");
        }
        if (autorizadoPor == null || autorizadoPor.isBlank()) {
            throw new IllegalArgumentException("Debe especificar quién autoriza la reclasificación");
        }

        this.hallazgoActual = new Hallazgo(
                hallazgoActual.getId(),
                nuevaEspecie,
                hallazgoActual.getLatitud(),
                hallazgoActual.getLongitud(),
                hallazgoActual.getDepartamento(),
                hallazgoActual.getFechaObservacion(),
                hallazgoActual.getInvestigadorId(),
                hallazgoActual.getAtributos()
        );
        this.versionActual++;

        return new HallazgoMemento(
                versionActual,
                hallazgoActual,
                fechaReclasificacion,
                autorizadoPor,
                rolAutorizador,
                motivo
        );
    }

    public void restaurarDesde(HallazgoMemento memento) {
        Objects.requireNonNull(memento, "El memento a restaurar no puede ser nulo");
        this.hallazgoActual = memento.getEstadoHallazgo();
        this.versionActual = memento.getVersion();
    }

    private void validarPermisoReclasificacion(RolEnum rol) {
        if (rol != RolEnum.DIRECTOR_CIENTIFICO && rol != RolEnum.INVESTIGADOR_CAMPO) {
            throw new SecurityException("El rol " + rol + " no está autorizado para reclasificar ejemplares");
        }
    }

    public Hallazgo getHallazgoActual() { return hallazgoActual; }
    public int getVersionActual() { return versionActual; }
}