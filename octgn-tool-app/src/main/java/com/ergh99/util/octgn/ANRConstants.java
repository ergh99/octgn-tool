package com.ergh99.util.octgn;

import java.util.Arrays;
import java.util.List;

public interface ANRConstants {

	public static final String ANR_TITLE = "Android-Netrunner";
	public static final String ANR_ID =
			"0f38e453-26df-4c04-9d67-6d43de939c77";
	public static final String FORMAT_EXT = ".png";
	public static final String OLD_FORMAT_EXT = ".jpg";
	public static final String MARKERS_SET = "Markers";
	public static final String PROMOS_SET = "Promos";
	public static final List<String> skippableSets =
			Arrays.asList(MARKERS_SET, PROMOS_SET);
}
