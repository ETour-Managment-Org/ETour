package com.example.demo.pricing;

public final class Occupancy {

    public static final int MAX_PER_ROOM = 2;
    public static final int MAX_EXTRA_BEDS_PER_ROOM = 1;
    public static final int MAX_OCCUPANTS_PER_ROOM = MAX_PER_ROOM + MAX_EXTRA_BEDS_PER_ROOM;

    public static final String TWIN_SHARING = "TWIN_SHARING";
    public static final String SINGLE = "SINGLE";
    public static final String EXTRA_PERSON = "EXTRA_PERSON";

    private Occupancy() {
    }

    public static String defaultOccupancy() {
        return TWIN_SHARING;
    }

    public static int roomsRequired(int twinSharingCount, int singleCount, int extraBedCount) {
        int twinRooms = (int) Math.ceil(twinSharingCount / (double) MAX_PER_ROOM);
        int extraBedCapacity = twinRooms * MAX_EXTRA_BEDS_PER_ROOM;
        int overflowBeds = Math.max(0, extraBedCount - extraBedCapacity);
        return twinRooms + singleCount + overflowBeds;
    }
}
