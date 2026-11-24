package co.edu.unbosque.biohub.consulta;

import java.util.List;
import java.util.Optional;

/**
 * Contrato segregado de caché para fichas públicas enmascaradas (ISP + DIP).
 * Permite conectar Redis en producción o un stub en memoria para pruebas unitarias.
 */
public interface CacheFichasPublicas {

    Optional<List<FichaPublicaEnmascarada>> obtenerCatalogo();

    void guardarCatalogo(List<FichaPublicaEnmascarada> catalogo);

    Optional<FichaPublicaEnmascarada> obtenerFicha(Long hallazgoId);

    void guardarFicha(FichaPublicaEnmascarada ficha);

    void invalidarTodo();

    void invalidarFicha(Long hallazgoId);
}
