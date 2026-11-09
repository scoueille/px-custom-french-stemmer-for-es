package com.proaxian.elasticsearch.analysis;

import com.proaxian.analysis.customfrenchstemmer.CustomFrenchStemTokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.plugin.NamedComponent;
import org.elasticsearch.plugin.analysis.TokenFilterFactory;

/**
 * Expose le filtre existant par l'API stable, sous son nom historique.
 */
@NamedComponent("custom_french_stemmer")
public final class CustomFrenchStemTokenFilterFactory implements TokenFilterFactory {
    /**
     * Crée un filtre indépendant pour chaque flux, sans modifier le stemming.
     */
    @Override
    public TokenStream create(TokenStream tokenStream) {
        return new CustomFrenchStemTokenFilter(tokenStream);
    }
}
