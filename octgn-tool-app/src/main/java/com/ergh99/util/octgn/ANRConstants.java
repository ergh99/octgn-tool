package com.ergh99.util.octgn;

public interface ANRConstants {

    public static final String ANR_TITLE = "Android-Netrunner";
    public static final String ANR_ID = "0f38e453-26df-4c04-9d67-6d43de939c77";
    public static final String FORMAT_EXT = ".png";
    public static final String OLD_FORMAT_EXT = ".jpg";

    public static enum SKIPPABLE_SETS {
        MARKERS, PROMOS;

        public static boolean contains(String setName) {
            for (SKIPPABLE_SETS set : SKIPPABLE_SETS.values()) {
                if (set.toString().equalsIgnoreCase(setName)) {
                    return true;
                }
            }
            return false;
        }
    }
}
