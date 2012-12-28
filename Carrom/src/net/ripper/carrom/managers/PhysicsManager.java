package net.ripper.carrom.managers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.ripper.carrom.managers.clients.IPhysicsManagerClient;
import net.ripper.carrom.managers.physics.collisionResolver.CustomCollisionResolver;
import net.ripper.carrom.model.CollisionPair;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.components.Vector2f;
import net.ripper.util.UtilityFunctions;
import android.graphics.PointF;
import android.graphics.Rect;

public class PhysicsManager {
	private static final String TAG = PhysicsManager.class.getSimpleName();
	List<Piece> pieces = null;
	List<CollisionPair> lastCollisionList;
	Rect boundsRect;
	List<IPhysicsManagerClient> clients;

	Map<Piece, CustomCollisionResolver> customCollisionResolversMap;

	boolean paused = true;

	public final float DAMPING = 0.985f;

	private final float EPSILON = (float) 1e-9;

	public PhysicsManager(Rect boundsRect) {
		this.boundsRect = boundsRect;
		pieces = new ArrayList<Piece>();
		lastCollisionList = new ArrayList<CollisionPair>();
		clients = new ArrayList<IPhysicsManagerClient>();
		customCollisionResolversMap = new HashMap<Piece, CustomCollisionResolver>();
	}

	public void registerClient(IPhysicsManagerClient client) {
		clients.add(client);
	}

	private void motionStoppedNotifyClients() {
		for (IPhysicsManagerClient client : clients) {
			client.allMotionStopped(lastCollisionList);
		}
	}

	public List<CollisionPair> getLastCollisionList() {
		return lastCollisionList;
	}

	public void setLastCollisionList(List<CollisionPair> lastCollisionList) {
		this.lastCollisionList = lastCollisionList;
	}

	public boolean isPaused() {
		return paused;
	}

	public void setPaused(boolean paused) {
		this.paused = paused;
		if (!paused) {
			lastCollisionList.clear();
		}
	}

	public void addPiece(Piece piece) {
		pieces.add(piece);
	}

	public void removePiece(Piece piece) {
		pieces.remove(piece);
	}

	public void addCustomCollisionResolverForPiece(Piece piece,
			CustomCollisionResolver collisionResolver) {
		customCollisionResolversMap.put(piece, collisionResolver);
		if (!pieces.contains(piece)) {
			pieces.add(piece);
		}
	}

	public float update() {
		Piece pieceA, pieceB;
		float nextCollisionTime = 1;
		if (!paused) {
			for (int i = 0; i < pieces.size(); i++) {
				pieceA = pieces.get(i);
				if (!pieceA.isVisible())
					continue;

				for (int j = i + 1; j < pieces.size(); j++) {
					pieceB = pieces.get(j);
					if (!pieceB.isVisible())
						continue;

					if (movingTowards(pieceA, pieceB)) {
						if (isColliding(pieceA, pieceB)) {

							if (customCollisionResolversMap.containsKey(pieceA)) {
								customCollisionResolversMap.get(pieceA)
										.resolveCollision(pieceA, pieceB);
							} else if (customCollisionResolversMap
									.containsKey(pieceB)) {
								customCollisionResolversMap.get(pieceB)
										.resolveCollision(pieceA, pieceB);
							} else {
								// resolve contact
								resolveContact(pieceA, pieceB);
								// Default collision resolver
								resolveCollisionByPConservation(pieceA, pieceB);
							}

							lastCollisionList.add(new CollisionPair(pieceA,
									pieceB));
						}
					}
				}
			}

			boolean moving = false;
			for (Piece piece : pieces) {
				moving = moving | updatePiece(piece, nextCollisionTime);
			}
			// nextCollisionTime = getNextCollisionTime();

			if (!moving) {
				// notify client
				motionStoppedNotifyClients();
			}
		}
		return nextCollisionTime;
	}

