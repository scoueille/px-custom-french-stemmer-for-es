package com.proaxian.elasticsearch.analysis;

import java.nio.charset.StandardCharsets;
import org.apache.lucene.analysis.core.KeywordTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.elasticsearch.plugin.NamedComponent;
import org.elasticsearch.plugin.analysis.TokenFilterFactory;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Vérifie l'enregistrement stable, les métadonnées et la création du filtre.
 */
class StableFactoryTest {
    /**
     * Garantit que le nom public et le flux produit restent utilisables.
     */
    @Test
    void registersHistoricalNameAndCreatesFilter() throws Exception {
        assertEquals("custom_french_stemmer",
            CustomFrenchStemTokenFilterFactory.class.getAnnotation(NamedComponent.class).value());
        TokenFilterFactory factory = new CustomFrenchStemTokenFilterFactory();
        var tokenizer = new KeywordTokenizer();
        tokenizer.setReader(new java.io.StringReader("ingénieure"));
        try (var filter = factory.create(tokenizer)) {
            var term = filter.addAttribute(CharTermAttribute.class);
            filter.reset();
            assertTrue(filter.incrementToken());
            assertEquals("ingenieu", term.toString());
            assertFalse(filter.incrementToken());
            filter.end();
        }
    }

    /**
     * Empêche le retour accidentel au descripteur classique et à ses dépendances.
     */
    @Test
    void packagesStableMetadata() throws Exception {
        var loader = getClass().getClassLoader();
        assertNull(loader.getResource("plugin-descriptor.properties"));
        var properties = new java.util.Properties();
        try (var stream = loader.getResourceAsStream("stable-plugin-descriptor.properties")) {
            assertNotNull(stream);
            properties.load(stream);
        }
        assertEquals("px-custom-french-stemmer", properties.getProperty("name"));
        assertEquals("9.0.0", properties.getProperty("elasticsearch.version"));
        assertNull(properties.getProperty("classname"));
        try (var stream = loader.getResourceAsStream("named_components.json")) {
            assertNotNull(stream);
            String json = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
            assertTrue(json.contains(TokenFilterFactory.class.getName()));
            assertTrue(json.contains(CustomFrenchStemTokenFilterFactory.class.getName()));
        }
    }
}
