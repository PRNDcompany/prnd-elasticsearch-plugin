package org.elasticsearch.index.analysis;

public class HangulUtils {
    public static final char UNICODE_START = 0xAC00; // 가
    public static final char UNICODE_END = 0xD79F; // 힣
    public static final char UNICODE_CONSONANT_START = 0x3131; // ㄱ
    public static final char UNICODE_CONSONANT_END = 0x3163; // ㅣ

    /*
    * ㄱ ㄲ ㄴ ㄷ ㄸ ㄹ ㅁ ㅂ ㅃ ㅅ
    * ㅆ ㅇ ㅈ ㅉ ㅊ ㅋ ㅌ ㅍ ㅎ
    * */
    public static final char[] LEADING_CONSONANTS = {
        0x3131, 0x3132, 0x3134, 0x3137, 0x3138, 0x3139, 0x3141, 0x3142, 0x3143, 0x3145,
        0x3146, 0x3147, 0x3148, 0x3149, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
    };

    /*
     * ㅏ ㅐ ㅑ ㅒ ㅓ ㅔ ㅕ ㅖ ㅗ ㅘ
     * ㅙ ㅚ ㅛ ㅜ ㅝ ㅞ ㅟ ㅠ ㅡ ㅢ
     * ㅣ
     * */
    public static final char[] MEDIAL_CONSONANTS = {
        0x314F, 0x3150, 0x3151, 0x3152, 0x3153, 0x3154, 0x3155, 0x3156, 0x3157, 0x3158,
        0x3159, 0x315A, 0x315B, 0x315C, 0x315D, 0x315E, 0x315F, 0x3160, 0x3161, 0x3162,
        0x3163
    };

    /*
     *  - ㄱ ㄲ ㄳ ㄴ ㄵ ㄶ ㄷ ㄹ ㄺ
     *  ㄻ ㄼ ㄽ ㄾ ㄿ ㅀ ㅁ ㅂ ㅄ ㅅ
     *  ㅆ ㅇ ㅈ ㅊ ㅋ ㅌ ㅍ ㅎ
     */
    public static final char EMPTY_TRAILING_CONSONANT = 0x0000;
    public static final char[] TRAILING_CONSONANTS = {
        EMPTY_TRAILING_CONSONANT, 0x3131, 0x3132, 0x3133, 0x3134, 0x3135, 0x3136, 0x3137, 0x3139, 0x313A,
        0x313B, 0x313C, 0x313D, 0x313E, 0x313F, 0x3140, 0x3141, 0x3142, 0x3144, 0x3145,
        0x3146, 0x3147, 0x3148, 0x314A, 0x314B, 0x314C, 0x314D, 0x314E
    };

    public static boolean isConsonant(char unicode) { return UNICODE_CONSONANT_START <= unicode && unicode <= UNICODE_CONSONANT_END; }
    public static boolean isHangul(char unicode) {
        return UNICODE_START <= unicode && unicode <= UNICODE_END;
    }

    public static char extractLeadingConsonant(char unicode) {
        int idx = (unicode - UNICODE_START) / (28 * 21);
        return LEADING_CONSONANTS[idx];
    }

    public static char extractMedialConsonant(char unicode) {
        int idx = (unicode - UNICODE_START) % (28 * 21) / 28;
        return MEDIAL_CONSONANTS[idx];
    }

    public static char extractTrailingConsonant(char unicode) {
        int idx = (unicode - UNICODE_START) % (28 * 21) %  28;
        return TRAILING_CONSONANTS[idx];
    }

    public static char removeTrailingConsonant(char unicode) {
        return (char) (unicode - (unicode - UNICODE_START) % 28);
    }
}
