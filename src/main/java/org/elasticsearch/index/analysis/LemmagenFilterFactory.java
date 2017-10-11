package org.elasticsearch.index.analysis;

import java.net.URI;

import java.io.File;
import java.io.FileInputStream;

import eu.hlavki.text.lemmagen.api.Lemmatizer;
import eu.hlavki.text.lemmagen.LemmatizerFactory;

import org.apache.lucene.analysis.TokenStream;

import org.elasticsearch.env.Environment;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.IndexSettings;


public class LemmagenFilterFactory extends AbstractTokenFilterFactory {

    private Lemmatizer lemmatizer;

    public LemmagenFilterFactory(IndexSettings indexSettings,
                                 Environment env,
                                 String name,
                                 Settings settings) {

        super(indexSettings, name, settings);

        String lexiconPath = settings.get("lexicon_path", null);

        if (lexiconPath != null) {
            this.lemmatizer = getLemmatizer(env.configFile().resolve(lexiconPath).toUri());
        } else {
            throw new IllegalArgumentException("lemmagen token filter requires `lexicon_path` to be specified");
        }
    }

    public Lemmatizer getLemmatizer(URI lexiconPath) {
        try {
            File lexiconFile = new File(lexiconPath);
            return LemmatizerFactory.read(new FileInputStream(lexiconFile));
        } catch (Exception e) {
            throw new IllegalArgumentException("Can't initialize lemmatizer from resource path " + lexiconPath.toString(), e);
        }
    }

    public TokenStream create(TokenStream tokenStream) {
        return new LemmagenFilter(tokenStream, lemmatizer);
    }

}
