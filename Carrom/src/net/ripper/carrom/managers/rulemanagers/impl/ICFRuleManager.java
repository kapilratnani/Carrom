package net.ripper.carrom.managers.rulemanagers.impl;

import java.util.List;
import java.util.Set;

import net.ripper.carrom.managers.RuleManager;
import net.ripper.carrom.managers.model.Player;
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
	private Player curPlayer;
	private Piece queen;
	private int currentPlayerIndex;
	private int numMaxGames = 3;
	private int numGameCount = 0;

	public ICFRuleManager(Player[] players) {
		super(players);
	}

	public int getNumGames() {
		return numMaxGames;
	}

	public void setNumGames(int numGames) {
		this.numMaxGames = numGames;
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
		curPlayer = players[playerIndex];
		this.queen = queen;
		this.currentPlayerIndex = playerIndex;

		// if (!breakDone) {
		// // if it is the first shot
		// // check for any collision with the c/m
		// // if no collision, increment breakRetryCount, max 3
		// // if no break is done in 3 tries, move the chance to next player
		// // if break is done set breadDone to true
		// }

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
						// check if it is last piece of the opponent
						if (numTotalBlackPottedPieces == 9) {
							// black has finished
							// call decide game
							// update white pieces potted
							numTotalWhitePottedPieces += numCurrentWhitePieces;
							return decideGame(PieceType.BLACK, 0);
						}
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

						// check if it is last piece of the opponent
						if (numTotalWhitePottedPieces == 9) {
							// white has finished
							// call decide game
							// no need to update black pieces count as it has
							// been checked above
							return decideGame(PieceType.WHITE, 0);
						}
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
				result.resultFlag = this.FOUL_POTTED_ENEMY_PIECE;
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			}

			// update num Black potted
			numTotalBlackPottedPieces += numCurrentBlackPieces;
			// check if it is the last piece
			if (numTotalBlackPottedPieces == 9) {
				// black has finished
				// call decideGame
				// game will end here, update white potted pieces
				numTotalWhitePottedPieces += numCurrentWhitePieces;
				return decideGame(PieceType.BLACK, numCurrentWhitePieces);
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

				// need to check this, in case a foul is already raised while
				// checking for black pieces
				if (result.resultFlag != this.FOUL_POTTED_ENEMY_PIECE) {
					result.resultFlag = this.CONTINUE;
					result.nextPlayerIndex = playerIndex;
				}
			} else {
				// white is not own piece
				// turn lost, c/m remains potted
				result.resultFlag = this.FOUL_POTTED_ENEMY_PIECE;
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			}

			// update num White potted
			numTotalWhitePottedPieces += numCurrentWhitePieces;
			// check if it is the last piece
			if (numTotalWhitePottedPieces == 9) {
				// white has finished
				// call decideGame
				return decideGame(PieceType.WHITE, numCurrentBlackPieces);
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
				// retain turn
				result.nextPlayerIndex = playerIndex;
				result.resultFlag = this.CONTINUE;
			} else if ((curPlayer.pieceType == PieceType.BLACK && numCurrentWhitePieces > 0)
					|| (curPlayer.pieceType == PieceType.WHITE && numCurrentBlackPieces > 0)) {
				// queen was potted with an enemy piece
				// return queen
				result.queen = -1;
				result.nextPlayerIndex = getNextPlayer(playerIndex);
			} else {
				result.nextPlayerIndex = playerIndex;
				result.resultFlag = this.CONTINUE;
			}
			// wait for cover
		} else {
			// this means waiting for cover
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

		// nothing happened move to other player
		if (numCurrentBlackPieces == 0 && numCurrentWhitePieces == 0
				&& !queenPotted) {
			result.nextPlayerIndex = getNextPlayer(playerIndex);
		}

		return result;
	}

	private int getNextPlayer(int currentPlayerIndex) {
		return (currentPlayerIndex + 1) % this.players.length;
	}

	/**
	 * called when black or white finishes, calculates point and declares winner
	 * 
	 * @return Result object with result flag RESET_GAME:to signal the start of
	 *         next frame and result flag WIN with winner's piece set to 1 and
	 *         scores updated in the players object
	 */
	private Result decideGame(PieceType pieceFinished, int numAdditionalPiece) {
		// check if the queen cover scored in the last shot
		Result result = new Result();
		if (!scoredQueen && queen.inHole && !strikerPotted) {
			if ((curPlayer.pieceType == PieceType.BLACK && numCurrentBlackPieces > 0)
					|| (curPlayer.pieceType == PieceType.WHITE && numCurrentWhitePieces > 0)) {
				// queen scored with the cover
				curPlayer.scoredQueen = true;
				this.scoredQueen = true;
			}
		}

		if (strikerPotted) {
			// calculate due and update number of pieces on board

			if (numCurrentBlackPieces > 0) {
				// if own piece
				if (curPlayer.pieceType == PieceType.BLACK) {
					// return all potted plus 1 due
					if (numTotalBlackPottedPieces > numCurrentBlackPieces + 1) {
						// the player has enough to be deducted
						numTotalBlackPottedPieces -= (numCurrentBlackPieces + 1);
					} else {
						// player doesn't have enough
						// take as much as possible and make score negative
						// take these when available
						numTotalBlackPottedPieces = numTotalBlackPottedPieces
								- numCurrentBlackPieces;
					}

				}
			}

			if (numCurrentWhitePieces > 0) {
				// if own piece
				if (curPlayer.pieceType == PieceType.WHITE) {
					// return all potted plus 1 due
					if (numTotalBlackPottedPieces > numCurrentBlackPieces + 1) {
						// the player has enough to be deducted
						numTotalBlackPottedPieces -= (numCurrentBlackPieces + 1);
					} else {
						// player doesn't have enough
						// take as much as possible and make score negative
						// take these when available
						numTotalBlackPottedPieces = numTotalBlackPottedPieces
								- numCurrentBlackPieces;
					}
				}
			}
		}

		// calculate points
		int points = 0;
		if (scoredQueen) {
			PieceType queenScorerType = null;
			for (Player player : this.players) {
				if (player.scoredQueen) {
					queenScorerType = player.pieceType;
					break;
				}
			}

			if (pieceFinished == PieceType.BLACK) {
				// black wins
				points = 9 - numTotalWhitePottedPieces
						+ (queenScorerType == PieceType.BLACK ? 3 : 0)
						+ numAdditionalPiece;

				for (Player player : this.players) {
					if (player.pieceType == PieceType.BLACK) {

						if (player.points >= 22
								&& queenScorerType == PieceType.BLACK) {
							player.points += (points - 2);
							// give only 1 point for queen in case
							// of points greater than 22
						} else {
							player.points += points;
						}
					}
				}
			} else {
				// white wins
				points = 9 - numTotalBlackPottedPieces
						+ (queenScorerType == PieceType.WHITE ? 3 : 0)
						+ numAdditionalPiece;

				for (Player player : this.players) {
					if (player.pieceType == PieceType.WHITE) {

						if (player.points >= 22
								&& queenScorerType == PieceType.WHITE) {
							player.points += (points - 2);
							// give only 1 point for queen in case
							// of points greater than 22
						} else {
							player.points += points;
						}
					}
				}
			}
		} else {
			// this happens when the player pots the last piece of the opponent
			// or own
			// with queen still on board
			if (pieceFinished == PieceType.BLACK) {
				// white wins

				points = 9 - numTotalBlackPottedPieces + 3;

				for (Player player : this.players) {
					if (player.pieceType == PieceType.WHITE) {
						if (player.points >= 22) {
							player.points += points - 2;
							// grant only 1 point
						} else {
							player.points += points;
						}
					}
				}

			} else {
				// black wins

				points = 9 - numTotalWhitePottedPieces + 3;

				for (Player player : this.players) {
					if (player.pieceType == PieceType.BLACK) {
						if (player.points >= 22) {
							player.points += points - 2;
							// grant only 1 point
						} else {
							player.points += points;
						}
					}
				}

			}
		}

		numGameCount++;
		// check if all games are over
		if (numGameCount == numMaxGames) {
			int blackPoints = 0;
			int whitePoints = 0;
			for (Player player : this.players) {
				if (player.pieceType == PieceType.BLACK) {
					blackPoints += player.points;
				} else {
					whitePoints += player.points;
				}
			}

			if (blackPoints > whitePoints) {
				result.black = 1;
			} else {
				result.white = 1;
			}
			result.resultFlag = WIN;
		} else {
			// start next game
			result.resultFlag = RESET_GAME;
		}

		result.nextPlayerIndex = getNextPlayer(currentPlayerIndex);
		return result;
	}
}
