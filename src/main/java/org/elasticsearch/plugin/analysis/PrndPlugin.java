package org.elasticsearch.plugin.analysis;

import org.elasticsearch.index.analysis.TokenizerFactory;
import org.elasticsearch.index.analysis.WordTokenizerFactory;
import org.elasticsearch.indices.analysis.AnalysisModule;
import org.elasticsearch.plugins.AnalysisPlugin;
import org.elasticsearch.plugins.Plugin;

import java.util.HashMap;
import java.util.Map;

public class PrndPlugin extends Plugin implements AnalysisPlugin {

    @Override
    public Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> getTokenizers() {
        Map<String, AnalysisModule.AnalysisProvider<TokenizerFactory>> tokenizers = new HashMap<>();
        tokenizers.put("prnd-word-tokenizer", WordTokenizerFactory::new);
        return tokenizers;
    }
}
