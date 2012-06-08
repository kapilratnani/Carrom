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
import android.graphics.Rect;
import android.util.Log;

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
		pieces.add(piece);
	}

	public float update() {
		Piece pieceA, pieceB;
		float nextCollisionTime = 1;
		if (!paused) {
			for (int i = 0; i < pieces.size(); i++) {
				pieceA = pieces.get(i);
				for (int j = i + 1; j < pieces.size(); j++) {
					pieceB = pieces.get(j);
					if (movingTowards(pieceA, pieceB)
							&& isColliding(pieceA, pieceB)) {

						if (customCollisionResolversMap.containsKey(pieceA)) {
							customCollisionResolversMap.get(pieceA)
									.resolveCollision(pieceA, pieceB);
						} else if (customCollisionResolversMap
								.containsKey(pieceB)) {
							customCollisionResolversMap.get(pieceB)
									.resolveCollision(pieceA, pieceB);
						} else {
							// Default collision resolver
							resolveCollisionByPConservation(pieceA, pieceB);
						}

						lastCollisionList
								.add(new CollisionPair(pieceA, pieceB));
					}
				}
			}

			nextCollisionTime = getNextCollisionTime();
			boolean moving = false;
			for (Piece piece : pieces) {
				moving = moving | updatePiece(piece, nextCollisionTime);
			}

			if (!moving) {
				// notify client
				motionStoppedNotifyClients();
			}
		}
		return nextCollisionTime;
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
		if (disc >= 0) {
			t = (float) Math.min(-b - Math.sqrt(disc) / (2 * a),
					-b + Math.sqrt(disc) / (2 * a));
		}
		return t;
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
