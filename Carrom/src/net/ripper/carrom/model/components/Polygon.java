package net.ripper.carrom.model.components;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;

public class Polygon {
	private List<PointF> points = new ArrayList<PointF>();
	public int tag;

	public Polygon() {
	}

	public Polygon(List<PointF> points) {
		this.points = points;
	}

	public void addPoint(PointF point) {
		points.add(point);
	}

	public List<PointF> getPoints() {
		return points;
	}

	public void removePoint(PointF point) {
		points.remove(point);
	}

	public void drawPolygon(Canvas canvas, Paint paint) {
		PointF p1 = points.get(0);
		for (int i = 1; i < points.size(); i++) {
			canvas.drawLine(p1.x, p1.y, points.get(i).x, points.get(i).y, paint);
			p1 = points.get(i);
		}

		p1 = points.get(0);
		canvas.drawLine(p1.x, p1.y, points.get(points.size() - 1).x,
				points.get(points.size() - 1).y, paint);
	}

	public PointF getLeftMostPoint() {
		PointF leftPoint = points.get(0);
		for (PointF point : points) {
			if (leftPoint.x > point.x) {
				leftPoint = point;
			}
		}
		return leftPoint;
	}

	public PointF getRightMostPoint() {
		PointF rightPoint = points.get(0);
		for (PointF point : points) {
			if (rightPoint.x < point.x) {
				rightPoint = point;
			}
		}
		return rightPoint;
	}

	public PointF getTopMostPoint() {
		PointF topPoint = points.get(0);
		for (PointF point : points) {
			if (topPoint.y > point.y) {
				topPoint = point;
			}
		}
		return topPoint;
	}

	public PointF getBottomMostPoint() {
		PointF bottomPoint = points.get(0);
		for (PointF point : points) {
			if (bottomPoint.y < point.y) {
				bottomPoint = point;
			}
		}
		return bottomPoint;
	}
}
