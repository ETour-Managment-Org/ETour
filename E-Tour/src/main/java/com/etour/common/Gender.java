package com.etour.common;

import java.util.List;

public final class Gender {
    public static final String MALE = "MALE";
    public static final String FEMALE = "FEMALE";
    public static final String OTHER = "OTHER";

    public static final List<String> ALL = List.of(MALE, FEMALE, OTHER);

    private Gender() {
    }

    public static boolean isValid(String value) {
        return value == null || value.isBlank() || ALL.contains(value.trim().toUpperCase());
    }

    public static String normalise(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim().toUpperCase();
    }
}
