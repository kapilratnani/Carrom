package net.ripper.carrom.ai;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.model.Board;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.Piece.PieceType;
import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.PolarLine;
import net.ripper.carrom.model.components.Polygon;
import net.ripper.carrom.model.components.Vector2f;
import net.ripper.util.UtilityFunctions;
import android.graphics.PointF;
import android.util.Log;

public class AIPlayerImpl extends AIPlayer {

	public List<Polygon> polygons = new ArrayList<Polygon>();
	private HashMap<Piece, ArrayList<Polygon>> pieceToPolygonMap = new HashMap<Piece, ArrayList<Polygon>>();
	public PolarLine line1;
	public PolarLine line2;

	@Override
	public Shot getShot(Set<Piece> blackPieces, Set<Piece> whitePieces,
			Piece striker, Piece queen, Board board, Player aiPlayer) {
		Circle[] holes = board.holes;
		Shot shot = null;
		// all pieces which are on board
		Set<Piece> allOnBoardPieces = new HashSet<Piece>();

		polygons = new ArrayList<Polygon>();
		pieceToPolygonMap = new HashMap<Piece, ArrayList<Polygon>>();
		List<Piece> pottablePieces = new ArrayList<Piece>();

		if (aiPlayer.pieceType == PieceType.BLACK) {
			for (Piece black : blackPieces) {
				if (!black.inHole) {
					pottablePieces.add(black);
				}
			}
		} else {
			for (Piece white : whitePieces) {
				if (!white.inHole) {
					pottablePieces.add(white);
				}
			}
		}

		for (Piece piece : blackPieces) {
			if (!piece.inHole)
				allOnBoardPieces.add(piece);
		}

		for (Piece piece : whitePieces) {
			if (!piece.inHole)
				allOnBoardPieces.add(piece);
		}

		if (!queen.inHole) {
			pottablePieces.add(queen);
			allOnBoardPieces.add(queen);
		}

		// for each pottable c/m make a rect connecting the hole and cm
		// make sure there is a direct path between the hole and cm
		Polygon poly = null;
		boolean directPath = true;
		for (Piece cm : pottablePieces) {
			for (int i = 0; i < holes.length; i++) {
				directPath = true;
				// only direct shots as of now
				if ((aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT && (i == Board.TOP_LEFT_HOLE || i == Board.TOP_RIGHT_HOLE))
						|| (aiPlayer.shootingRectIndex == Board.TOP_SHOOTING_RECT && (i == Board.BOTTOM_LEFT_HOLE || i == Board.BOTTOM_RIGHT_HOLE))
						|| (aiPlayer.shootingRectIndex == Board.LEFT_SHOOTING_RECT && (i == Board.TOP_RIGHT_HOLE || i == Board.BOTTOM_RIGHT_HOLE))
						|| (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT && (i == Board.TOP_LEFT_HOLE || i == Board.BOTTOM_LEFT_HOLE))) {

					if (cm.region.y < holes[i].y) {
						poly = makeRectFromLine(cm.region.x, cm.region.y,
								holes[i].x, holes[i].y, 2 * cm.region.radius);
					} else {
						poly = makeRectFromLine(holes[i].x, holes[i].y,
								cm.region.x, cm.region.y, 2 * cm.region.radius);
					}

					// check if rect intersects with any other cm
					for (Piece piece : allOnBoardPieces) {
						if (!piece.equals(cm)) {
							if (UtilityFunctions.CirclePolygonIntersection(
									poly, piece.region)) {
								directPath = false;
								break;
							}
						}
					}

					// all pottable pieces must have a direct path to the hole
					if (directPath) {
						// tag contains hole index
						poly.tag = i;
						polygons.add(poly);
						ArrayList<Polygon> p = pieceToPolygonMap.get(cm);
						if (p == null) {
							p = new ArrayList<Polygon>();
						}
						p.add(poly);
						pieceToPolygonMap.put(cm, p);
					}
				}
			}

		}

		// for all pieces, estimate most promising shot
		// exit, when a shot is found
		for (Piece cm : pieceToPolygonMap.keySet()) {
			List<Polygon> ps = pieceToPolygonMap.get(cm);

			for (Polygon p : ps) {
				if (aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT) {
					PointF leftPoint = p.getLeftMostPoint();
					PointF rightPoint = p.getRightMostPoint();
					int holeIndex = p.tag;

					if (holeIndex == Board.TOP_LEFT_HOLE) {
						float stX = rightPoint.x;
						if (stX < (board.shootingRect[aiPlayer.shootingRectIndex].left + striker.region.radius)) {
							stX = board.shootingRect[aiPlayer.shootingRectIndex].left
									+ striker.region.radius;
						}

						float stY = board.shootingRect[aiPlayer.shootingRectIndex]
								.centerY();
						float slope = getSlopeInRad(stX, stY, cm.region.x,
								cm.region.y);
						float distance = UtilityFunctions.euclideanDistance(
								stX, stY, cm.region.x, cm.region.y);

						PolarLine shootingLine = new PolarLine(stX, stY,
								distance, slope);

						line1 = new PolarLine(stX, stY, distance, slope);

						float initSlope = slope - striker.region.radius
								/ distance;
						shootingLine.rotateTo(initSlope);

						line1.rotateTo(initSlope);

						float totalArcLen = 2 * striker.region.radius
								/ distance;
						Piece strikerCopy = new Piece(striker);
						Piece cmCopy = new Piece(cm);

						// remember: position vector= target point - source
						// point
						Vector2f cmToHoleVector = new Vector2f(
								board.holes[holeIndex].x - cm.region.x,
								board.holes[holeIndex].y - cm.region.y);

						// range of acos is 0 to PI
						float angleVfCmNCmToHoleVector = (float) Math.PI;
						float shotAngle = 0;
						Vector2f finalVfStriker = null;
						// use "a" to rotate shooting line by 1 degree or 0.02
						// radian

						for (float a = 0; a < totalArcLen; a += 0.02) {
							// for each iteration, simulate collision
							// and see if the angle between the vector, joining
							// hole and cm,
							// and the vector of the final velocity of cm is
							// minimum

							// rotate shooting line
							shootingLine.rotateBy(0.02f);

							// store final velocities
							Vector2f vfStriker = new Vector2f(0, 0), vfCm = new Vector2f(
									0, 0);
							// give striker a velocity of 7 in the current
							// direction of shooting line
							Vector2f velDirection = new Vector2f(
									shootingLine.getFinalX()
											- shootingLine.originX,
									shootingLine.getFinalY()
											- shootingLine.originY);

							strikerCopy.velocity = velDirection.unitVector()
									.mulScalar(7);
							Log.d("sd", strikerCopy.velocity.toString());
							simulateCollision(cmCopy, strikerCopy, vfCm,
									vfStriker);

							// check the angle between vfCm and cmToHoleVector
							// get the vector having the minimum angle
							float tmpAngle = vfCm.angle(cmToHoleVector);
							if (tmpAngle < angleVfCmNCmToHoleVector) {
								angleVfCmNCmToHoleVector = tmpAngle;
								finalVfStriker = strikerCopy.velocity;
								shotAngle = shootingLine.theta;
							}
						}
						shot = new Shot();
						shot.strikerX = stX;
						shot.strikerY = stY;
						shot.angle = shotAngle;
						shot.v = 7;
						line2 = new PolarLine(shootingLine.originX,
								shootingLine.originY, shootingLine.r,
								shootingLine.theta);

					}
				}
			}
		}

		return shot;
	}

