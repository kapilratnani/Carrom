package net.ripper.carrom.model;

import net.ripper.carrom.model.components.Circle;
import android.graphics.Rect;

/**
 * this class represents the carrom board. Describes the holes in the corner,
 * the circle in the center, shooting regions(which are basically rectangles)
 * bounds of the board and the friction on the surface of the board.
 * 
 * The Renderer uses this class to draw the board. The Collision manager uses
 * this class to check collisions with the the holes and walls
 * 
 * @author Kapil Ratnani
 * 
 */
public class Board {
	/**
	 * Represent the 4 holes in the corner
	 */
	public Circle[] holes;
	public Circle centerCircle;
	public Rect[] shootingRect;
	public Rect boundsRect;
	public float damping;

	private final float HOLE_RADIUS = 10.0f;
	private final float CENTER_CIRCLE_RADIUS = 41.0f;
	private final float BOARD_SIZE = 300;
	private final float BOARD_OFFSET = 18;

	private final int TOP_SHOOTING_RECT = 2;
	private final int RIGHT_SHOOTING_RECT = 1;
	private final int BOTTOM_SHOOTING_RECT = 0;
	private final int LEFT_SHOOTING_RECT = 3;

	private int posXOffset = 0;
	private int posYOffset = 0;

	// board
	// 300X300
	// bounds 264X264
	// center circle 82X82
	// holes 20x20
	private void init() {
		holes = new Circle[4];
		// top left
		holes[0] = new Circle(HOLE_RADIUS, posXOffset + BOARD_OFFSET
				+ HOLE_RADIUS, posYOffset + BOARD_OFFSET + HOLE_RADIUS);
		// top right
		holes[1] = new Circle(HOLE_RADIUS, BOARD_SIZE - HOLE_RADIUS
				- BOARD_OFFSET + posXOffset, HOLE_RADIUS + BOARD_OFFSET
				+ posYOffset);
		// bottom right
		holes[2] = new Circle(HOLE_RADIUS, BOARD_SIZE - HOLE_RADIUS
				- BOARD_OFFSET + posXOffset, BOARD_SIZE - HOLE_RADIUS
				- BOARD_OFFSET + posYOffset);
		// bottom left
		holes[3] = new Circle(HOLE_RADIUS, posXOffset + BOARD_OFFSET
				+ HOLE_RADIUS, BOARD_SIZE - HOLE_RADIUS - BOARD_OFFSET
				+ posYOffset);

		// init center Circle
		centerCircle = new Circle(CENTER_CIRCLE_RADIUS, BOARD_SIZE / 2,
				BOARD_SIZE / 2);

		// shooting rects
		// left 56,86,73,213
		// top 88,56,212,70
		// right 228,86,243,213
		// bottom 88,228,212.244
		shootingRect = new Rect[4];
		// top
		shootingRect[TOP_SHOOTING_RECT] = new Rect(posXOffset + 88,
				posYOffset + 56, posXOffset + 212, posYOffset + 70);

		// right
		shootingRect[RIGHT_SHOOTING_RECT] = new Rect(posXOffset + 228,
				posYOffset + 86, posXOffset + 243, posYOffset + 213);
		// bottom
		shootingRect[BOTTOM_SHOOTING_RECT] = new Rect(posXOffset + 88,
				posYOffset + 228, posXOffset + 212, posYOffset + 244);
		// left
		shootingRect[LEFT_SHOOTING_RECT] = new Rect(posXOffset + 56,
				posYOffset + 86, posXOffset + 73, posYOffset + 213);

		boundsRect = new Rect((int) (posXOffset + BOARD_OFFSET),
				(int) (posYOffset + BOARD_OFFSET), (int) (BOARD_SIZE
						- BOARD_OFFSET + posXOffset), (int) (BOARD_SIZE
						- BOARD_OFFSET + posYOffset));

		damping = 0.25F;
	}

	public Board(int posXOffset, int posYOffset) {
		this.posXOffset = posXOffset;
		this.posYOffset = posYOffset;
		init();
	}

}
