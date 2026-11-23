package co.edu.unbosque.biohub.hallazgos;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import co.edu.unbosque.biohub.hallazgos.fichas.CatalogoAtributosTaxonomicos;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaAnfibio;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaAveMigratoria;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaFicha;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaPlanta;
import co.edu.unbosque.biohub.hallazgos.fichas.FabricaReptil;
import co.edu.unbosque.biohub.hallazgos.fichas.FichaTaxonomica;
import co.edu.unbosque.biohub.hallazgos.fichas.RepositorioAtributosGrupo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Servicio de aplicación del módulo Hallazgos que orquesta la creación y validación
 * de fichas mediante el patrón Factory Method ({@link FabricaFicha}).
 */
@Service
public class ServicioHallazgos {

    private final Map<String, FabricaFicha> fabricasPorGrupo = new LinkedHashMap<>();
    private final Optional<RepositorioAtributosGrupo> repositorioAtributos;

    public ServicioHallazgos() {
        this(
                List.of(
                        new FabricaAnfibio(),
                        new FabricaAveMigratoria(),
                        new FabricaPlanta(),
                        new FabricaReptil()
                ),
                Optional.empty()
        );
    }

    @Autowired
    public ServicioHallazgos(List<FabricaFicha> fabricas,
                             Optional<RepositorioAtributosGrupo> repositorioAtributos) {
        this.repositorioAtributos = repositorioAtributos == null ? Optional.empty() : repositorioAtributos;
        if (fabricas != null) {
            fabricas.forEach(this::registrarFabrica);
        }
    }

    private void registrarFabrica(FabricaFicha fabrica) {
        fabricasPorGrupo.put(fabrica.obtenerGrupo(), fabrica);
    }

    public FabricaFicha obtenerFabrica(String grupo) {
        if (grupo == null || grupo.isBlank()) {
            throw new IllegalArgumentException("El grupo taxonómico es obligatorio");
        }
        FabricaFicha fabrica = fabricasPorGrupo.get(grupo.trim().toUpperCase());
        if (fabrica == null) {
            throw new IllegalArgumentException("Grupo taxonómico no soportado: " + grupo);
        }
        return fabrica;
    }

    public List<String> listarGruposSoportados() {
        return List.copyOf(fabricasPorGrupo.keySet());
    }

    public Map<String, TipoDato> obtenerAtributosRequeridos(String grupo) {
        return obtenerFabrica(grupo).obtenerDefinicionAtributos();
    }

    public FichaTaxonomica validarYCrearFicha(String grupo, List<AtributoDinamico> atributos) {
        return obtenerFabrica(grupo).registrar(atributos);
    }

    public Map<String, TipoDato> agregarAtributoAGrupo(String grupo, String nombreAtributo, TipoDato tipoDato) {
        FabricaFicha fabrica = obtenerFabrica(grupo);
        String grupoNormalizado = fabrica.obtenerGrupo();
        CatalogoAtributosTaxonomicos.agregarAtributo(grupoNormalizado, nombreAtributo, tipoDato);
        Map<String, TipoDato> actualizados = fabrica.obtenerDefinicionAtributos();
        repositorioAtributos.ifPresent(repo -> repo.guardarAtributosGrupo(grupoNormalizado, actualizados));
        return actualizados;
    }
}