	private Polygon makeRectFromLine(float x1, float y1, float x2, float y2,
			float width) {
		// get two points perpendicularly away from the line by width/2
		// one point from (x1,y1) and other from (x2,y2)

		// get the slope angle of line
		float theta = getSlopeInRad(x1, y1, x2, y2);

		float x3 = (float) (x1 - (width / 2) * Math.cos(theta - Math.PI / 2));
		float y3 = (float) (y1 - (width / 2) * Math.sin(theta - Math.PI / 2));

		float x4 = (float) (x1 + (width / 2) * Math.cos(theta - Math.PI / 2));
		float y4 = (float) (y1 + (width / 2) * Math.sin(theta - Math.PI / 2));

		float x5 = (float) (x2 + (width / 2) * Math.cos(theta - Math.PI / 2));
		float y5 = (float) (y2 + (width / 2) * Math.sin(theta - Math.PI / 2));

		float x6 = (float) (x2 - (width / 2) * Math.cos(theta - Math.PI / 2));
		float y6 = (float) (y2 - (width / 2) * Math.sin(theta - Math.PI / 2));

		Polygon poly = new Polygon();
		poly.addPoint(new PointF(x3, y3));
		poly.addPoint(new PointF(x4, y4));
		poly.addPoint(new PointF(x5, y5));
		poly.addPoint(new PointF(x6, y6));

		// RectF rect = new RectF(x3, y3, x4, y4);

		// rect.sort();
		return poly;
	}

	private float getSlopeInRad(float x1, float y1, float x2, float y2) {
		float chX = x2 - x1;
		float chY = y2 - y1;
		float theta = (float) Math.atan(chY / chX);

		// check in which quadrant the point is
		if (chX < 0 && chY >= 0) {
			// 2nd quadrant
			theta = theta + (float) Math.PI;
		} else if (chX < 0 && chY < 0) {
			// 3rd quadrant
			theta = theta - (float) Math.PI;
		}
		return theta;
	}

	private Vector2f makeVelocityVectorFromPositionVector(float x1, float y1,
			float x2, float y2, float velocity) {
		Vector2f v = new Vector2f(x2 - x1, y2 - y1);
		return v.unitVector().mulScalar(velocity);
	}

	private void simulateCollision(Piece a, Piece b, Vector2f vfa, Vector2f vfb) {
		// First, find the normalized vector n from the center of
		// circle1 to the center of circle2
		Vector2f n = new Vector2f(a.region.x - b.region.x, a.region.y
				- b.region.y);
		n.normalize();

		// Find the length of the component of each of the movement
		// vectors along n.
		// a1 = v1 . n
		// a2 = v2 . n
		float a1 = a.velocity.dot(n);
		float a2 = b.velocity.dot(n);

		// Using the optimized version,
		// optimizedP = 2(a1 - a2)
		// -----------
		// m1 + m2
		float optimizedP = (float) ((2.0 * (a1 - a2)) / (a.mass + b.mass));

		// Calculate v1', the new movement vector of circle1
		// v1' = v1 - optimizedP * m2 * n
		Vector2f tmpVfa = a.velocity.sub(n.mulScalar(optimizedP * b.mass));
		vfa.x = tmpVfa.x;
		vfa.y = tmpVfa.y;

		// Calculate v1', the new movement vector of circle1
		// v2' = v2 + optimizedP * m1 * n
		Vector2f tmpVfb = b.velocity.sum(n.mulScalar(optimizedP * a.mass));
		vfb.x = tmpVfb.x;
		vfb.y = tmpVfb.y;
	}

}