	private void resolveContact(Piece pieceA, Piece pieceB) {

		// static-dynamic
		if ((pieceA.isMoving() && !pieceB.isMoving())
				|| (!pieceA.isMoving() && pieceB.isMoving())) {
			Piece staticPiece;
			Piece movingPiece;

			if (pieceA.isMoving()) {
				staticPiece = pieceB;
				movingPiece = pieceA;
			} else {
				staticPiece = pieceA;
				movingPiece = pieceB;
			}

			// REF:https://sites.google.com/site/t3hprogrammer/research/circle-circle-collision-tutorial
			PointF d = UtilityFunctions.closestpointonline(
					movingPiece.region.x, movingPiece.region.y,
					movingPiece.region.x + movingPiece.velocity.x,
					movingPiece.region.y + movingPiece.velocity.y,
					staticPiece.region.x, staticPiece.region.y);

			float closestDistsq = UtilityFunctions.euclideanSqDistance(
					staticPiece.region.x, staticPiece.region.y, d.x, d.y);
			float sumRadiusSq = (movingPiece.region.radius + staticPiece.region.radius)
					* (movingPiece.region.radius + staticPiece.region.radius);

			float backDist = android.util.FloatMath.sqrt(sumRadiusSq
					- closestDistsq);
			Vector2f movingPieceVu = movingPiece.velocity.unitVector();

			movingPiece.region.x = d.x - backDist * movingPieceVu.x;
			movingPiece.region.y = d.y - backDist * movingPieceVu.y;

		} else if (pieceA.isMoving() == false && pieceB.isMoving() == false) {
			// static-static
			float midPtX = (pieceA.region.x + pieceB.region.x) / 2;
			float midPtY = (pieceA.region.y + pieceB.region.y) / 2;
			float dist = UtilityFunctions.euclideanDistance(pieceA.region.x,
					pieceA.region.y, pieceB.region.x, pieceB.region.y);
			pieceA.region.x = midPtX + pieceA.region.radius
					* (pieceA.region.x - pieceB.region.x) / dist;
			pieceA.region.y = midPtY + pieceA.region.radius
					* (pieceA.region.y - pieceB.region.y) / dist;

			pieceB.region.x = midPtX + pieceB.region.radius
					* (pieceB.region.x - pieceA.region.x) / dist;
			pieceB.region.y = midPtY + pieceB.region.radius
					* (pieceB.region.y - pieceA.region.y) / dist;

		}
	}

	private float getNextCollisionTime() {
		Piece pieceA, pieceB;
		float t = 1, temp;
		for (int i = 0; i < pieces.size(); i++) {
			pieceA = pieces.get(i);
			for (int j = i + 1; j < pieces.size(); j++) {
				pieceB = pieces.get(j);
				if (movingTowards(pieceA, pieceB)) {
					temp = getCollisionTime(pieceA, pieceB);
					if (temp != -1) {
						t = Math.min(temp, t);
					}
				}
			}
		}

		return t > 0 ? t : 1;
	}

	private float getCollisionTime(Piece pieceA, Piece pieceB) {
		float t = -1;
		float b = 2 * ((pieceB.region.x - pieceA.region.x)
				* (pieceB.velocity.x - pieceA.velocity.x) + (pieceB.region.y - pieceA.region.y)
				* (pieceB.velocity.y - pieceA.velocity.y));

		float a = ((pieceB.velocity.x - pieceA.velocity.x)
				* (pieceB.velocity.x - pieceA.velocity.x) + (pieceB.velocity.y - pieceA.velocity.y)
				* (pieceB.velocity.y - pieceA.velocity.y));

		float c = ((pieceB.region.x - pieceA.region.x)
				* (pieceB.region.x - pieceA.region.x)
				+ (pieceB.region.y - pieceA.region.y)
				* (pieceB.region.y - pieceA.region.y) - (pieceA.region.radius + pieceB.region.radius)
				* (pieceA.region.radius + pieceB.region.radius));
		float disc = b * b - 4 * a * c;
		// disc <0 then no solution to the quadratic equation.
		// cm will not collide ever
		if (disc >= 0) {
			float t1 = -b - android.util.FloatMath.sqrt(disc) / (2 * a);
			float t2 = -b + android.util.FloatMath.sqrt(disc) / (2 * a);
			t = Math.min(t1, t2);
			// Log.d(TAG, "t1=" + t1 + ",t2=" + t2);
		}
		return t / 100;
	}

