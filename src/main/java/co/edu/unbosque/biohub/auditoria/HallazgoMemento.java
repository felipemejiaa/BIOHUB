package co.edu.unbosque.biohub.auditoria;

import co.edu.unbosque.biohub.hallazgos.dominio.Hallazgo;
import co.edu.unbosque.biohub.hallazgos.dominio.RolEnum;

import java.time.LocalDateTime;
import java.util.Objects;

public final class HallazgoMemento {

    private final int version;
    private final Hallazgo estadoHallazgo;
    private final LocalDateTime fechaVersion;
    private final String autorizadoPor;
    private final RolEnum rolAutorizador;
    private final String motivo;

    public HallazgoMemento(int version, Hallazgo estadoHallazgo, LocalDateTime fechaVersion,
                           String autorizadoPor, RolEnum rolAutorizador, String motivo) {
        this.version = version;
        this.estadoHallazgo = Objects.requireNonNull(estadoHallazgo, "El estado del hallazgo es obligatorio");
        this.fechaVersion = Objects.requireNonNull(fechaVersion, "La fecha de versión es obligatoria");
        this.autorizadoPor = Objects.requireNonNull(autorizadoPor, "El autorizador es obligatorio");
        this.rolAutorizador = Objects.requireNonNull(rolAutorizador, "El rol del autorizador es obligatorio");
        this.motivo = Objects.requireNonNull(motivo, "El motivo es obligatorio");
    }

    public int getVersion() { return version; }
    public Hallazgo getEstadoHallazgo() { return estadoHallazgo; }
    public LocalDateTime getFechaVersion() { return fechaVersion; }
    public String getAutorizadoPor() { return autorizadoPor; }
    public RolEnum getRolAutorizador() { return rolAutorizador; }
    public String getMotivo() { return motivo; }
}