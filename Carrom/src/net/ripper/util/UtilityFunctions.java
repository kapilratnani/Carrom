package net.ripper.util;

import java.util.List;

import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Polygon;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;

public class UtilityFunctions {

	public static float euclideanSqDistance(float x1, float y1, float x2,
			float y2) {
		return (x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1);
	}

	public static float euclideanDistance(float x1, float y1, float x2, float y2) {
		return android.util.FloatMath.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1)
				* (y2 - y1));
	}

	public static boolean RectangleCircleIntersection(RectF rect, Circle circle) {

		// find closest point of the rectangle from the centre of the circle
		float closestX = clampF(circle.x, rect.left, rect.right);
		float closestY = clampF(circle.y, rect.top, rect.bottom);

		// find distance between the closest point and the centre of the circle
		float distanceX = circle.x - closestX;
		float distanceY = circle.y - closestY;

		// if the distance is less than radius, then intersection occurs
		return (distanceX * distanceX) + (distanceY * distanceY) < (circle.radius * circle.radius);
	}

	public static boolean RectangleCircleIntersection(Rect rect, Circle circle) {

		// find closest point of the rectangle from the centre of the circle
		float closestX = clampF(circle.x, rect.left, rect.right);
		float closestY = clampF(circle.y, rect.top, rect.bottom);

		// find distance between the closest point and the centre of the circle
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

		PointF leftPoint = p.getLeftMostPoint();
		PointF rightPoint = p.getRightMostPoint();
		PointF topPoint = p.getTopMostPoint();
		PointF bottomPoint = p.getBottomMostPoint();

		if (c.x > leftPoint.x && c.x < rightPoint.x && c.y < bottomPoint.y
				&& c.y > topPoint.y)
			return true;

		return false;
	}

	public static boolean CirclePolygonIntersection2(Polygon p, Circle c) {
		List<PointF> polyPoints = p.getPoints();
		PointF closestPoint = null;
		for (int i = 0; i < polyPoints.size() - 1; i++) {
			closestPoint = closestpointonline(polyPoints.get(i).x,
					polyPoints.get(i).y, polyPoints.get(i + 1).x,
					polyPoints.get(i).y, c.x, c.y);
			if (euclideanSqDistance(closestPoint.x, closestPoint.y, c.x, c.y) <= (c.radius * c.radius))
				return true;
		}

		closestPoint = closestpointonline(polyPoints.get(0).x,
				polyPoints.get(0).y, polyPoints.get(polyPoints.size() - 1).x,
				polyPoints.get(polyPoints.size() - 1).y, c.x, c.y);

		if (euclideanSqDistance(closestPoint.x, closestPoint.y, c.x, c.y) <= (c.radius * c.radius))
			return true;

		return false;
	}

	// public static double pointToLineDistance(PointF A, PointF B, PointF P) {
	// double normalLength = Math.sqrt((B.x - A.x) * (B.x - A.x) + (B.y - A.y)
	// * (B.y - A.y));
	// return Math.abs((P.x - A.x) * (B.y - A.y) - (P.y - A.y) * (B.x - A.x))
	// / normalLength;
	// }

	public static PointF closestpointonline(float lx1, float ly1, float lx2,
			float ly2, float x0, float y0) {
		float A1 = ly2 - ly1;
		float B1 = lx1 - lx2;
		float C1 = (ly2 - ly1) * lx1 + (lx1 - lx2) * ly1;
		float C2 = -B1 * x0 + A1 * y0;
		float det = A1 * A1 - -B1 * B1;
		float cx = 0;
		float cy = 0;
		if (det != 0) {
			cx = ((A1 * C1 - B1 * C2) / det);
			cy = ((A1 * C2 - -B1 * C1) / det);
		} else {
			cx = x0;
			cy = y0;
		}
		return new PointF(cx, cy);
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
			float quadsqrt = android.util.FloatMath.sqrt(quad);
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

	public static PointF mirrorXaxis(float x, float y, float originY) {
		if (y > originY) {
			return new PointF(x, -(y - 2 * originY));
		} else {
			return new PointF(x, y + 2 * originY);
		}
	}

	public static PointF mirrorYaxis(float x, float y, float originX) {
		if (x > originX) {
			return new PointF(-(x - 2 * originX), y);
		} else {
			return new PointF(x + 2 * originX, y);
		}
	}

}
