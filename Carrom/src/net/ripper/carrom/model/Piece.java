package net.ripper.carrom.model;

import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Vector2f;
import android.graphics.Color;

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
	public int id = 0;
	public Vector2f velocity = new Vector2f(0, 0);
	public Circle region = new Circle(0, 0, 0);
	public int color = Color.BLACK;
	public Board board;
	public float mass = 0;
	public boolean inHole = false;

	public enum PieceType {
		BLACK, WHITE, STRIKER, QUEEN, HOLE
	};

	public PieceType pieceType;

	public Piece() {
	}

	// copy constructor
	public Piece(Piece piece) {
		this.id = piece.id;
		this.velocity = new Vector2f(piece.velocity);
		this.region = new Circle(piece.region);
		this.color = piece.color;
		this.board = piece.board;
		this.mass = piece.mass;
		this.inHole = piece.inHole;
		this.pieceType = piece.pieceType;
	}

	public boolean isMoving() {
		return this.velocity.x != 0 || this.velocity.y != 0;
	}

	public String toString() {
		return "< " + pieceType.name() + " " + id + " " + region.toString()
				+ "w= " + mass + " v=" + velocity.toString() + " >";
	}

	public boolean isVisible() {
		return !inHole;
	}
}