	private boolean movingTowards(Piece a, Piece b) {
		/* Position Vector dotted with the Relative Velocity Vector */
		// position vector = (b.x - a.x),(b.y - a.y)
		// Relative velocity vector : (a.vx - b.vx),(a.vy - b.vy)
		// if pv . Rv > 0 then balls are moving towards each other
		// elaborating, if angle between the vectors is acute
		// (b2.x - b1.x) * (b1.vx - b2.vx) + (b2.y - b1.y) * (b1.vy - b2.vy)

		return (b.region.x - a.region.x) * (a.velocity.x - b.velocity.x)
				+ (b.region.y - a.region.y) * (a.velocity.y - b.velocity.y) > 0;
		// Vector2f pv = new Vector2f(b.region.x - a.region.x, b.region.y
		// - a.region.y);
		// Vector2f rv = a.velocity.sub(b.velocity);
		// return pv.dot(rv) > 0;
	}

	/**
	 * Collision resolution using momentum conservation
	 * 
	 * @param a
	 * @param b
	 */
	private void resolveCollisionByPConservation(Piece a, Piece b) {
		// Log.d(TAG, a.toString() + " " + b.toString());

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
		Vector2f vfa = a.velocity.sub(n.mulScalar(optimizedP * b.mass));

		// Calculate v1', the new movement vector of circle1
		// v2' = v2 + optimizedP * m1 * n
		Vector2f vfb = b.velocity.sum(n.mulScalar(optimizedP * a.mass));

		a.velocity = vfa;
		b.velocity = vfb;

	}

	/**
	 * 
	 * @param piece
	 * @param nextCollisionTime
	 * @return true if the piece is still moving
	 */
	public boolean updatePiece(Piece piece, float nextCollisionTime) {
		if (piece.velocity.x != 0 || piece.velocity.y != 0) {

			piece.region.x = piece.region.x + piece.velocity.x
					* nextCollisionTime;
			piece.region.y = piece.region.y + piece.velocity.y
					* nextCollisionTime;

			piece.velocity.scale(DAMPING);

			if (((int) piece.velocity.x) == 0 && ((int) piece.velocity.y) == 0) {
				piece.velocity.x = 0;
				piece.velocity.y = 0;
			}

			// left and right wall
			if (piece.region.x - piece.region.radius <= boundsRect.left) {
				piece.velocity.x = -piece.velocity.x;
				piece.region.x = boundsRect.left + piece.region.radius;
			} else if (piece.region.x + piece.region.radius >= boundsRect.right) {
				piece.velocity.x = -piece.velocity.x;
				piece.region.x = boundsRect.right - piece.region.radius;
			}

			if (piece.region.y - piece.region.radius <= boundsRect.top) {
				// float penetration = piece.region.radius - piece.region.y
				// + boundsRect.top;
				// float t = Math.abs(penetration / piece.velocity.y);
				// piece.region.x += -piece.velocity.x * t;

				piece.velocity.y = -piece.velocity.y;
				piece.region.y = boundsRect.top + piece.region.radius;

			} else if (piece.region.y + piece.region.radius >= boundsRect.bottom) {
				piece.velocity.y = -piece.velocity.y;
				piece.region.y = boundsRect.bottom - piece.region.radius;
			}

			//
			return true;
		}
		return false;
	}

	public boolean isColliding(Piece pieceA, Piece pieceB) {
		float sqDistance = UtilityFunctions.euclideanSqDistance(
				pieceA.region.x, pieceA.region.y, pieceB.region.x,
				pieceB.region.y);

		if (sqDistance <= ((pieceA.region.radius + pieceB.region.radius + EPSILON) * (pieceA.region.radius
				+ pieceB.region.radius + EPSILON)))
			return true;
		return false;
	}
}
