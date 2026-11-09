package com.proaxian.analysis.customfrenchstemmer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.miscellaneous.SetKeywordMarkerFilter;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Fige les sorties historiques et le respect des mots protégés pendant la migration.
 */
class CustomFrenchStemmerTest {
    /**
     * Compare 1372 termes historiques, avec une liste explicite des corrections autorisées.
     */
    @Test
    void preservesHistoricalStems() throws Exception {
        Map<String, String> corrections = new HashMap<>();
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/stemmer-corrections.tsv"), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                corrections.put(fields[0], fields[1]);
            }
        }
        // Les nouvelles formes ont elles aussi un résultat explicite, indépendant du code.
        for (var correction : corrections.entrySet()) {
            char[] word = correction.getKey().toCharArray();
            int length = new CustomFrenchLightStemmer().stem(word, word.length);
            assertEquals(correction.getValue(), new String(word, 0, length), correction.getKey());
        }
        int count = 0;
        try (var reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream("/stemmer-regression.tsv"), StandardCharsets.UTF_8))) {
            for (String line; (line = reader.readLine()) != null;) {
                if (line.startsWith("#")) continue;
                String[] fields = line.split("\t", -1);
                char[] word = fields[0].toCharArray();
                int length = new CustomFrenchLightStemmer().stem(word, word.length);
                assertEquals(corrections.getOrDefault(fields[0], fields[1]), new String(word, 0, length), fields[0]);
                count++;
            }
        }
        assertEquals(1372, count);
    }

    /**
     * Vérifie le filtre réel et la conservation du KeywordAttribute des mots exclus.
     */
    @Test
    void respectsProtectedKeywordsAndEmptyTokens() throws Exception {
        for (boolean protectedWord : List.of(false, true)) {
            var tokenizer = new KeywordTokenizer();
            tokenizer.setReader(new java.io.StringReader("ingénieure"));
            TokenStream input = protectedWord
                ? new SetKeywordMarkerFilter(tokenizer, new CharArraySet(List.of("ingénieure"), false))
                : tokenizer;
            try (var filter = new CustomFrenchStemTokenFilter(input)) {
                var term = filter.addAttribute(CharTermAttribute.class);
                filter.reset();
                assertTrue(filter.incrementToken());
                assertEquals(protectedWord ? "ingénieure" : "ingenieu", term.toString());
                assertFalse(filter.incrementToken());
                filter.end();
            }
        }
        assertEquals(0, new CustomFrenchLightStemmer().stem(new char[0], 0));
    }
}
