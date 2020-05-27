package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class WordTokenizer extends Tokenizer {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final Dictionary dict;
    private final Matcher matcher;
    private int index = 0;

    private final StringBuilder str = new StringBuilder();

    private final List<String> words;
    private int wordIndex;
    private final List<Integer> startOffsets;

    public WordTokenizer(Dictionary dict) {
        this.dict = dict;
        words = new ArrayList<>();
        startOffsets = new ArrayList<>();
        wordIndex = 0;
        matcher = Pattern.compile("\\s+").matcher("");
    }

    @Override
    public boolean incrementToken() {
        clearAttributes();

        if (wordIndex >= words.size()) {
            if (index >= str.length()) {
                return false;
            }

            if (matcher.find()) {
                splitWords(index, matcher.start()-1);
                index = matcher.end();
            } else {
                splitWords(index, str.length()-1);
                index = str.length();
            }
        }

        String word = words.get(wordIndex);
        int start = startOffsets.get(wordIndex);
        termAtt.setEmpty().append(word);
        offsetAtt.setOffset(start, start + word.length() - 1);
        wordIndex ++;
        return true;
    }

    private void splitWords(int start, int end) {
        words.clear();
        wordIndex = 0;
        startOffsets.clear();

        int length = end - start + 1;
        double[] scores = new double[length+1];
        int[] indices = new int[length+1];
        final int maxWordSize = 100;

        scores[0] = 0;
        indices[0] = 0;

        for (int i = 1; i <= length; i++) {
            double tmpScore = Double.MAX_VALUE;
            int tmpIndex = Integer.MAX_VALUE;
            for (int j = Math.max(0, i - maxWordSize); j < i; j ++) {
                String candidate = str.substring(start + j, start + i);
                double score = scores[j] + dict.get(candidate);
                if (score < tmpScore) {
                    tmpScore = score;
                    tmpIndex = j;
                }
                // System.out.printf("candidate %d %d : %s (prob=%f, max_prob=%f, max_index=%d)\n", j, i, candidate, score, tmpScore, tmpIndex);
            }
            scores[i] = tmpScore;
            indices[i] = tmpIndex;
        }

        for (int i = length; i > 0; i = indices[i]) {
            String word = str.substring(start + indices[i], start + i);
            words.add(0, word);
            startOffsets.add(0, start + indices[i]);
        }
    }

    @Override
    public void end() throws IOException {
        super.end();
        final int ofs = correctOffset(str.length());
        offsetAtt.setOffset(ofs, ofs);
    }

    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            str.setLength(0);
            str.trimToSize();
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        fillBuffer(input);
        matcher.reset(str);
        index = 0;
        // System.out.printf("---- RESET %s\n", str);
    }

    // TODO: we should see if we can make this tokenizer work without reading
    // the entire document into RAM, perhaps with Matcher.hitEnd/requireEnd ?
    private void fillBuffer(Reader input) throws IOException {
        int BUFFER_SIZE = 1024;
        char[] buffer = new char[BUFFER_SIZE];
        int len;
        str.setLength(0);
        while ((len = input.read(buffer)) > 0) {
            str.append(buffer, 0, len);
        }
        // System.out.println(str);
    }
}
