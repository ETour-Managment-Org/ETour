package com.etour.common;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class CityNames {
    private static final String SEPARATORS = "\\s+[-\u2013\u2014]\\s+|\\s*,\\s*|\\s*/\\s*|\\s+to\\s+";

    private CityNames() {
    }

    public static List<String> split(String destination) {
        List<String> out = new ArrayList<>();
        if (destination == null || destination.isBlank()) {
            return out;
        }

        Set<String> seen = new LinkedHashSet<>();
        for (String part : destination.split(SEPARATORS)) {
            String city = part.trim();
            if (city.isEmpty()) {
                continue;
            }
            if (city.length() > 100) {
                city = city.substring(0, 100);
            }
            seen.add(city);
        }
        out.addAll(seen);
        return out;
    }
}
