package net.ripper.carrom.managers;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import android.graphics.Color;

import net.ripper.carrom.model.Board;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.Piece.PieceType;
import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Vector2f;
import net.ripper.util.Clock;

/**
 * This class:
 * 
 * <pre>
 * initializes the game
 * player management
 * raises fouls 
 * stores points
 * holds the reference to all the pieces in and out of the game
 * 
 * <pre>
 * @author Kapil Ratnani
 * 
 */
public class GameManager {
	public Set<Piece> blackPieces;
	public Set<Piece> whitePieces;
	public Piece queen;
	public Piece striker;

	CollisionManager collisionMgr;
	public Board board;

	// its a square board
	private final int BOARD_SIZE = 300;
	private final int NUM_CARROM_MEN = 9;
	private final int CARROM_MEN_RADIUS = 6;
	private final int STRIKER_RADIUS = 8;

	private Clock clock;

	public GameManager() {
		init();
		clock = new Clock();
	}

	private void initPieces() {
		// create pieces
		blackPieces = new HashSet<Piece>();
		Random rnd = new Random();
		for (int i = 0; i < NUM_CARROM_MEN; i++) {
			Piece piece = new Piece();
			piece.id = i + 1;
			piece.board = this.board;
			piece.pieceType = PieceType.BLACK;
			piece.color = Color.BLACK;
			piece.region = new Circle(CARROM_MEN_RADIUS,
					this.board.boundsRect.left
							+ rnd.nextInt(this.board.boundsRect.right),
					this.board.boundsRect.top
							+ rnd.nextInt(this.board.boundsRect.bottom));
			piece.velocity = new Vector2f(0, 0);
			piece.mass = 5;
			blackPieces.add(piece);
		}

		whitePieces = new HashSet<Piece>();
		for (int i = 0; i < NUM_CARROM_MEN; i++) {
			Piece piece = new Piece();
			piece.id = i + 1;
			piece.board = this.board;
			piece.pieceType = PieceType.WHITE;
			piece.color = Color.WHITE;
			piece.region = new Circle(3, rnd.nextInt(BOARD_SIZE),
					rnd.nextInt(BOARD_SIZE));
			piece.velocity = new Vector2f(0, 0);
			piece.mass = 5;
			whitePieces.add(piece);
		}

		queen = new Piece();
		queen.id = 1;
		queen.board = this.board;
		queen.pieceType = PieceType.QUEEN;
		queen.color = Color.RED;
		queen.region = new Circle(CARROM_MEN_RADIUS, board.centerCircle.x,
				board.centerCircle.y);
		queen.velocity = new Vector2f(0, 0);
		queen.mass = 5;

		striker = new Piece();
		striker.id = 1;
		striker.board = board;
		striker.pieceType = PieceType.STRIKER;
		striker.color = Color.BLUE;
		striker.region = new Circle(STRIKER_RADIUS,
				board.shootingRect[2].exactCenterX(),
				board.shootingRect[2].exactCenterY());
		striker.velocity = new Vector2f(-0.3f, -6);
		striker.mass = 15;

		collisionMgr = new CollisionManager();
		collisionMgr.addPiece(striker);
		collisionMgr.addPiece(queen);
	}

	private void init() {
		// create board;
		board = new Board(0, 0);
		initPieces();
	}

	public float update() {
		float nextCollisionTime = collisionMgr.update();
		this.striker.update(nextCollisionTime);
		this.queen.update(nextCollisionTime);
		return nextCollisionTime;
	}
}
