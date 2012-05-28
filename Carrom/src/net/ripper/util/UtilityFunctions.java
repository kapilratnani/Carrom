package net.ripper.util;

public class UtilityFunctions {

	public static float euclideanSqDistance(float x1, float y1, float x2,
			float y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	public static float euclideanDistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}
}
