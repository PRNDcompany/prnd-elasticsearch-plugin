package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.env.Environment;
import org.elasticsearch.index.IndexSettings;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordTokenizerFactory extends AbstractTokenizerFactory {
    private static final String DICTIONARY_RULES = "dictionary_rules";
    private static Dictionary dict;

    public WordTokenizerFactory(IndexSettings indexSettings, Environment env, String ignored, Settings settings) {
        super(indexSettings, settings);
        List<String> rules = Analysis.getWordList(env, settings, DICTIONARY_RULES);
        if (rules == null) {
            return;
        }
        Map<String, Double> map = new HashMap<>();
        int i = 1;
        for (String rule : rules) {
            map.put(rule, Math.log((i++)*Math.log(rules.size())));
        }
        dict = new Dictionary(map);
    }

    @Override
    public Tokenizer create() {
        return new WordTokenizer(dict);
    }
}
