package org.elasticsearch.index.analysis;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final public class WordTokenizer extends Tokenizer {
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final Dictionary dict;
    private final Matcher matcher;
    private int index = 0;

    private final StringBuilder str = new StringBuilder();

    private List<String> words;
    private List<String> words2;
    private int wordIndex;
    private List<Integer> startOffsets;
    private List<Integer> startOffsets2;

    public WordTokenizer(Dictionary dict) {
        this.dict = dict;
        words = new ArrayList<>();
        startOffsets = new ArrayList<>();
        words2 = new ArrayList<>();
        startOffsets2 = new ArrayList<>();
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

            wordIndex = 0;
            boolean found = matcher.find();
            int end = found ? matcher.start() - 1 : str.length() - 1;
            System.out.printf("s1=%s\n", str.toString());
            double score = splitWords(str.toString(), index, end, words, startOffsets);

            char lastChar = str.charAt(end);
            if (!found && HangulUtils.isHangul(lastChar) && HangulUtils.extractTrailingConsonant(lastChar) != HangulUtils.EMPTY_TRAILING_CONSONANT) {
                // 마지막 문자가 종성이 있는 한글인 경우, 없는 경우에 대해 단어를 계산하고 score가 낮다면 교체
                str.setCharAt(end, HangulUtils.removeTrailingConsonant(lastChar));
                System.out.printf("s2=%s\n", str.toString());
                double score2 = splitWords(str.toString(), index, end, words2, startOffsets2) + dict.get(Character.toString(lastChar));
                str.setCharAt(end, lastChar);
                System.out.printf("score=%f score2=%f\n", score, score2);

                if (score > score2) {
                    words2.add(Character.toString(HangulUtils.extractTrailingConsonant(lastChar)));
                    startOffsets2.add(str.length());

                    List<String> tmpWords = words;
                    List<Integer> tmpStartOffsets = startOffsets;

                    words = words2;
                    startOffsets = startOffsets2;

                    words2 = tmpWords;
                    startOffsets2 = tmpStartOffsets;
                }
            }

            index = found ? matcher.end() : str.length();
        }

        String word = words.get(wordIndex);
        int start = startOffsets.get(wordIndex);
        termAtt.setEmpty().append(word);
        offsetAtt.setOffset(start, start + word.length() - 1);
        wordIndex ++;
        return true;
    }

    private double splitWords(String str, int start, int end, List<String> words, List<Integer> startOffsets) {
        words.clear();
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
        return scores[scores.length - 1];
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
         System.out.printf("---- RESET %s\n", str);
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
