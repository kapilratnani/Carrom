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
import net.ripper.util.MutableFloat;
import net.ripper.util.UtilityFunctions;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.Log;

public class AIPlayerImpl extends AIPlayer {

	/**
	 * speed in pixels/frame
	 */
	public static int strikerInitSpeedDirectShot = 7;
	public static int strikerInitSpeedReboundShot = 10;

	/**
	 * contains a mapping of c/m to rect which represents the motion of c/m
	 * towards the hole.
	 */

	public PolarLine line1;
	public PolarLine line2;
	public PolarLine line3;
	public PolarLine line4;
	public PolarLine line5;
	public Polygon testRect;

	public Piece strikerTest;
	float sttx, stty;
	private Shot prevShot;

	private Set<Piece> allOnBoardPieces = new HashSet<Piece>();

	/**
	 * contains all rects which represent the motion/direction of striker to hit
	 * a particular c/m
	 */
	public List<Polygon> polygons = new ArrayList<Polygon>();
	private HashMap<Piece, ArrayList<Polygon>> pieceToDirectShotPolygonMap = new HashMap<Piece, ArrayList<Polygon>>();
	private HashMap<Piece, ArrayList<Polygon>> pieceToReboundShotPolygonMap = new HashMap<Piece, ArrayList<Polygon>>();
	private List<Piece> pottablePieces = new ArrayList<Piece>();
	private List<Piece> cmsOntheWay = new ArrayList<Piece>();

	/**
	 * for direct shots, choose holes which lie opposite to the shooting rect
	 * 
	 * check if the pottable c/m touches the shooting rect, if it does then
	 * leave it. it can't be hit directly
	 */
	private boolean directShotTest(Player aiPlayer, Board board, Piece cm,
			int holeIndex) {
		/**
		 * check if it touches the current shooting rect
		 */
		if (!UtilityFunctions.RectangleCircleIntersection(
				board.shootingRect[aiPlayer.shootingRectIndex], cm.region)) {
			/**
			 * check if the current hole lies opposite to the shooting rect and
			 * cm lies in direct shooting path rather than in the rebound path
			 */
			Rect shootingRect = board.shootingRect[aiPlayer.shootingRectIndex];
			if (aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT
					&& (holeIndex == Board.TOP_LEFT_HOLE || holeIndex == Board.TOP_RIGHT_HOLE)
					&& shootingRect.top > cm.region.y) {
				return true;
			} else if (aiPlayer.shootingRectIndex == Board.TOP_SHOOTING_RECT
					&& (holeIndex == Board.BOTTOM_LEFT_HOLE || holeIndex == Board.BOTTOM_RIGHT_HOLE)
					&& shootingRect.bottom < cm.region.y) {
				return true;
			} else if (aiPlayer.shootingRectIndex == Board.LEFT_SHOOTING_RECT
					&& (holeIndex == Board.TOP_RIGHT_HOLE || holeIndex == Board.BOTTOM_RIGHT_HOLE)
					&& shootingRect.right < cm.region.x) {
				return true;
			} else if (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT
					&& (holeIndex == Board.TOP_LEFT_HOLE || holeIndex == Board.BOTTOM_LEFT_HOLE)
					&& shootingRect.left > cm.region.x) {
				return true;
			} else {
				return false;
			}
		} else
			return false;
	}

