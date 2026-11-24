package co.edu.unbosque.biohub.consulta;

public record ResultadoConsulta<T>(T datos, EstadoCache estadoCache) {
}
