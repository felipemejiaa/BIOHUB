package co.edu.unbosque.biohub.hallazgos.dominio;

public class Especie {
    private final Long id;
    private final String nombreCientifico;
    private final String nombreComun;
    private final String estadoConservacion;
    private final boolean esSensible;

    public Especie(Long id, String nombreCientifico, String nombreComun,
                   String estadoConservacion, boolean esSensible) {
        this.id = id;
        this.nombreCientifico = nombreCientifico;
        this.nombreComun = nombreComun;
        this.estadoConservacion = estadoConservacion;
        this.esSensible = esSensible;
    }

    public Long getId() { return id; }
    public String getNombreCientifico() { return nombreCientifico; }
    public String getNombreComun() { return nombreComun; }
    public String getEstadoConservacion() { return estadoConservacion; }
    public boolean esSensible() { return esSensible; }
}