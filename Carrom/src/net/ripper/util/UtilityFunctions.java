package net.ripper.util;

import java.util.List;

import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Polygon;
import net.ripper.carrom.model.components.Vector2f;
import android.graphics.PointF;
import android.graphics.RectF;

public class UtilityFunctions {

	public static float euclideanSqDistance(float x1, float y1, float x2,
			float y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	public static float euclideanDistance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1));
	}

	public static boolean RectangleCircleIntersection(RectF rect, Circle circle) {

		// find closest point of the rectangle from the center of the circle
		float closestX = clampF(circle.x, rect.left, rect.right);
		float closestY = clampF(circle.y, rect.top, rect.bottom);

		// find distance between the closest point and the center of the circle
		float distanceX = circle.x - closestX;
		float distanceY = circle.y - closestY;

		// if the distance is less than radius, then intersection occurs
		return (distanceX * distanceX) + (distanceY * distanceY) < (circle.radius * circle.radius);
	}

	public static float clampF(float value, float min, float max) {
		return value > max ? max : value < min ? min : value;
	}

	public static boolean CirclePolygonIntersection(Polygon p, Circle c) {
		List<PointF> polyPoints = p.getPoints();
		for (int i = 0; i < polyPoints.size() - 1; i++) {
			if (CircleLineIntersection(c.x, c.y, c.radius, polyPoints.get(i).x,
					polyPoints.get(i).y, polyPoints.get(i + 1).x,
					polyPoints.get(i + 1).y))
				return true;
		}

		if (CircleLineIntersection(c.x, c.y, c.radius, polyPoints.get(0).x,
				polyPoints.get(0).y, polyPoints.get(polyPoints.size() - 1).x,
				polyPoints.get(polyPoints.size() - 1).y))
			return true;

		return false;
	}

	public static double pointToLineDistance(PointF A, PointF B, PointF P) {
		double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y)
				* (B.y - A.y));
		return Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x))
				/ normalLength;
	}

	public static boolean CircleLineIntersection(float cx, float cy, float cr,
			float ax, float ay, float bx, float by) {
		float vx = bx - ax;
		float vy = by - ay;
		float xdiff = ax - cx;
		float ydiff = ay - cy;
		float a = vx * vx + vy * vy;
		float b = 2 * ((vx * xdiff) + (vy * ydiff));
		float c = xdiff * xdiff + ydiff * ydiff - cr * cr;
		float quad = b * b - 4 * a * c;
		if (quad >= 0) {
			// An infinite collision is happening, but let's not stop here
			float quadsqrt = (float) Math.sqrt(quad);
			for (int i = -1; i <= 1; i += 2) {
				// Returns the two coordinates of the intersection points
				float t = (i * -b + quadsqrt) / (2 * a);
				float x = ax + (i * vx * t);
				float y = ay + (i * vy * t);
				// If one of them is in the boundaries of the segment, it
				// collides
				if (x >= Math.min(ax, bx) && x <= Math.max(ax, bx)
						&& y >= Math.min(ay, by) && y <= Math.max(ay, by))
					return true;
			}
		}
		return false;
	}
}
