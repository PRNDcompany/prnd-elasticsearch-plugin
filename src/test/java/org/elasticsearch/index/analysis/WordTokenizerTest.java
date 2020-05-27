package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.BaseTokenStreamTestCase;

import org.apache.lucene.util.IOUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class WordTokenizerTest extends BaseTokenStreamTestCase {
    private Analyzer testAnalyzer;

    @Override
    public void setUp() throws Exception {
        super.setUp();

        testAnalyzer = new Analyzer() {
            @Override
            protected TokenStreamComponents createComponents(String fieldName) {
                Map<String, Double> map = new HashMap<>();
                map.put("엔진", Math.log(1*Math.log(100)));
                map.put("누유", Math.log(2*Math.log(100)));
                return new TokenStreamComponents(new WordTokenizer(new Dictionary(map)));
            }
        };
    }

    @Override
    public void tearDown() throws Exception {
        IOUtils.close(testAnalyzer);
        super.tearDown();
    }

    public void testBasic() throws IOException {
        assertAnalyzesTo(testAnalyzer, "엔진누유",
            new String[]{"엔진", "누유"},
            new int[]{0, 2},
            new int[]{1, 3}
        );
    }

    public void testIncomplete() throws IOException {
        assertAnalyzesTo(testAnalyzer, "엔진눙",
            new String[]{"엔진", "눙"},
            new int[]{0, 2},
            new int[]{1, 2}
        );
    }

    public void testWhitespace() throws IOException {
        assertAnalyzesTo(testAnalyzer, "엔진누 유",
            new String[]{"엔진", "누", "유"}
        );
    }
}