	/**
	 * get start position of striker
	 * 
	 * @param holeIndex
	 * @param shootingRectIndex
	 * @param cmToHoleRect
	 * @param aiPlayer
	 * @param board
	 * @return
	 */
	private PointF getStrikerStartPosition(int holeIndex,
			int shootingRectIndex, Polygon cmToHoleRect, Board board,
			Piece striker) {
		PointF stPos = null;
		Rect shootingRect;
		float stX = 0, stY = 0;

		/**
		 * The trick is to return a position on the shooting rect from which if
		 * the cm is hit by the striker, it will go towards the current hole.
		 * 
		 */
		if (Board.BOTTOM_SHOOTING_RECT == shootingRectIndex
				|| Board.TOP_SHOOTING_RECT == shootingRectIndex) {
			shootingRect = board.shootingRect[shootingRectIndex];

			if (Board.TOP_LEFT_HOLE == holeIndex
					|| Board.BOTTOM_LEFT_HOLE == holeIndex) {
				PointF rightPoint = cmToHoleRect.getRightMostPoint();
				stX = rightPoint.x;
				if (stX < (shootingRect.left + striker.region.radius)) {
					stX = shootingRect.left + striker.region.radius;
				} else if (stX > (shootingRect.right - striker.region.radius)) {
					stX = shootingRect.right - striker.region.radius;
				}
			} else if (Board.TOP_RIGHT_HOLE == holeIndex
					|| Board.BOTTOM_RIGHT_HOLE == holeIndex) {
				PointF leftPoint = cmToHoleRect.getLeftMostPoint();
				stX = leftPoint.x;
				if (stX > (shootingRect.right - striker.region.radius)) {
					stX = shootingRect.right - striker.region.radius;
				} else if (stX < (shootingRect.left + striker.region.radius)) {
					stX = shootingRect.left + striker.region.radius;
				}

			}
			stY = shootingRect.centerY();

			stPos = new PointF(stX, stY);
		} else if (Board.LEFT_SHOOTING_RECT == shootingRectIndex
				|| Board.RIGHT_SHOOTING_RECT == shootingRectIndex) {
			shootingRect = board.shootingRect[shootingRectIndex];

			if (Board.TOP_LEFT_HOLE == holeIndex
					|| Board.TOP_RIGHT_HOLE == holeIndex) {
				PointF bottomPoint = cmToHoleRect.getBottomMostPoint();
				stY = bottomPoint.y;
				if (stY > (shootingRect.bottom - striker.region.radius)) {
					stY = shootingRect.bottom - striker.region.radius;
				} else if (stY < shootingRect.top + striker.region.radius) {
					stY = shootingRect.top + striker.region.radius;
				}
			} else if (Board.BOTTOM_LEFT_HOLE == holeIndex
					|| Board.BOTTOM_RIGHT_HOLE == holeIndex) {
				PointF topPoint = cmToHoleRect.getTopMostPoint();
				stX = topPoint.y;
				if (stY < (shootingRect.top + striker.region.radius)) {
					stY = shootingRect.top + striker.region.radius;
				} else if (stY > (shootingRect.bottom - striker.region.radius)) {
					stY = shootingRect.bottom - striker.region.radius;
				}

			}
			stX = shootingRect.centerX();

			stPos = new PointF(stX, stY);
		}
		return stPos;
	}

	/**
	 * Moves the striker to the next possible shooting location in the shooting
	 * rect
	 * 
	 * @param currentPosition
	 * @param holeIndex
	 * @param shootingRectIndex
	 * @param striker
	 * @param board
	 * @return null, when the striker is at the ends of rect
	 */
	private PointF getNextStrikerPosition(PointF currentPosition,
			int holeIndex, int shootingRectIndex, Piece striker, Board board) {
		PointF stPos = null;
		Rect shootingRect;
		float stX = 0, stY = 0;
		/**
		 * The trick is to return a position on the shooting rect from which if
		 * the cm is hit by the striker, it will go towards the current hole.
		 * 
		 */
		if (Board.BOTTOM_SHOOTING_RECT == shootingRectIndex
				|| Board.TOP_SHOOTING_RECT == shootingRectIndex) {
			shootingRect = board.shootingRect[shootingRectIndex];

			if (Board.TOP_LEFT_HOLE == holeIndex
					|| Board.BOTTOM_LEFT_HOLE == holeIndex) {
				stX = currentPosition.x + striker.region.radius / 2;
				if (stX > (shootingRect.right - striker.region.radius)) {
					stX = currentPosition.x;
				}
			} else if (Board.TOP_RIGHT_HOLE == holeIndex
					|| Board.BOTTOM_RIGHT_HOLE == holeIndex) {
				stX = currentPosition.x - striker.region.radius / 2;
				if (stX < (shootingRect.left + striker.region.radius)) {
					stX = currentPosition.x;
				}

			}
			stY = shootingRect.centerY();

			stPos = new PointF(stX, stY);
		} else if (Board.LEFT_SHOOTING_RECT == shootingRectIndex
				|| Board.RIGHT_SHOOTING_RECT == shootingRectIndex) {
			shootingRect = board.shootingRect[shootingRectIndex];

			if (Board.TOP_LEFT_HOLE == holeIndex
					|| Board.TOP_RIGHT_HOLE == holeIndex) {

				stY = currentPosition.y + striker.region.radius / 2;
				if (stY > (shootingRect.bottom - striker.region.radius)) {
					stY = currentPosition.y;
				}
			} else if (Board.BOTTOM_LEFT_HOLE == holeIndex
					|| Board.BOTTOM_RIGHT_HOLE == holeIndex) {
				stX = currentPosition.y - striker.region.radius / 2;
				if (stY < (shootingRect.top + striker.region.radius)) {
					stY = currentPosition.y;
				}

			}
			stX = shootingRect.centerX();

			stPos = new PointF(stX, stY);
		}

		if (stPos.equals(currentPosition.x, currentPosition.y))
			return null;
		return stPos;
	}

