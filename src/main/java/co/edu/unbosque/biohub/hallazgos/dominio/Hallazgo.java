package co.edu.unbosque.biohub.hallazgos.dominio;

import java.time.LocalDateTime;
import java.util.List;

public class Hallazgo {

    private final Long id;
    private final Especie especie;
    private final double latitud;
    private final double longitud;
    private final String departamento;
    private final LocalDateTime fechaObservacion;
    private final String investigadorId;
    private final List<AtributoDinamico> atributos;

    public Hallazgo(Long id, Especie especie, double latitud, double longitud,
                    String departamento, LocalDateTime fechaObservacion,
                    String investigadorId, List<AtributoDinamico> atributos) {
        this.id = id;
        this.especie = especie;
        this.latitud = latitud;
        this.longitud = longitud;
        this.departamento = departamento;
        this.fechaObservacion = fechaObservacion;
        this.investigadorId = investigadorId;
        this.atributos = List.copyOf(atributos);
    }

    public Long getId() { return id; }
    public Especie getEspecie() { return especie; }
    public double getLatitud() { return latitud; }
    public double getLongitud() { return longitud; }
    public String getDepartamento() { return departamento; }
    public LocalDateTime getFechaObservacion() { return fechaObservacion; }
    public String getInvestigadorId() { return investigadorId; }
    public List<AtributoDinamico> getAtributos() { return atributos; }
}