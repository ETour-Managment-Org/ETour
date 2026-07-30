package com.example.demo.security;

final class JsonErrorWriter {

    private JsonErrorWriter() {
    }

    static String build(int status, String error, String message, String path, String timestamp) {
        return "{"
             + "\"timestamp\":\"" + escape(timestamp) + "\","
             + "\"status\":" + status + ","
             + "\"error\":\"" + escape(error) + "\","
             + "\"message\":\"" + escape(message) + "\","
             + "\"path\":\"" + escape(path) + "\""
             + "}";
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(value.length() + 16);
        for (int i = 0; i < value.length(); i++) {
            char c = value.charAt(i);
            switch (c) {
                case '"'  -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\n' -> sb.append("\\n");
                case '\r' -> sb.append("\\r");
                case '\t' -> sb.append("\\t");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.toString();
    }
}
