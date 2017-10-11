package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.standard.UAX29URLEmailTokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.Index;
import org.elasticsearch.plugin.analysis.lemmagen.AnalysisLemmagenPlugin;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.ESTokenStreamTestCase;
import org.elasticsearch.cluster.metadata.IndexMetaData;
import org.elasticsearch.Version;
import org.elasticsearch.env.Environment;

import static org.elasticsearch.test.ESTestCase.createTestAnalysis;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;

public class LemmagenAnalysisTest extends ESTokenStreamTestCase {

    public void testLemmagenFilterFactoryWithPath() throws IOException {
        ESTestCase.TestAnalysis analysis = createAnalysis();

        TokenFilterFactory tokenFilter = analysis.tokenFilter.get("lemmagen_cs_path_filter");
        assertThat(tokenFilter, instanceOf(LemmagenFilterFactory.class));

        String source = "Děkuji, že jsi přišel.";
        String[] expected = {"Děkovat", "že", "být", "přijít"};

        Tokenizer tokenizer = new UAX29URLEmailTokenizer();
        tokenizer.setReader(new StringReader(source));

        assertTokenStreamContents(tokenFilter.create(tokenizer), expected);
    }

    public ESTestCase.TestAnalysis createAnalysis() throws IOException {
        InputStream lexicon = LemmagenAnalysisTest.class.getResourceAsStream("/org/elasticsearch/index/analysis/cs.lem");

        Path home = createTempDir();
        Path config = home.resolve("config");
        Files.createDirectory(config);
        Files.copy(lexicon, config.resolve("cs.lem"));

        String path = "/org/elasticsearch/index/analysis/lemmagen.json";

        Settings settings = Settings.builder().loadFromStream(path, getClass().getResourceAsStream(path))
                .put(IndexMetaData.SETTING_VERSION_CREATED, Version.CURRENT)
                .put(Environment.PATH_HOME_SETTING.getKey(), home)
                .build();

        return AnalysisTestsHelper.createTestAnalysisFromSettings(settings, new AnalysisLemmagenPlugin());
    }

}