	@Override
	public Shot getShot(Set<Piece> blackPieces, Set<Piece> whitePieces,
			Piece striker, Piece queen, Board board, Player aiPlayer) {
		Circle[] holes = board.holes;
		Shot shot = null;
		Shot directShot = null;
		Shot reboundShot = null;
		List<Shot> directShots = new ArrayList<Shot>();
		List<Shot> reboundShots = new ArrayList<Shot>();
		MutableFloat diff = new MutableFloat(0);
		boolean intersectsWithOtherCm = false;
		// all pieces which are on board
		allOnBoardPieces.clear();
		pieceToDirectShotPolygonMap.clear();
		pieceToReboundShotPolygonMap.clear();
		pottablePieces.clear();
		polygons.clear();

		/**
		 * stores all pieces which can be potted in a list
		 */
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

		/**
		 * stores all pieces on board in a list
		 */
		for (Piece piece : blackPieces) {
			if (!piece.inHole)
				allOnBoardPieces.add(piece);
		}

		for (Piece piece : whitePieces) {
			if (!piece.inHole)
				allOnBoardPieces.add(piece);
		}

		/**
		 * queen
		 */
		if (!queen.inHole) {
			pottablePieces.add(0, queen);
			allOnBoardPieces.add(queen);
		}

		/**
		 * early exit, if pottable pieces are zero
		 */
		// FIXME: temp exit, game manager will not call when their is nothing to
		// shoot
		if (pottablePieces.size() == 0)
			return null;

		/**
		 * for each pottable c/m make a rect connecting the hole and cm make
		 * sure there is a clear path between the hole and cm.
		 * 
		 * Pieces which can be potted directly go in
		 * piecesToDirectShotPolygonMap and for the ones with rebound shots go
		 * in piecesToReboundShotPolygonMap
		 */
		Polygon poly = null;
		boolean clearPath;
		boolean directShotPath;
		for (Piece cm : pottablePieces) {
			for (int holeIndex = 0; holeIndex < holes.length; holeIndex++) {
				clearPath = true;

				/**
				 * check if the cm can be potted via a direct shot or a rebound
				 * shot
				 */
				if (directShotTest(aiPlayer, board, cm, holeIndex)) {
					directShotPath = true;
				} else {
					// can be potted via a rebound shot
					directShotPath = false;
				}

				/**
				 * make rect to represent shot path
				 */
				if (cm.region.y < holes[holeIndex].y) {
					poly = makeRectFromLine(cm.region.x, cm.region.y,
							holes[holeIndex].x, holes[holeIndex].y,
							2 * cm.region.radius);
				} else {
					poly = makeRectFromLine(holes[holeIndex].x,
							holes[holeIndex].y, cm.region.x, cm.region.y,
							2 * cm.region.radius);
				}

				// check if rect intersects with any other cm
				for (Piece piece : allOnBoardPieces) {
					if (!piece.equals(cm)) {
						if (UtilityFunctions.CirclePolygonIntersection(poly,
								piece.region)) {
							clearPath = false;
							break;
						}
					}
				}

				if (clearPath) {
					// tag contains hole index
					poly.tag = holeIndex;
					polygons.add(poly);
					if (directShotPath) {
						ArrayList<Polygon> p = pieceToDirectShotPolygonMap
								.get(cm);
						if (null == p) {
							p = new ArrayList<Polygon>();
						}
						p.add(poly);
						pieceToDirectShotPolygonMap.put(cm, p);
					} else {
						ArrayList<Polygon> p = pieceToReboundShotPolygonMap
								.get(cm);
						if (null == p) {
							p = new ArrayList<Polygon>();
						}
						p.add(poly);
						pieceToReboundShotPolygonMap.put(cm, p);
					}
				}

			}

		}

		/**
		 * for all pieces, estimate most promising shot. Exit, when a shot is
		 * found
		 */
		/**
		 * go through direct shots first
		 */
		Set<Piece> pottableCm = pieceToDirectShotPolygonMap.keySet();
		PointF strikerPos = null;
		boolean promisingShotFound = false;
		for (Piece cm : pottableCm) {
			List<Polygon> ps = pieceToDirectShotPolygonMap.get(cm);

			if (promisingShotFound)
				break;

			for (Polygon p : ps) {
				if (promisingShotFound)
					break;
				// if (aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT
				// && Board.TOP_LEFT_HOLE == p.tag) {
				int holeIndex = p.tag;

				/**
				 * get striker start position to hit the current c/m. On this
				 * position striker is kept for the first time when hitting a
				 * c/m. Shot selection starts from this position. Search
				 * radially first, if no optimal shot found, move the striker
				 * away from the polygon rect to a new position. Each position
				 * must be away from the previous by striker radius(for no
				 * reason, just an assumption)
				 */
				strikerPos = getStrikerStartPosition(holeIndex,
						aiPlayer.shootingRectIndex, p, board, striker);

				// /**
				// * while simulating shots with the target cm, striker might
				// * touch other cms on the way. Make a list of the cm which may
				// * be on the way.
				// */
				// cmsOntheWay.clear();

				while (null != strikerPos) {

					float stX = strikerPos.x, stY = strikerPos.y;

					/**
					 * 
					 */
					float slope = getSlopeInRad(stX, stY, cm.region.x,
							cm.region.y);
					float distance = UtilityFunctions.euclideanDistance(stX,
							stY, cm.region.x, cm.region.y);

					PolarLine shootingLine = new PolarLine(stX, stY, distance,
							slope);

					float initSlope = slope - striker.region.radius / distance;
					shootingLine.rotateTo(initSlope);

					float totalArcLen = 2 * striker.region.radius / distance;

					Piece strikerCopy = new Piece(striker);
					Piece cmCopy = new Piece(cm);

					strikerCopy.region.x = stX;
					strikerCopy.region.y = stY;

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

					line5 = new PolarLine(cmCopy.region.x, cmCopy.region.y,
							cmToHoleVector.mag(), (float) Math.atan2(
									cmToHoleVector.y, cmToHoleVector.x));

					Vector2f finalVfCm = null;

					for (float a = 0; a < totalArcLen; a += 0.005) {
						intersectsWithOtherCm = false;
						// TODO to figure out an efficient way, for finding
						// intersecting cm in the path of striker and target cm

						// for each iteration, simulate collision
						// and see if the angle between the vector, joining
						// hole and cm,
						// and the vector of the final velocity of cm is
						// minimum

						// rotate shooting line
						shootingLine.rotateBy(0.005f);

						testRect = makeRectFromLine(strikerPos.x, strikerPos.y,
								cm.region.x, cm.region.y,
								2 * striker.region.radius);

						for (Piece piece : allOnBoardPieces) {
							if (piece != cm) {
								if (UtilityFunctions.CirclePolygonIntersection(
										testRect, piece.region)) {
									intersectsWithOtherCm = true;
									break;
								}
							}
						}

						if (intersectsWithOtherCm) {
							intersectsWithOtherCm = false;
							continue;
						}

						// store final velocities
						Vector2f vfStriker = new Vector2f(0, 0), vfCm = new Vector2f(
								0, 0);
						// give striker a velocity of 7 in the current
						// direction of shooting line
						Vector2f velDirection = new Vector2f(
								shootingLine.getFinalX() - shootingLine.originX,
								shootingLine.getFinalY() - shootingLine.originY);

						strikerCopy.velocity = velDirection.unitVector()
								.mulScalar(strikerInitSpeedDirectShot);

						simulateCollision(cmCopy, strikerCopy, vfCm, vfStriker);

						// check the angle between vfCm and cmToHoleVector
						// get the vector having the minimum angle
						float tmpAngle = (float) (vfCm.angle(cmToHoleVector));
						if (tmpAngle < angleVfCmNCmToHoleVector) {
							angleVfCmNCmToHoleVector = tmpAngle;
							finalVfStriker = strikerCopy.velocity;
							shotAngle = shootingLine.theta;
							finalVfCm = vfCm;
						}

						line4 = new PolarLine(cmCopy.region.x, cmCopy.region.y,
								cmToHoleVector.mag(), (float) Math.atan2(
										vfCm.y, vfCm.x));

					}
					directShot = new Shot();
					directShot.strikerX = stX;
					directShot.strikerY = stY;
					directShot.angle = shotAngle;
					directShot.v = strikerInitSpeedDirectShot;

					shootingLine.rotateTo(shotAngle);
					directShot.strikerVelocity = new Vector2f(
							shootingLine.getFinalX() - shootingLine.originX,
							shootingLine.getFinalY() - shootingLine.originY);
					directShot.strikerVelocity = directShot.strikerVelocity
							.unitVector().mulScalar(strikerInitSpeedDirectShot);

					if (finalVfCm != null) {
						line4 = new PolarLine(cmCopy.region.x, cmCopy.region.y,
								cmToHoleVector.mag(), (float) Math.atan2(
										finalVfCm.y, finalVfCm.x));

						// check if the shot is promising i.e. will it hit the
						// hole. For this, check the angle between
						// vfcm and position vector between cm and hole, if the
						// angle is less than the angle made by an
						// arc, with length equal to radius of hole. If so, it
						// will
						// hit the hole
						if (isPromising(finalVfCm, cmToHoleVector, board, diff)) {
							promisingShotFound = true;
							shot = directShot;
							shot.diff = diff.getNum();
							break;
						}
						directShot.diff = diff.getNum();
						directShots.add(directShot);
					}
					strikerPos = getNextStrikerPosition(strikerPos, holeIndex,
							aiPlayer.shootingRectIndex, striker, board);

				}
				// }

			}

		}

		if (!promisingShotFound) {
			// try for rebound shots now,
			/**
			 * Trick for rebound shots is just to make the mirror image of the
			 * cm along the x axis or y axis according to the target hole
			 */
			pottableCm = pieceToReboundShotPolygonMap.keySet();
			for (Piece cm : pottableCm) {
				List<Polygon> ps = pieceToReboundShotPolygonMap.get(cm);
				for (Polygon p : ps) {
					if (promisingShotFound)
						break;

					int holeIndex = p.tag;
					/**
					 * get striker start position to hit the current c/m. On
					 * this position striker is kept for the first time when
					 * hitting a c/m. Shot selection starts from this position.
					 * Search radially first, if no optimal shot found, move the
					 * striker away from the polygon rect to a new position.
					 * Each position must be away from the previous by striker
					 * radius(for no reason, just an assumption)
					 */
					strikerPos = getStrikerStartPosition(holeIndex,
							aiPlayer.shootingRectIndex, p, board, striker);

					Piece strikerCopy = new Piece(striker);
					Piece cmCopy = new Piece(cm);
					PointF mirroredCmPoint = null;
					PointF mirroredHole = null;
					if (aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT
							&& (holeIndex == Board.BOTTOM_LEFT_HOLE || holeIndex == Board.BOTTOM_RIGHT_HOLE)) {
						mirroredCmPoint = UtilityFunctions.mirrorXaxis(
								cmCopy.region.x, cmCopy.region.y,
								board.boundsRect.top);

						mirroredHole = UtilityFunctions.mirrorXaxis(
								board.holes[holeIndex].x,
								board.holes[holeIndex].y, board.boundsRect.top);

					} else if (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT
							&& (holeIndex == Board.BOTTOM_RIGHT_HOLE || holeIndex == Board.TOP_RIGHT_HOLE)) {
						mirroredCmPoint = UtilityFunctions.mirrorYaxis(
								cmCopy.region.x, cmCopy.region.y,
								board.boundsRect.left);
						mirroredHole = UtilityFunctions
								.mirrorYaxis(board.holes[holeIndex].x,
										board.holes[holeIndex].y,
										board.boundsRect.left);
					} else if (aiPlayer.shootingRectIndex == Board.TOP_SHOOTING_RECT
							&& (holeIndex == Board.TOP_LEFT_HOLE || holeIndex == Board.TOP_RIGHT_HOLE)) {
						mirroredCmPoint = UtilityFunctions.mirrorXaxis(
								cmCopy.region.x, cmCopy.region.y,
								board.boundsRect.bottom);
						mirroredHole = UtilityFunctions.mirrorXaxis(
								board.holes[holeIndex].x,
								board.holes[holeIndex].y,
								board.boundsRect.bottom);
					} else if (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT
							&& (holeIndex == Board.TOP_LEFT_HOLE || holeIndex == Board.BOTTOM_LEFT_HOLE)) {
						mirroredCmPoint = UtilityFunctions.mirrorYaxis(
								cmCopy.region.x, cmCopy.region.y,
								board.boundsRect.right);
						mirroredHole = UtilityFunctions.mirrorYaxis(
								board.holes[holeIndex].x,
								board.holes[holeIndex].y,
								board.boundsRect.right);

					} else {
						continue;
					}

					cmCopy.region.x = mirroredCmPoint.x;
					cmCopy.region.y = mirroredCmPoint.y;

					while (null != strikerPos) {

						float stX = strikerPos.x, stY = strikerPos.y;
						/**
						 * 
						 */
						float slope = getSlopeInRad(stX, stY, cmCopy.region.x,
								cmCopy.region.y);
						float distance = UtilityFunctions.euclideanDistance(
								stX, stY, cmCopy.region.x, cmCopy.region.y);

						PolarLine shootingLine = new PolarLine(stX, stY,
								distance, slope);

						float initSlope = slope - striker.region.radius
								/ distance;
						shootingLine.rotateTo(initSlope);

						float totalArcLen = 2 * striker.region.radius
								/ distance;

						strikerCopy.region.x = stX;
						strikerCopy.region.y = stY;

						// remember: position vector= target point - source
						// point
						Vector2f cmToHoleVector = new Vector2f(mirroredHole.x
								- cmCopy.region.x, mirroredHole.y
								- cmCopy.region.y);

						// range of acos is 0 to PI
						float angleVfCmNCmToHoleVector = (float) Math.PI;
						float shotAngle = 0;
						Vector2f finalVfStriker = null;
						// use "a" to rotate shooting line by 1 degree or 0.02
						// radian

						Vector2f finalVfCm = null;
						for (float a = 0; a < totalArcLen; a += 0.005) {
							// TODO to figure out an efficient way, for finding
							// intersecting cm in the path of striker and target
							// cm

							// for each iteration, simulate collision
							// and see if the angle between the vector, joining
							// hole and cm,
							// and the vector of the final velocity of cm is
							// minimum

							// rotate shooting line
							shootingLine.rotateBy(0.005f);

//							testRect = makeRectFromLine(strikerPos.x,
//									strikerPos.y, cmCopy.region.x,
//									cmCopy.region.y, 2 * striker.region.radius);
//
//							for (Piece piece : allOnBoardPieces) {
//								if (piece != cm) {
//									Circle testCircle = new Circle(
//											piece.region.radius, 0, 0);
//									if (aiPlayer.shootingRectIndex == Board.BOTTOM_SHOOTING_RECT) {
//										mirroredCmPoint = UtilityFunctions
//												.mirrorXaxis(piece.region.x,
//														piece.region.y,
//														board.boundsRect.top);
//									} else if (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT) {
//										mirroredCmPoint = UtilityFunctions
//												.mirrorYaxis(piece.region.x,
//														piece.region.y,
//														board.boundsRect.left);
//
//									} else if (aiPlayer.shootingRectIndex == Board.TOP_SHOOTING_RECT) {
//										mirroredCmPoint = UtilityFunctions
//												.mirrorXaxis(piece.region.x,
//														piece.region.y,
//														board.boundsRect.bottom);
//									} else if (aiPlayer.shootingRectIndex == Board.RIGHT_SHOOTING_RECT) {
//										mirroredCmPoint = UtilityFunctions
//												.mirrorYaxis(piece.region.x,
//														piece.region.y,
//														board.boundsRect.right);
//
//									}
//									testCircle.x = mirroredCmPoint.x;
//									testCircle.y = mirroredCmPoint.y;
//
//									if (UtilityFunctions
//											.CirclePolygonIntersection(
//													testRect, testCircle)) {
//										intersectsWithOtherCm = true;
//										break;
//									}
//								}
//							}
//
//							if (intersectsWithOtherCm) {
//								intersectsWithOtherCm = false;
//								continue;
//							}

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
									.mulScalar(strikerInitSpeedReboundShot);

							simulateCollision(cmCopy, strikerCopy, vfCm,
									vfStriker);

							// check the angle between vfCm and cmToHoleVector
							// get the vector having the minimum angle
							float tmpAngle = (float) (vfCm
									.angle(cmToHoleVector));
							if (tmpAngle < angleVfCmNCmToHoleVector) {
								angleVfCmNCmToHoleVector = tmpAngle;
								finalVfStriker = strikerCopy.velocity;
								shotAngle = shootingLine.theta;
								finalVfCm = vfCm;
								sttx = strikerTest.region.x;
								stty = strikerTest.region.y;
							}

						}
						reboundShot = new Shot();
						reboundShot.strikerX = stX;
						reboundShot.strikerY = stY;
						reboundShot.angle = shotAngle;
						reboundShot.v = strikerInitSpeedReboundShot;

						shootingLine.rotateTo(shotAngle);
						reboundShot.strikerVelocity = new Vector2f(
								shootingLine.getFinalX() - shootingLine.originX,
								shootingLine.getFinalY() - shootingLine.originY);
						reboundShot.strikerVelocity = reboundShot.strikerVelocity
								.unitVector().mulScalar(
										strikerInitSpeedReboundShot);

						// check if the shot is promising i.e. will it hit the
						// hole. For this, check the angle between
						// vfcm and position vector between cm and hole, if the
						// angle is less than the angle made by an
						// arc, with length equal to radius of hole. If so, it
						// will
						// hit the hole
						if (isPromising(finalVfCm, cmToHoleVector, board, diff)) {
							promisingShotFound = true;

							strikerTest.region.x = sttx;
							strikerTest.region.y = stty;
							shot = reboundShot;
							shot.diff = diff.getNum();
							break;
						}
						reboundShot.diff = diff.getNum();
						reboundShots.add(reboundShot);

						strikerPos = getNextStrikerPosition(strikerPos,
								holeIndex, aiPlayer.shootingRectIndex, striker,
								board);

					}

				}
			}
		}

		if (!promisingShotFound) {
			// go through all shots collected and choose the one
			// with the least diff value

			float minDiff = Float.MAX_VALUE, len;
			int i;
			Shot lastPossibleShot = null;
			if (directShots.size() > 0) {
				len = directShots.size();
				for (i = 0; i < len; i++) {
					if (minDiff > directShots.get(i).diff) {
						lastPossibleShot = directShots.get(i);
						minDiff = lastPossibleShot.diff;
					}
				}
				Log.e(this.getClass().getName(), "MinDiffDirect");
			} else {
				Log.e(this.getClass().getName(), "NoDirectShots");
			}

			if (lastPossibleShot == null) {
				if (reboundShots.size() > 0) {
					len = reboundShots.size();
					for (i = 0; i < len; i++) {
						if (minDiff > reboundShots.get(i).diff) {
							lastPossibleShot = reboundShots.get(i);
							minDiff = reboundShots.get(i).diff;
						}
					}
					Log.e(this.getClass().getName(), "MinDiffRebound");
				} else {
					Log.e(this.getClass().getName(), "NoReboundShots");
				}
			}

			if (prevShot != null && prevShot.equals(lastPossibleShot)) {
				/**
				 * This happens when the ai gets stuck in some rebound shot.
				 * Ignore the shot.
				 */
				lastPossibleShot = null;
			}

			if (lastPossibleShot == null) {
				/**
				 * Find a pottable piece which can be shot directly or via
				 * rebound
				 */
				Piece cm;
				Rect shootingRect = board.shootingRect[aiPlayer.shootingRectIndex];
				boolean direct;
				int playerShootingRectIndex = aiPlayer.shootingRectIndex;

				direct = false;
				cm = pottablePieces.get(0);
				if (!UtilityFunctions.RectangleCircleIntersection(shootingRect,
						cm.region)) {
					if (playerShootingRectIndex == Board.BOTTOM_SHOOTING_RECT) {
						if (cm.region.y < shootingRect.top) {
							direct = true;
						}
					} else if (playerShootingRectIndex == Board.TOP_SHOOTING_RECT) {
						if (cm.region.y > shootingRect.bottom) {
							direct = true;
						}
					} else if (playerShootingRectIndex == Board.LEFT_SHOOTING_RECT) {
						if (cm.region.x > shootingRect.right) {
							direct = true;
						}
					} else /* Right shooting rect */{
						if (cm.region.x < shootingRect.left) {
							direct = true;
						}
					}
				}

				// calculate a vector towards the cm
				lastPossibleShot = new Shot();
				float stX, stY;
				// decide striker position
				if (playerShootingRectIndex == Board.TOP_SHOOTING_RECT
						|| playerShootingRectIndex == Board.BOTTOM_SHOOTING_RECT) {
					stY = striker.region.y;
					// FIX ME: Temp method to set striker pos
					if (cm.region.x >= board.centerCircle.x) {

						stX = cm.region.x - striker.region.radius * 2;
					} else {
						stX = cm.region.x + striker.region.radius * 2;
					}
				} else {
					stX = striker.region.x;
					if (cm.region.y >= board.centerCircle.y) {
						stY = cm.region.y - striker.region.radius * 2;
					} else {
						stY = cm.region.y + striker.region.radius * 2;
					}
				}
				lastPossibleShot.strikerX = stX;
				lastPossibleShot.strikerY = stY;
				Vector2f shotVector = new Vector2f(0, 0);
				if (direct) {
					Log.d(this.getClass().getName(), "direct shot");
					// calculate shot direction
					shotVector.x = cm.region.x - stX;
					shotVector.y = cm.region.y - stY;
					shotVector = shotVector.unitVector().mulScalar(
							strikerInitSpeedDirectShot);
					lastPossibleShot.strikerVelocity = shotVector;
				} else {
					Log.d(this.getClass().getName(), "rebound shot");
					PointF mirroredCmPoint = null;

					if (playerShootingRectIndex == Board.BOTTOM_SHOOTING_RECT) {
						mirroredCmPoint = UtilityFunctions.mirrorXaxis(
								cm.region.x, cm.region.y, board.boundsRect.top);
					} else if (playerShootingRectIndex == Board.TOP_SHOOTING_RECT) {
						mirroredCmPoint = UtilityFunctions.mirrorXaxis(
								cm.region.x, cm.region.y,
								board.boundsRect.bottom);
					} else if (playerShootingRectIndex == Board.LEFT_SHOOTING_RECT) {
						mirroredCmPoint = UtilityFunctions
								.mirrorYaxis(cm.region.x, cm.region.y,
										board.boundsRect.left);
					} else /* Right Shooting rect */{
						mirroredCmPoint = UtilityFunctions.mirrorYaxis(
								cm.region.x, cm.region.y,
								board.boundsRect.right);
					}
					shotVector.x = mirroredCmPoint.x - stX;
					shotVector.y = mirroredCmPoint.y - stY;
					shotVector = shotVector.unitVector().mulScalar(
							strikerInitSpeedReboundShot);
					lastPossibleShot.strikerVelocity = shotVector;
				}

				Log.e(this.getClass().getName(), "No MinDiffShot");
			} else {
				Log.e(this.getClass().getName(), "MinDiffShot");
			}
			shot = lastPossibleShot;

			Log.d(this.getClass().getName(), "NoPromisingShot");
		}

		prevShot = shot;
		return shot;
	}

