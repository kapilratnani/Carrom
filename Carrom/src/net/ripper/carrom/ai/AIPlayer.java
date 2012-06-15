package net.ripper.carrom.ai;

import java.util.Set;

import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.model.Board;
import net.ripper.carrom.model.Piece;

public abstract class AIPlayer {

	public int difficulty;

	public abstract Shot getShot(Set<Piece> blackPieces,
			Set<Piece> whitePieces, Piece striker, Piece queen, Board board,
			Player aiPlayer);
}
