package net.ripper.carrom.managers.model;

import net.ripper.carrom.model.Piece.PieceType;

public class Player {
	public int id;
	public PieceType pieceType;
	public boolean scoredQueen;
	public int points;
	public int shootingRectIndex;
	public boolean aiPlayer;
}
