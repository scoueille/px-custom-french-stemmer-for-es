package org.elasticsearch.plugin.analysis.customfrenchstemmer;

import java.util.Map;
import static java.util.Collections.singletonMap;

import org.elasticsearch.index.analysis.TokenFilterFactory;
import org.elasticsearch.index.analysis.customfrenchstemmer.CustomFrenchStemTokenFilterFactory;
import org.elasticsearch.indices.analysis.AnalysisModule.AnalysisProvider;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

public class CustomFrenchStemTokenPlugin extends Plugin implements AnalysisPlugin {
    @Override
    public Map<String, AnalysisProvider<TokenFilterFactory>> getTokenFilters() {
        return singletonMap("custom_french_stemmer", CustomFrenchStemTokenFilterFactory::new);
    }
}