	private boolean isPromising(Vector2f vfcm, Vector2f cmToHoleVector,
			Board board, MutableFloat diff) {
		if (vfcm == null || cmToHoleVector == null)
			return false;
		float angle = vfcm.angle(cmToHoleVector);
		float holeRadius = board.holes[0].radius;
		float arcAngle = holeRadius / cmToHoleVector.mag();
		diff.setNum((float) Math.abs(angle - arcAngle));
		return angle < arcAngle;
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
		return (float) (theta < 0 ? theta + 2 * Math.PI : theta);
	}

	// private Vector2f makeVelocityVectorFromPositionVector(float x1, float y1,
	// float x2, float y2, float velocity) {
	// Vector2f v = new Vector2f(x2 - x1, y2 - y1);
	// return v.unitVector().mulScalar(velocity);
	// }

	private void simulateCollision(Piece cm, Piece striker, Vector2f vfcm,
			Vector2f vfst) {
		// REF:http://www.gamasutra.com/view/feature/131424/pool_hall_lessons_fast_accurate_.php?page=2

		// here cm is always stationary and striker is moving
		// first bring the striker near to cm, so that they are almost touching
		// each other
		// N - normalised vector of striker velocity
		Vector2f N = new Vector2f(striker.velocity);
		N.normalize();

		// C - position vector from striker to cm
		Vector2f C = new Vector2f(cm.region.x - striker.region.x, cm.region.y
				- striker.region.y);

		// D - projection of C on the direction of striker velocity vector
		float D = N.dot(C);

		// F - shortest distance between striker velocity vector and cm's centre
		float cMag = C.mag();
		float Fsquare = cMag * cMag - D * D;

		// R - distance between cm and striker when they are touching
		float Rsquare = (cm.region.radius + striker.region.radius)
				* (cm.region.radius + striker.region.radius);

		float T = android.util.FloatMath.sqrt(Rsquare - Fsquare);

		// dist - distance to move in the direction of velocity vector
		// so that cm and striker will just touch each other
		float dist = D - T;

		// now move the striker by dist so that the cm and striker touch each
		// other
		float prevStrikerX = striker.region.x;
		float prevStrikerY = striker.region.y;
		PolarLine vLine = new PolarLine(striker.region.x, striker.region.y,
				dist,
				(float) Math.atan2(striker.velocity.y, striker.velocity.x));

		striker.region.x = vLine.getFinalX();
		striker.region.y = vLine.getFinalY();

		strikerTest = new Piece(striker);
		strikerTest.region.y = (-strikerTest.region.y + 36);

		// now the striker and cm touch each other
		// normal momentum conservation equation will work

		// First, find the normalised vector n from the centre of
		// circle1 to the centre of circle2
		Vector2f n = new Vector2f(cm.region.x - striker.region.x, cm.region.y
				- striker.region.y);
		n.normalize();

		// Find the length of the component of each of the movement
		// vectors along n.
		// a1 = v1 . n
		// a2 = v2 . n
		float a1 = cm.velocity.dot(n);
		float a2 = striker.velocity.dot(n);

		// Using the optimised version,
		// optimizedP = 2(a1 - a2)
		// -----------
		// m1 + m2
		float optimizedP = (float) ((2.0 * (a1 - a2)) / (cm.mass + striker.mass));

		// Calculate v1', the new movement vector of circle1
		// v1' = v1 - optimizedP * m2 * n
		Vector2f tmpVfa = cm.velocity.sub(n
				.mulScalar(optimizedP * striker.mass));
		vfcm.x = tmpVfa.x;
		vfcm.y = tmpVfa.y;

		// Calculate v1', the new movement vector of circle1
		// v2' = v2 + optimizedP * m1 * n
		Vector2f tmpVfb = striker.velocity.sum(n
				.mulScalar(optimizedP * cm.mass));
		vfst.x = tmpVfb.x;
		vfst.y = tmpVfb.y;

		striker.region.x = prevStrikerX;
		striker.region.y = prevStrikerY;
	}

}
