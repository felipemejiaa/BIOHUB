package co.edu.unbosque.biohub.hallazgos.fichas;

import co.edu.unbosque.biohub.hallazgos.dominio.AtributoDinamico;
import co.edu.unbosque.biohub.hallazgos.dominio.TipoDato;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FabricaFichaTest {

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
}