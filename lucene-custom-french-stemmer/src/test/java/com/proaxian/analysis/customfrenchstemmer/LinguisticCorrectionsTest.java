package com.proaxian.analysis.customfrenchstemmer;

import java.io.StringReader;
import java.util.List;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.LowerCaseFilter;
import org.apache.lucene.analysis.standard.StandardTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie les familles corrigées et les formes inclusives à travers la tokenisation réelle.
 */
class LinguisticCorrectionsTest {
    /**
     * Calcule le radical brut d'un seul mot pour contrôler les équivalences métier.
     */
    private String stem(String word) {
        char[] buffer = word.toCharArray();
        return new String(buffer, 0, new CustomFrenchLightStemmer().stem(buffer, buffer.length));
    }

    /**
     * Conserve les familles scientifiques tout en réparant les couples nom/infinitif.
     */
    @Test
    void repairsLogueAndDocteure() {
        for (String[] family : new String[][] {
            {"géologue", "géologie"}, {"psychologue", "psychologie"},
            {"biologue", "biologie"}, {"dialogue", "dialoguer"},
            {"catalogue", "cataloguer"}, {"épilogue", "épiloguer"},
            {"docteur", "docteure", "docteurs", "docteures"}
        }) {
            for (String word : family) assertEquals(stem(family[0]), stem(word), word);
        }
    }

    /**
     * Teste les points usuels, les féminins abrégés et les deux écritures du pluriel.
     */
    @Test
    void recognizesInclusiveSuffixesAfterStandardTokenization() throws Exception {
        for (String[] family : new String[][] {
            {"ingénieur", "ingénieur·e", "ingénieur·es", "ingénieur·e·s", "ingénieur.e.s"},
            {"développeur", "développeur·se", "développeur·ses", "développeur·se·s",
                "développeur·euse", "développeur·euses", "développeur·euse·s"},
            {"directeur", "directeur·rice", "directeur·rices", "directeur·rice·s",
                "directeur·trice", "directeur·trices"},
            {"acteur", "acteur·rice", "acteur·rices", "acteur·rice·s"},
            {"technicien", "technicien·ne", "technicien·nes", "technicien·ne·s"},
            {"professionnel", "professionnel·le", "professionnel·les", "professionnel·le·s"},
            {"conseiller", "conseiller·ère", "conseiller·ères", "conseiller·ère·s", "conseiller.ere.s"},
            {"sportif", "sportif·ve", "sportif·ves", "sportif·ve·s"},
            {"heureux", "heureux·se", "heureux·ses", "heureux·se·s"},
            {"docteur", "docteur·e", "docteur·e·s"}
        }) {
            for (int i = 1; i < family.length; i++) {
                String expected = stem(family[0]);
                assertEquals(expected, stem(family[i]), family[i]);
                var tokenizer = new StandardTokenizer();
                tokenizer.setReader(new StringReader(family[i]));
                try (TokenStream stream = new CustomFrenchStemTokenFilter(new LowerCaseFilter(tokenizer))) {
                    var term = stream.addAttribute(CharTermAttribute.class);
                    stream.reset();
                    assertTrue(stream.incrementToken(), family[i]);
                    assertEquals(expected, term.toString(), family[i]);
                    assertFalse(stream.incrementToken(), family[i]);
                    stream.end();
                }
            }
        }
        for (String dot : List.of(".", "·", "‧", "․", "﹒")) {
            assertEquals(stem("développeur"), stem("développeur" + dot + "se" + dot + "s"));
        }
    }

    /**
     * Évite d'étendre la reconnaissance inclusive à des suffixes incompatibles.
     */
    @Test
    void preservesTechnicalWordsAndRejectsUnrelatedSuffixes() {
        assertEquals("nodej", stem("node.js"));
        assertEquals("php", stem("p.h.p."));
        assertNotEquals(stem("acteur"), stem("acteur·ne"));
        assertNotEquals(stem("technicien"), stem("technicien·rice"));
        assertNotEquals(stem("développeur"), stem("développeur·xyz"));
        // Ces règles de suffixes n'ont pas été modifiées par cette évolution.
        assertEquals("organ", stem("organisme"));
        assertEquals("compt", stem("comptable"));
    }
}
