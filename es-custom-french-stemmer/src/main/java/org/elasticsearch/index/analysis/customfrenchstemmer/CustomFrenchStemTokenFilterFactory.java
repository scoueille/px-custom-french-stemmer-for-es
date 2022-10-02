package org.elasticsearch.index.analysis.customfrenchstemmer;

import org.apache.lucene.analysis.TokenStream;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;
import org.elasticsearch.index.analysis.AbstractTokenFilterFactory;
import com.proaxian.analysis.customfrenchstemmer.CustomFrenchStemTokenFilter;
import java.io.IOException;
import org.apache.lucene.analysis.miscellaneous.EmptyTokenStream;

public class CustomFrenchStemTokenFilterFactory extends AbstractTokenFilterFactory {

        private static final TokenStream EMPTY_TOKEN_STREAM = new EmptyTokenStream();

	public CustomFrenchStemTokenFilterFactory(IndexSettings indexSettings, Environment env, String name, Settings settings) throws IOException {
		super(indexSettings, name, settings);
                create(EMPTY_TOKEN_STREAM).close();
	}

	@Override
	public TokenStream create(TokenStream tokenStream) {

            return new CustomFrenchStemTokenFilter(tokenStream);
	}
}