package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.ServicioHallazgos;
import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricaFichaTest {

    @BeforeEach
    @AfterEach
    void restaurarCatalogoPorDefecto() {
        CatalogoAtributosTaxonomicos.restablecerPorDefecto();
    }

    @Test
    void fabricaAnfibioDebeCrearFichaDeAnfibio() {
        FabricaFicha fabrica = new FabricaAnfibio();
        List<AtributoDinamico> atributos = List.of(
                new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                new AtributoDinamico("grabacionAcustica", TipoDato.TEXTO, "canto_rana.mp3")
        );

        FichaTaxonomica ficha = fabrica.registrar(atributos);

        assertEquals("ANFIBIO", ficha.getGrupo());
    }

    @Test
    void fabricaAveMigratoriaDebeCrearFichaDeAve() {
        FabricaFicha fabrica = new FabricaAveMigratoria();
        List<AtributoDinamico> atributos = List.of(
                new AtributoDinamico("rutaGps", TipoDato.TEXTO, "ruta_norte.gpx"),
                new AtributoDinamico("muestraAdn", TipoDato.TEXTO, "ADN-4521")
        );

        FichaTaxonomica ficha = fabrica.registrar(atributos);

        assertEquals("AVE_MIGRATORIA", ficha.getGrupo());
    }

    @Test
    void fabricaReptilDebeCrearFichaDeReptil() {
        FabricaFicha fabrica = new FabricaReptil();
        List<AtributoDinamico> atributos = List.of(
                new AtributoDinamico("longitudCorporal", TipoDato.DECIMAL, "1.2"),
                new AtributoDinamico("fotoEscamas", TipoDato.TEXTO, "escamas_01.jpg")
        );

        FichaTaxonomica ficha = fabrica.registrar(atributos);

        assertEquals("REPTIL", ficha.getGrupo());
    }

    @Test
    void fabricaPlantaDebeCrearFichaPlanta() {
        FabricaFicha fabrica = new FabricaPlanta();
        List<AtributoDinamico> atributos = List.of(
                new AtributoDinamico("alturaTallo", TipoDato.DECIMAL, "3.5"),
                new AtributoDinamico("muestraHerbario", TipoDato.TEXTO, "HERB-7890")
        );

        FichaTaxonomica ficha = fabrica.registrar(atributos);

        assertEquals("PLANTA", ficha.getGrupo());
    }

    @Test
    void debeFallarSiFaltaAtributoObligatorio() {
        FabricaFicha fabrica = new FabricaAnfibio();
        List<AtributoDinamico> atributosIncompletos = List.of(
                new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5")
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> fabrica.registrar(atributosIncompletos)
        );

        assertTrue(error.getMessage().contains("grabacionAcustica"));
    }

    @Test
    void debeFallarSiNoHayNingunAtributo() {
        FabricaFicha fabrica = new FabricaReptil();
        List<AtributoDinamico> vacio = List.of();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> fabrica.registrar(vacio)
        );

        assertTrue(error.getMessage().contains("longitudCorporal"));
    }

    @Test
    void debeFallarSiAtributoObligatorioVieneEnBlanco() {
        FabricaFicha fabrica = new FabricaAnfibio();
        List<AtributoDinamico> atributosConBlanco = List.of(
                new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                new AtributoDinamico("grabacionAcustica", TipoDato.TEXTO, "   ")
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> fabrica.registrar(atributosConBlanco)
        );

        assertTrue(error.getMessage().contains("grabacionAcustica"));
    }

    @Test
    void debeFallarSiListaDeAtributosEsNula() {
        FabricaFicha fabrica = new FabricaAveMigratoria();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> fabrica.registrar(null)
        );

        assertTrue(error.getMessage().contains("rutaGps"));
        assertTrue(error.getMessage().contains("muestraAdn"));
    }

    @Test
    void debeExigirAtributoNuevoAgregadoDinamicamenteSinModificarCodigo() {
        CatalogoAtributosTaxonomicos.agregarAtributo("ANFIBIO", "phAgua", TipoDato.DECIMAL);

        FabricaFicha fabrica = new FabricaAnfibio();
        Map<String, TipoDato> definicion = fabrica.obtenerDefinicionAtributos();
        assertEquals(TipoDato.DECIMAL, definicion.get("phAgua"));

        List<AtributoDinamico> sinAtributoNuevo = List.of(
                new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                new AtributoDinamico("grabacionAcustica", TipoDato.TEXTO, "canto_rana.mp3")
        );

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> fabrica.registrar(sinAtributoNuevo)
        );
        assertTrue(error.getMessage().contains("phAgua"));

        List<AtributoDinamico> completos = List.of(
                new AtributoDinamico("temperaturaAgua", TipoDato.DECIMAL, "18.5"),
                new AtributoDinamico("grabacionAcustica", TipoDato.TEXTO, "canto_rana.mp3"),
                new AtributoDinamico("phAgua", TipoDato.DECIMAL, "6.8")
        );
        FichaTaxonomica ficha = fabrica.registrar(completos);
        assertEquals("ANFIBIO", ficha.getGrupo());
    }

    @Test
    void servicioHallazgosDebeAgregarAtributoYValidarMedianteFactoryMethod() {
        ServicioHallazgos servicio = new ServicioHallazgos();
        Map<String, TipoDato> actualizados = servicio.agregarAtributoAGrupo(
                "AVE_MIGRATORIA",
                "altitudVuelo",
                TipoDato.ENTERO
        );
        assertEquals(TipoDato.ENTERO, actualizados.get("altitudVuelo"));

        List<AtributoDinamico> incompletos = List.of(
                new AtributoDinamico("rutaGps", TipoDato.TEXTO, "ruta_norte.gpx"),
                new AtributoDinamico("muestraAdn", TipoDato.TEXTO, "ADN-4521")
        );
        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.validarYCrearFicha("AVE_MIGRATORIA", incompletos)
        );
        assertTrue(error.getMessage().contains("altitudVuelo"));

        List<AtributoDinamico> completos = List.of(
                new AtributoDinamico("rutaGps", TipoDato.TEXTO, "ruta_norte.gpx"),
                new AtributoDinamico("muestraAdn", TipoDato.TEXTO, "ADN-4521"),
                new AtributoDinamico("altitudVuelo", TipoDato.ENTERO, "2400")
        );
        FichaTaxonomica ficha = servicio.validarYCrearFicha("AVE_MIGRATORIA", completos);
        assertEquals("AVE_MIGRATORIA", ficha.getGrupo());
    }

    @Test
    void servicioHallazgosDebeRechazarGrupoNoSoportado() {
        ServicioHallazgos servicio = new ServicioHallazgos();

        IllegalArgumentException error = assertThrows(
                IllegalArgumentException.class,
                () -> servicio.obtenerFabrica("MAMIFERO")
        );

        assertTrue(error.getMessage().contains("Grupo taxonómico no soportado"));
    }

    @Test
    void servicioHallazgosDebeAceptarNuevaFabricaInyectadaSinModificarCodigo() {
        FabricaFicha fabricaMamifero = new FabricaFicha() {
            @Override
            protected FichaTaxonomica crearFicha() {
                return new FichaTaxonomica() {
                    @Override
                    public String getGrupo() {
                        return "MAMIFERO";
                    }

                    @Override
                    public List<String> getAtributosRequeridos() {
                        return List.of("tipoPelaje");
                    }

                    @Override
                    public Map<String, TipoDato> getDefinicionAtributos() {
                        return Map.of("tipoPelaje", TipoDato.TEXTO);
                    }
                };
            }
        };

        ServicioHallazgos servicioExtendido = new ServicioHallazgos(List.of(fabricaMamifero), Optional.empty());

        FichaTaxonomica ficha = servicioExtendido.validarYCrearFicha(
                "MAMIFERO",
                List.of(new AtributoDinamico("tipoPelaje", TipoDato.TEXTO, "denso"))
        );
        assertEquals("MAMIFERO", ficha.getGrupo());
    }

    @Test
    void servicioHallazgosDebePersistirAtributoMedianteInterfazRepositorio() {
        Map<String, Map<String, TipoDato>> guardado = new HashMap<>();
        RepositorioAtributosGrupo stubRepo = new RepositorioAtributosGrupo() {
            @Override
            public Map<String, Map<String, TipoDato>> listarAtributosPorGrupo() {
                return guardado;
            }

            @Override
            public void guardarAtributosGrupo(String grupoTaxonomico, Map<String, TipoDato> atributos) {
                guardado.put(grupoTaxonomico, atributos);
            }
        };

        ServicioHallazgos servicio = new ServicioHallazgos(List.of(new FabricaAnfibio()), Optional.of(stubRepo));
        servicio.agregarAtributoAGrupo("ANFIBIO", "phAgua", TipoDato.DECIMAL);

        assertTrue(guardado.containsKey("ANFIBIO"));
        assertEquals(TipoDato.DECIMAL, guardado.get("ANFIBIO").get("phAgua"));
    }
}