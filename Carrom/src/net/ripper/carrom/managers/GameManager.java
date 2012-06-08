package net.ripper.carrom.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import net.ripper.carrom.managers.RuleManager.Result;
import net.ripper.carrom.managers.clients.IGameManagerClient;
import net.ripper.carrom.managers.clients.IPhysicsManagerClient;
import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.managers.physics.collisionResolver.CustomCollisionResolver;
import net.ripper.carrom.managers.physics.collisionResolver.client.ICustomCollissionResolverClient;
import net.ripper.carrom.managers.rulemanagers.impl.ICFRuleManager;
import net.ripper.carrom.model.Board;
import net.ripper.carrom.model.CollisionPair;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.Piece.PieceType;
import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.model.components.Vector2f;
import net.ripper.util.Clock;
import net.ripper.util.UtilityFunctions;
import android.graphics.Color;
import android.graphics.PointF;
import android.util.Log;

/**
 * This class:
 * 
 * <pre>
 * initializes the game
 * player management
 * stores points
 * holds the reference to all the pieces in and out of the game
 * 
 * <pre>
 * @author Kapil Ratnani
 * 
 */
public class GameManager implements IPhysicsManagerClient,
		ICustomCollissionResolverClient {
	private static final String TAG = GameManager.class.getSimpleName();
	public Set<Piece> blackPieces;
	public Set<Piece> whitePieces;
	public Piece queen;
	public Piece striker;

	List<IGameManagerClient> clients;

	PhysicsManager physicsMgr;
	public Board board;

	// its a square board
	private final int BOARD_SIZE = 300;
	private final int NUM_CARROM_MEN = 9;
	private final int CARROM_MEN_RADIUS = 6;
	private final int STRIKER_RADIUS = 8;

	private RuleManager ruleManager;
	List<Piece> pottedPieces;
	private int currentPlayerIndex = 0;

	Player[] players;

	private PointF[] blackPiecesInitPos = new PointF[] { new PointF(169, 139),
			new PointF(152, 128), new PointF(139, 150), new PointF(157, 161),
			new PointF(133, 160), new PointF(157, 139), new PointF(151, 171),
			new PointF(132, 138), new PointF(169, 161) };

	private PointF[] whitePiecesInitPos = new PointF[] { new PointF(163, 149),
			new PointF(145, 161), new PointF(144, 138), new PointF(174, 150),
			new PointF(138, 170), new PointF(127, 149), new PointF(164, 129),
			new PointF(139, 127), new PointF(163, 171) };

	private Clock clock;

	public enum GameState {
		STRIKER_POSITIONING, STRIKER_AIMING, STRIKER_SHOT_POWER, STRIKER_SHOT_TAKEN, FOUL_COMMITED, QUEEN_TAKEN_COVER_NEEDED
	};

	public GameState gameState = GameState.STRIKER_POSITIONING;

	public GameManager(int numPlayers) {
		init();
		clock = new Clock();
		clients = new ArrayList<IGameManagerClient>();
		pottedPieces = new ArrayList<Piece>();

		players = new Player[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			players[i] = new Player();
		}

		ruleManager = new ICFRuleManager(players);
	}

	private void shotFinishedNotifyClients() {
		for (IGameManagerClient client : clients) {
			client.shotFinished();
		}
	}

	public void registerClient(IGameManagerClient client) {
		clients.add(client);
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
					blackPiecesInitPos[i].x, blackPiecesInitPos[i].y);
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
			piece.region = new Circle(CARROM_MEN_RADIUS,
					whitePiecesInitPos[i].x, whitePiecesInitPos[i].y);
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
		striker.velocity = new Vector2f(0, 0);
		striker.mass = 10;

		physicsMgr = new PhysicsManager(board.boundsRect);
		physicsMgr.addPiece(striker);
		physicsMgr.addPiece(queen);
		for (Piece piece : blackPieces) {
			physicsMgr.addPiece(piece);
		}

		for (Piece piece : whitePieces) {
			physicsMgr.addPiece(piece);
		}
		physicsMgr.registerClient(this);

		// add holes as pieces
		for (Circle hole : this.board.holes) {
			Piece piece = new Piece();
			piece.region = hole;
			physicsMgr.addCustomCollisionResolverForPiece(piece,
					new HoleCoinCollisionResolver(this));
		}

	}

	private void init() {
		// create board;
		board = new Board(0, 0);
		initPieces();
	}

	public float update() {
		float nextCollisionTime = physicsMgr.update();
		return nextCollisionTime;
	}

	@Override
	public void allMotionStopped(List<CollisionPair> collisionPairs) {
		physicsMgr.setPaused(true);
		this.gameState = GameState.STRIKER_POSITIONING;

		RuleManager.Result result = ruleManager.getResult(currentPlayerIndex,
				pottedPieces, blackPieces, whitePieces, queen, striker);
		// processResult

		// clear potted Pieces
		pottedPieces.clear();

		// position striker to next players position
		currentPlayerIndex = result.nextPlayerIndex;
		this.striker.region.x = this.board.shootingRect[2].exactCenterX();
		this.striker.region.y = this.board.shootingRect[2].exactCenterY();
		shotFinishedNotifyClients();
	}

	public void takeShot(float vx, float vy) {
		physicsMgr.setPaused(false);
		striker.velocity.x = vx;
		striker.velocity.y = vy;
	}

	@Override
	/**
	 * Custom collision handling for collision between a hole
	 * and striker,black or white
	 * basically this will tell when a carrom-men is potted
	 */
	public void collisionHappened(Piece pieceA, Piece pieceB) {
		// carrom-men potted
		/**
		 * Rule manager will decide the actions to be taken
		 * 
		 */

		// for now just remove the carrom-men from play
		if (pieceA.pieceType == PieceType.BLACK
				|| pieceA.pieceType == PieceType.WHITE
				|| pieceA.pieceType == PieceType.QUEEN
				|| pieceA.pieceType == PieceType.STRIKER) {
			removePiece(pieceA);
			pottedPieces.add(pieceA);
			Log.d(TAG, pieceA.toString() + " potted.");
		}

		if (pieceB.pieceType == PieceType.BLACK
				|| pieceB.pieceType == PieceType.WHITE
				|| pieceB.pieceType == PieceType.QUEEN
				|| pieceB.pieceType == PieceType.STRIKER) {
			removePiece(pieceB);
			pottedPieces.add(pieceB);
			Log.d(TAG, pieceB.toString() + " potted.");
		}

	}

	private void removePiece(Piece piece) {
		physicsMgr.removePiece(piece);
		piece.inHole = true;
	}
}

class HoleCoinCollisionResolver extends CustomCollisionResolver {

	public HoleCoinCollisionResolver(ICustomCollissionResolverClient client) {
		super(client);
	}

	@Override
	public void resolveCollision(Piece pieceA, Piece pieceB) {
		float sqDistance = UtilityFunctions.euclideanSqDistance(
				pieceA.region.x, pieceA.region.y, pieceB.region.x,
				pieceB.region.y);
		// basically it finds out when the coin is almost half
		// in the hole
		if (pieceA.region.radius > pieceB.region.radius) {
			// pieceA is the hole
			if (sqDistance < pieceA.region.radius * pieceA.region.radius)
				notifyClient(pieceA, pieceB);
		} else {
			// pieceB is the hole
			if (sqDistance < pieceB.region.radius * pieceB.region.radius)
				notifyClient(pieceA, pieceB);
		}

	}
}
