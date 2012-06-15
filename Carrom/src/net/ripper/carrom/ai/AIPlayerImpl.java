package net.ripper.carrom.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.ripper.carrom.managers.model.Player;
import net.ripper.carrom.model.Board;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.Piece.PieceType;
import net.ripper.carrom.model.components.Circle;
import android.graphics.Rect;
import android.graphics.RectF;

public class AIPlayerImpl extends AIPlayer {

	public List<RectF> rects = new ArrayList<RectF>();

	@Override
	public Shot getShot(Set<Piece> blackPieces, Set<Piece> whitePieces,
			Piece striker, Piece queen, Board board, Player aiPlayer) {
		Circle[] holes = board.holes;

		rects = new ArrayList<RectF>();
		List<Piece> pottablePieces = new ArrayList<Piece>();

		if (aiPlayer.pieceType == PieceType.BLACK) {
			for (Piece black : blackPieces) {
				if (!black.inHole) {
					pottablePieces.add(black);
				}
			}
		} else {
			for (Piece white : whitePieces) {
				if (!white.inHole) {
					pottablePieces.add(white);
				}
			}
		}

		// for each pottable c/m make a rect connectent the hole and cm
		for (Piece cm : pottablePieces) {
			for (int i = 0; i < holes.length; i++) {
				rects.add(makeRectFromLine(cm.region.x, cm.region.y,
						holes[i].x, holes[i].y, 2 * cm.region.radius));
			}
		}

		return null;
	}

	private RectF makeRectFromLine(float x1, float y1, float x2, float y2,
			float width) {
		// get two points perpendicularly away from the line by width/2
		// one point from (x1,y1) and other from (x2,y2)

		// get the slope angle of line
		float chX = x2 - x1;
		float chY = y2 - y1;
		float theta = (float) Math.atan(chY / chX);

		// check in which quadrant the point is
		if (chX < 0 && chY >= 0) {
			// 2nd quadrant
			theta = theta + (float) Math.PI;
		} else if (chX < 0 && chY < 0) {
			// 3rd quadrant
			theta = theta - (float) Math.PI;
		}

		float x3 = (float) (x1 - (width / 2) * Math.cos(theta - Math.PI / 2));
		float y3 = (float) (y1 - (width / 2) * Math.sin(theta - Math.PI / 2));

		float x4 = (float) (x2 + (width / 2) * Math.cos(theta - Math.PI / 2));
		float y4 = (float) (y2 + (width / 2) * Math.sin(theta - Math.PI / 2));

		RectF rect = new RectF(x3, y3, x4, y4);

		return rect;
	}
	
	
}
