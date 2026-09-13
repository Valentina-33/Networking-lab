package edu.eci.arsw.networking.util;

/**
 * The lab's JSON responses are a handful of fixed shapes, so a full JSON
 * library would be overkill. This class only provides the one thing that
 * still needs to be correct by hand: escaping a value before it is embedded
 * in a JSON string literal, so untrusted query-string input (a name, an
 * error message) can never break out of the string or inject extra fields.
 */
public final class JsonUtil {

    private JsonUtil() {
    }

    public static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(value.length());
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                case '\b' -> out.append("\\b");
                case '\f' -> out.append("\\f");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }

    public static String quote(String value) {
        return "\"" + escape(value) + "\"";
    }
}
