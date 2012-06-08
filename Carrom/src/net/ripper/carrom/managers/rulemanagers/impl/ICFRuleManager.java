package net.ripper.carrom.managers.rulemanagers.impl;

import java.util.List;
import java.util.Set;

import net.ripper.carrom.managers.RuleManager;
import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.model.CollisionPair;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.Piece.PieceType;

/**
 * This class implements the rule manager based on Indian Carrom Federation
 * rules
 * 
 * @author theripper
 * 
 */
public class ICFRuleManager extends RuleManager {

	boolean breakDone = false;
	boolean scoredQueen = false;
	int breakRetryCount = 0;

	int numTotalBlackPottedPieces = 0;
	int numTotalWhitePottedPieces = 0;

	private Result result = null;
	private int numCurrentBlackPieces = 0;
	private int numCurrentWhitePieces = 0;
	private boolean strikerPotted = false;
	private boolean queenPotted = false;

	public ICFRuleManager(Player[] players) {
		super(players);
	}

	@Override
	public Result getResult(int playerIndex, List<Piece> pottedPieces,
			Set<Piece> blackPieces, Set<Piece> whitePieces, Piece queen,
			Piece striker) {

		result = new Result();
		numCurrentBlackPieces = 0;
		numCurrentWhitePieces = 0;
		strikerPotted = false;
		queenPotted = false;
		Player curPlayer = players[playerIndex];

		if (!breakDone) {
			// if it is the first shot
			// check for any collision with the c/m
			// if no collision, increment breakRetryCount, max 3
			// if no break is done in 3 tries, move the chance to next player
			// if break is done set breadDone to true
		}

		// iterate through each pottedPiece
		// and get number of potted pieces of each type
		for (Piece pottedPiece : pottedPieces) {
			if (pottedPiece.pieceType == PieceType.BLACK)
				numCurrentBlackPieces++;
			if (pottedPiece.pieceType == PieceType.WHITE)
				numCurrentWhitePieces++;
			if (pottedPiece.pieceType == PieceType.STRIKER)
				strikerPotted = true;
			if (pottedPiece.pieceType == PieceType.QUEEN)
				queenPotted = true;
		}

		// if striker is potted
		// decide action on foul
		if (strikerPotted) {
			result.resultFlag = this.FOUL_POTTED_STRIKER;
			// if potted only striker
			if (numCurrentBlackPieces == 0 && numCurrentWhitePieces == 0
					&& queenPotted == false) {
				// turn lost and return one piece

				// check players piecetype
				if (curPlayer.pieceType == PieceType.BLACK) {
					// check if player has scored any
					if (numTotalBlackPottedPieces > 0) {
						numTotalBlackPottedPieces--;
						result.black = -1;
					} else {
						// player has not scored any
						// make the score negative, will collect when she scores
						numTotalBlackPottedPieces--;
					}
				} else {
					// check if player has scored any
					if (numTotalWhitePottedPieces > 0) {
						numTotalWhitePottedPieces--;
						result.white = -1;
					} else {
						// player has not scored any
						// make the score negative, will collect when she scores
						numTotalWhitePottedPieces--;
					}
				}

				// turn lost
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			} else {
				// if potted own c/m with striker potted
				// turn not lost, return number of potted plus 1
				// if black is potted
				if (numCurrentBlackPieces > 0) {
					// update number of black pieces potted
					numTotalBlackPottedPieces += numCurrentBlackPieces;
					// if own piece
					if (curPlayer.pieceType == PieceType.BLACK) {
						// return all potted plus 1 due
						if (numTotalBlackPottedPieces > numCurrentBlackPieces + 1) {
							// the player has enough to be deducted
							numTotalBlackPottedPieces -= (numCurrentBlackPieces + 1);
							result.black = -(numCurrentBlackPieces + 1);
						} else {
							// player doesn't have enough
							// take as much as possible and make score negative
							result.black = -numTotalBlackPottedPieces;
							// take these when available
							numTotalBlackPottedPieces = numTotalBlackPottedPieces
									- numCurrentBlackPieces;
						}
						// retain turn
						result.nextPlayerIndex = playerIndex;
					} else {
						// black is not own piece
						// turn lost, c/m will remain potted
						result.nextPlayerIndex = getNextPlayer(playerIndex);
					}
				}

				// if white is potted with striker
				if (numCurrentWhitePieces > 0) {
					// update number of white pieces potted
					numTotalWhitePottedPieces += numCurrentWhitePieces;
					// if own piece
					if (curPlayer.pieceType == PieceType.WHITE) {
						// return all potted plus 1 due
						if (numTotalWhitePottedPieces > numCurrentWhitePieces + 1) {
							// the player has enough to be deducted
							numTotalWhitePottedPieces -= (numCurrentWhitePieces + 1);
							result.white = -(numCurrentWhitePieces + 1);
						} else {
							// player doesn't have enough
							// take as much as possible and make score negative
							result.white = -numTotalWhitePottedPieces;
							// take these when available, keep the number
							// negative
							numTotalWhitePottedPieces = numTotalWhitePottedPieces
									- numCurrentWhitePieces;
						}
						// retain turn
						result.nextPlayerIndex = playerIndex;
					} else {
						// white is not own piece
						// turn lost, c/m will remain potted
						result.nextPlayerIndex = getNextPlayer(playerIndex);
					}
				}

				if (queenPotted) {
					// if queen is potted with striker
					// turn lost, if own piece is not potted
					// potting enemy/own piece with striker is already covered
					// above

					// return queen
					result.queen = -1;
					if (numCurrentBlackPieces == 0
							&& numCurrentWhitePieces == 0) {
						// turn lost
						result.nextPlayerIndex = getNextPlayer(playerIndex);
					}
				} else {

					/**
					 * While covering the Queen by a proper stroke, if a player
					 * pockets the striker along with his C/m, the C/m so
					 * pocketed plus one C/m as Due shall be taken out for
					 * placing by the opponent. The player shall however,
					 * continue his turn. If in that subsequent stroke no C/m of
					 * the player is pocketed, the Queen shall not be considered
					 * to have been covered and it shall be taken out for
					 * placing.
					 */
					// due to above comment, do nothing here
					// if (!this.scoredQueen && queen.inHole) {
					// // return queen
					// result.queen = -1;
					// }
				}
			}
			// return the result
			return result;
		}

		// on potting enemy c/m
		// turn is lost and no due
		// but, if the potted c/m is the last one for the enemy, then foul

		// if black is potted
		if (numCurrentBlackPieces > 0) {
			// if own c/m, retain turn
			if (curPlayer.pieceType == PieceType.BLACK) {
				// check for previous due
				// return c/m, if any
				if (numTotalBlackPottedPieces < 0) {
					// check if player has enough to be deducted
					if (numTotalBlackPottedPieces + numCurrentBlackPieces >= 0) {
						// player has enough to be deducted
						result.black = numTotalBlackPottedPieces;
					} else {
						// doesn't have enough
						// take whatever received
						result.black = -numCurrentBlackPieces;
					}
				} else {
					// no previous due
					result.black = numCurrentBlackPieces;
				}

				result.resultFlag = this.CONTINUE;
				result.nextPlayerIndex = playerIndex;

			} else {
				// black is not own piece
				// turn lost, c/m remains potted
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			}

			// update num Black potted
			numTotalBlackPottedPieces += numCurrentBlackPieces;
			// check if it is the last piece
			if (numTotalBlackPottedPieces == 9) {
				// black has finished
				// call decideGame
			}
		}

		// if white is potted
		if (numCurrentWhitePieces > 0) {
			// if own c/m, retain turn
			if (curPlayer.pieceType == PieceType.WHITE) {
				// check for previous due
				// return c/m, if any
				if (numTotalWhitePottedPieces < 0) {
					// check if player has enough to be deducted
					if (numTotalWhitePottedPieces + numCurrentWhitePieces >= 0) {
						// player has enough to be deducted
						result.white = numTotalWhitePottedPieces;
					} else {
						// doesn't have enough
						// take whatever received
						result.white = -numCurrentWhitePieces;
					}
				} else {
					// no previous due
					result.white = numCurrentWhitePieces;
				}

				result.resultFlag = this.CONTINUE;
				result.nextPlayerIndex = playerIndex;

			} else {
				// white is not own piece
				// turn lost, c/m remains potted
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			}

			// update num White potted
			numTotalWhitePottedPieces += numCurrentWhitePieces;
			// check if it is the last piece
			if (numTotalWhitePottedPieces == 9) {
				// white has finished
				// call decideGame
			}
		}

		// check for potting queen and queen waiting for cover
		if (queenPotted) {
			// if queen is potted, along with own piece, then the queen is
			// scored
			// else wait for cover
			if ((curPlayer.pieceType == PieceType.BLACK && numCurrentBlackPieces > 0)
					|| (curPlayer.pieceType == PieceType.WHITE && numCurrentWhitePieces > 0)) {
				curPlayer.scoredQueen = true;
				result.queen = 1;
				this.scoredQueen = true;
			}

			// retain turn
			result.nextPlayerIndex = playerIndex;
			result.resultFlag = this.CONTINUE;
			// wait for cover
		} else {
			// this means it is waiting for cover
			if (!scoredQueen && queen.inHole) {
				if ((curPlayer.pieceType == PieceType.BLACK && numCurrentBlackPieces > 0)
						|| (curPlayer.pieceType == PieceType.WHITE && numCurrentWhitePieces > 0)) {
					// queen scored with the cover
					curPlayer.scoredQueen = true;
					result.nextPlayerIndex = playerIndex;
					result.resultFlag = this.CONTINUE;
					this.scoredQueen = true;
				} else {
					// cover not scored, return queen
					result.queen = -1;
					// turn lost
					result.nextPlayerIndex = getNextPlayer(playerIndex);
					result.resultFlag = this.PASS;
				}
			}
		}

		return result;
	}

	private int getNextPlayer(int currentPlayerIndex) {
		return (currentPlayerIndex + 1) % this.players.length;
	}

}
