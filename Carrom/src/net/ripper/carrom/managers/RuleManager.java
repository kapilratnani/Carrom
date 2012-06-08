package net.ripper.carrom.managers;

import java.util.List;
import java.util.Set;

import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.model.CollisionPair;
import net.ripper.carrom.model.Piece;

public abstract class RuleManager {
	protected Player[] players;

	/**
	 * Signal for passing turn to other player
	 */
	public final int PASS = 0;
	/**
	 * Signal for retaining the turn of current player
	 */
	public final int CONTINUE = 1;
	/**
	 * Signal on committing foul. i.e. Potting striker
	 */
	public final int FOUL_POTTED_STRIKER = 2;
	/**
	 * Signal on commiting foul. i.e. Potting enemy c/m
	 */
	public final int FOUL_POTTED_ENEMY_PIECE = 3;
	/**
	 * Signal when the current player wins
	 */
	public final int WIN = 4;
	/**
	 * Signal when current player looses
	 */
	public final int LOST = 5;
	
	/**
	 * Signal when the board has to be reset for the next game
	 */
	public final int RESET_GAME = 6;

	protected RuleManager(Player[] players) {
		this.players = players;
	}

	public class Result {
		public int resultFlag;
		public int black = 0;
		public int white = 0;
		public int queen = 0;
		public int nextPlayerIndex = 0;
	}

	public abstract Result getResult(int playerIndex, List<Piece> pottedPieces,
			final Set<Piece> blackPieces, final Set<Piece> whitePieces,
			final Piece queen, final Piece striker);
}
