package edu.eci.arsw.networking.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonUtilTest {

    @Test
    void plainTextIsUnchanged() {
        assertEquals("Ada", JsonUtil.escape("Ada"));
    }

    @Test
    void doubleQuotesAreEscaped() {
        assertEquals("say \\\"hi\\\"", JsonUtil.escape("say \"hi\""));
    }

    @Test
    void backslashesAreEscaped() {
        assertEquals("C:\\\\path", JsonUtil.escape("C:\\path"));
    }

    @Test
    void newlinesAndTabsAreEscaped() {
        assertEquals("line1\\nline2\\ttabbed", JsonUtil.escape("line1\nline2\ttabbed"));
    }

    @Test
    void controlCharactersAreEscapedAsUnicode() {
        assertEquals("\\u0001", JsonUtil.escape("\u0001"));
    }

    @Test
    void nullBecomesEmptyString() {
        assertEquals("", JsonUtil.escape(null));
    }

    @Test
    void quoteWrapsEscapedValueInDoubleQuotes() {
        assertEquals("\"say \\\"hi\\\"\"", JsonUtil.quote("say \"hi\""));
    }

    @Test
    void injectionAttemptCannotCloseTheStringOrAddFields() {
        String malicious = "\", \"admin\": true, \"x\": \"";
        String quoted = JsonUtil.quote(malicious);
        // The escaped value must contain no raw, unescaped double quote,
        // otherwise it could terminate the JSON string early.
        String inner = quoted.substring(1, quoted.length() - 1);
        assertEquals(-1, indexOfUnescapedQuote(inner));
    }

    private int indexOfUnescapedQuote(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) == '"' && (i == 0 || s.charAt(i - 1) != '\\')) {
                return i;
            }
        }
        return -1;
    }
}
