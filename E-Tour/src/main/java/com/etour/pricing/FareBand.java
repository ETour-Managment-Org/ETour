package com.etour.pricing;

public enum FareBand {
    TWIN_SHARING("Twin sharing (per person)"),
    SINGLE("Single occupancy"),
    EXTRA_PERSON("Extra person in room"),
    CHILD_WITH_BED("Child with bed"),
    CHILD_WITHOUT_BED("Child without bed");

    private final String label;

    FareBand(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static FareBand fromOccupancy(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String v = value.trim().toUpperCase();
        if (v.equals("ADULT")) {
            return TWIN_SHARING;
        }
        if (v.equals("SINGLE_PERSON")) {
            return SINGLE;
        }
        for (FareBand b : values()) {
            if (b.name().equals(v)) {
                return b;
            }
        }
        return null;
    }
}
