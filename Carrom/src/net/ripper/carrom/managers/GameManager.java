package net.ripper.carrom.managers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
	private final int NUM_CARROM_MEN = 9;
	private int carromMenRadius;
	private int strikerRadius;
	private int boardSize;

	public int getBoardSize() {
		return boardSize;
	}

	public void setBoardSize(int boardSize) {
		this.boardSize = boardSize;
	}

	public int getCarromMenRadius() {
		return carromMenRadius;
	}

	public void setCarromMenRadius(int carromMenRadius) {
		this.carromMenRadius = carromMenRadius;
	}

	public int getStrikerRadius() {
		return strikerRadius;
	}

	public void setStrikerRadius(int strikerRadius) {
		this.strikerRadius = strikerRadius;
	}

	private RuleManager ruleManager;
	List<Piece> pottedPieces;
	public int currentPlayerIndex = 0;

	public Player[] players;

	private void genInitPoints(Circle queenRegion) {
		float r = queenRegion.radius;
		float cX = queenRegion.x;
		float cY = queenRegion.y;

		PointF[] c1 = new PointF[3];
		PointF[] c2 = new PointF[4];
		PointF[] c3 = new PointF[5];
		PointF[] c4 = new PointF[4];
		PointF[] c5 = new PointF[3];

		float sX = cX - android.util.FloatMath.sqrt(3) * 2 * r;
		float sY = cY - 2 * r;

		for (int i = 0; i < c1.length; i++) {
			c1[i] = new PointF(sX, sY);
			sY = sY + 2 * r;
		}

		sX = (cX - android.util.FloatMath.sqrt(3) * r);
		sY = (cY - 3 * r);
		for (int i = 0; i < c2.length; i++) {
			c2[i] = new PointF(sX, sY);
			sY = sY + 2 * r;
		}

		sX = cX;
		sY = cY - 4 * r;
		for (int i = 0; i < c3.length; i++) {
			c3[i] = new PointF(sX, sY);
			sY = sY + 2 * r;
		}

		sX = (cX + android.util.FloatMath.sqrt(3) * r);
		sY = (cY - 3 * r);
		for (int i = 0; i < c2.length; i++) {
			c4[i] = new PointF(sX, sY);
			sY = sY + 2 * r;
		}

		sX = cX + android.util.FloatMath.sqrt(3) * 2 * r;
		sY = cY - 2 * r;
		for (int i = 0; i < c1.length; i++) {
			c5[i] = new PointF(sX, sY);
			sY = sY + 2 * r;
		}

		blackPiecesInitPos = new PointF[] { c1[1], c2[0], c2[2], c2[3], c3[1],
				c4[0], c4[2], c4[3], c5[1] };
		whitePiecesInitPos = new PointF[] { c1[0], c1[2], c2[1], c3[0], c3[3],
				c3[4], c4[1], c5[0], c5[2] };
	}

	private PointF[] blackPiecesInitPos = null;

	private PointF[] whitePiecesInitPos = null;

	public enum GameState {
		STRIKER_POSITIONING, STRIKER_AIMING, STRIKER_SHOT_POWER, STRIKER_SHOT_TAKEN, FOUL_COMMITED, QUEEN_TAKEN_COVER_NEEDED
	};

	public GameState gameState = GameState.STRIKER_POSITIONING;

	public GameManager(int numPlayers, int strikerRadius, int carromMenRadius,
			int boardSize, int panelWidth, int panelHeight) {

		this.strikerRadius = strikerRadius;
		this.carromMenRadius = carromMenRadius;
		this.boardSize = boardSize;

		clients = new ArrayList<IGameManagerClient>();
		pottedPieces = new ArrayList<Piece>();

		players = new Player[numPlayers];
		for (int i = 0; i < numPlayers; i++) {
			players[i] = new Player();
			players[i].shootingRectIndex = i;
		}
		players[0].pieceType = PieceType.WHITE;
		players[1].pieceType = PieceType.BLACK;
		players[1].shootingRectIndex = Board.TOP_SHOOTING_RECT;
		players[1].aiPlayer = true;

		ruleManager = new ICFRuleManager(players);

		board = new Board((panelWidth - boardSize) / 2,
				(panelHeight - boardSize) / 2, boardSize);
		initPieces();

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
		queen = new Piece();
		queen.id = 1;
		queen.board = this.board;
		queen.pieceType = PieceType.QUEEN;
		queen.color = Color.RED;
		queen.region = new Circle(carromMenRadius, board.centerCircle.x,
				board.centerCircle.y);
		queen.velocity = new Vector2f(0, 0);
		queen.mass = 5;

		striker = new Piece();
		striker.id = 1;
		striker.board = board;
		striker.pieceType = PieceType.STRIKER;
		striker.color = Color.BLUE;
		striker.region = new Circle(strikerRadius,
				board.shootingRect[this.players[0].shootingRectIndex]
						.exactCenterX(),
				board.shootingRect[this.players[0].shootingRectIndex]
						.exactCenterY());
		striker.velocity = new Vector2f(0, 0);
		striker.mass = 10;

		genInitPoints(queen.region);

		// FIXME Temporary AI testing hardcoding
		blackPieces = new HashSet<Piece>();
		for (int i = 0; i < NUM_CARROM_MEN; i++) {
			Piece piece = new Piece();
			piece.id = i + 1;
			piece.board = this.board;
			piece.pieceType = PieceType.BLACK;
			piece.color = Color.BLACK;
			piece.region = new Circle(carromMenRadius, blackPiecesInitPos[i].x,
					blackPiecesInitPos[i].y);
			piece.velocity = new Vector2f(0, 0);
			piece.mass = 5;
			blackPieces.add(piece);
			piece.inHole = false;
		}

		whitePieces = new HashSet<Piece>();
		for (int i = 0; i < NUM_CARROM_MEN; i++) {
			Piece piece = new Piece();
			piece.id = i + 1;
			piece.board = this.board;
			piece.pieceType = PieceType.WHITE;
			piece.color = Color.WHITE;
			piece.region = new Circle(carromMenRadius, whitePiecesInitPos[i].x,
					whitePiecesInitPos[i].y);
			piece.velocity = new Vector2f(0, 0);
			piece.mass = 5;
			whitePieces.add(piece);
			piece.inHole = false;
		}

		physicsMgr = new PhysicsManager(board.boundsRect);
		physicsMgr.addPiece(striker);

		physicsMgr.addPiece(queen);
		queen.inHole = false;

		// queen.region.x = 110;

		// Piece tmp = (Piece) whitePieces.toArray()[0];
		// tmp.inHole = false;
		// physicsMgr.addPiece(tmp);
		// tmp.region.x = 100;
		// tmp.region.y = 130;
		//
		// Piece tmp = (Piece) blackPieces.toArray()[0];
		// tmp.inHole = false;
		// physicsMgr.addPiece(tmp);
		// tmp.region.x = 218;
		// tmp.region.y = 98;
		//
		// tmp = (Piece) blackPieces.toArray()[1];
		// tmp.inHole = false;
		// physicsMgr.addPiece(tmp);
		// tmp.region.x = 418;
		// tmp.region.y = 98;
		//
		// // tmp = (Piece) blackPieces.toArray()[0];
		// // tmp.inHole = false;
		// // physicsMgr.addPiece(tmp);
		// // tmp.region.x = 120;
		// // tmp.region.y = 200;
		for (Piece piece : blackPieces) {
			physicsMgr.addPiece(piece);
		}
		//
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

		if (result.resultFlag == ruleManager.FOUL_POTTED_STRIKER) {
			// collect dues
			striker.inHole = false;

			physicsMgr.addPiece(striker);
		} else if (result.resultFlag == ruleManager.FOUL_POTTED_ENEMY_PIECE) {

		} else if (result.resultFlag == ruleManager.RESET_GAME) {
			// reset board start a new game
		} else if (result.resultFlag == ruleManager.WIN) {
			// a player has won the game
		}

		Log.d(TAG, result.resultFlag + " black:" + result.black + " white:"
				+ result.white + " next:" + result.nextPlayerIndex);

		// clear potted Pieces
		pottedPieces.clear();

		// position striker to next players position
		currentPlayerIndex = result.nextPlayerIndex;
		this.striker.region.x = this.board.shootingRect[players[currentPlayerIndex].shootingRectIndex]
				.exactCenterX();
		this.striker.region.y = this.board.shootingRect[players[currentPlayerIndex].shootingRectIndex]
				.exactCenterY();
		shotFinishedNotifyClients();
		if (this.players[currentPlayerIndex].aiPlayer) {
			for (IGameManagerClient client : clients) {
				client.callAI();
			}
		}
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
