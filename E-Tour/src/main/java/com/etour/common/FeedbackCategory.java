package com.etour.common;

import java.util.List;

public final class FeedbackCategory {
    public static final String SUGGESTION = "SUGGESTION";
    public static final String PROBLEM = "PROBLEM";
    public static final String COMPLIMENT = "COMPLIMENT";
    public static final String OTHER = "OTHER";

    public static final List<String> ALL =
            List.of(SUGGESTION, PROBLEM, COMPLIMENT, OTHER);

    public static final String NEW = "NEW";
    public static final String REVIEWED = "REVIEWED";
    public static final String CLOSED = "CLOSED";

    private FeedbackCategory() {
    }

    public static boolean isValidCategory(String value) {
        return value != null && ALL.contains(value.trim().toUpperCase());
    }
}
