package org.elasticsearch.index.analysis;

public class HangulParser {

    public String parse(String token) {
        if (token.equals("")) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char[] chArr = token.toCharArray();
        for (char ch : chArr) {
            if (HangulUtils.isHangul(ch)) {
                char leading = HangulUtils.extractLeadingConsonant(ch),
                        medial = HangulUtils.extractMedialConsonant(ch),
                        trailing = HangulUtils.extractTrailingConsonant(ch);
                sb.append(leading);
                sb.append(medial);
                if (trailing != HangulUtils.EMPTY_TRAILING_CONSONANT)
                    sb.append(trailing);
            }
        }
        return sb.toString();
    }
}
