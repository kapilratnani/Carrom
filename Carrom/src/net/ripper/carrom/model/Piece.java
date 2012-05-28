package net.ripper.carrom.model;

import android.util.Log;
import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Vector2f;
import net.ripper.util.UtilityFunctions;

/**
 * Common class which represents all kinds of carrom-men i.e. White, Black,
 * Queen and Striker
 * 
 * The Renderer uses this class to draw the particular piece
 * 
 * @author Kapil Ratnani
 * 
 */
public class Piece {
	public int id;
	public Vector2f velocity;
	public Circle region;
	public int color;
	public Board board;
	public float mass;

	public final float DAMPING = 0.985f;

	private final float EPSILON = (float) 1e-9;

	public enum PieceType {
		BLACK, WHITE, STRIKER, QUEEN
	};

	public PieceType pieceType;

	public String toString() {
		return "< " + pieceType.name() + " " + id + " " + region.toString()
				+ "w= " + mass + " >";
	}

	public void update(float nextCollisionTime) {
		if (velocity.x != 0 || velocity.y != 0) {

			region.x = region.x + velocity.x * nextCollisionTime;
			region.y = region.y + velocity.y * nextCollisionTime;

			velocity.scale(DAMPING);

			if (Math.floor(velocity.x) == 0 && Math.floor(velocity.y) == 0) {
				velocity.scale(0);
			}

			// left and right wall
			if (region.x - region.radius <= board.boundsRect.left) {
				velocity.x = -velocity.x;
				region.x = board.boundsRect.left + region.radius;
				Log.d("H collision", "reverse");
			} else if (region.x + region.radius >= board.boundsRect.right) {
				velocity.x = -velocity.x;
				region.x = board.boundsRect.right - region.radius;
				Log.d("H collision", "reverse");
			}

			if (region.y - region.radius <= board.boundsRect.top) {
				velocity.y = -velocity.y;
				region.y = board.boundsRect.top + region.radius;
				Log.d("V collision", "reverse");
			} else if (region.y + region.radius >= board.boundsRect.bottom) {
				velocity.y = -velocity.y;
				region.y = board.boundsRect.bottom - region.radius;
				Log.d("V collision", "reverse");
			}

		}
	}

	public boolean isColliding(Piece piece) {
		float sqDistance = UtilityFunctions.euclideanSqDistance(piece.region.x,
				piece.region.y, this.region.x, this.region.y);

		if (sqDistance <= ((piece.region.radius + this.region.radius + EPSILON) * (piece.region.radius
				+ this.region.radius + EPSILON)))
			return true;
		return false;
	}
}
