package net.ripper.carrom.model.components;

/**
 * line with polar coordinates
 * 
 * @author theripper
 * 
 */
public class PolarLine {
	public float originX;
	public float originY;
	public float r;
	public float theta;
	private float finalX;
	private float finalY;

	/**
	 * 
	 * @param originX
	 * @param originY
	 * @param r
	 * @param theta
	 *            in radians
	 */
	public PolarLine(float originX, float originY, float r, float theta) {
		this.originX = originX;
		this.originY = originY;
		this.r = r;
		this.theta = theta;
		this.finalX = (float) (originX + r * Math.cos(theta));
		this.finalY = (float) (originY + r * Math.sin(theta));
	}

	public float getFinalX() {
		return finalX;
	}

	public float getFinalY() {
		return finalY;
	}

	/**
	 * 
	 * @param theta
	 *            in radians
	 */
	public void rotateTo(float theta) {
		this.theta = theta;
		this.finalX = (float) (originX + r * Math.cos(theta));
		this.finalY = (float) (originY + r * Math.sin(theta));
	}

	/**
	 * 
	 * @param theta
	 *            in radians
	 */
	public void rotateBy(float inctheta) {
		this.theta += inctheta;
		this.finalX = (float) (originX + r * Math.cos(this.theta));
		this.finalY = (float) (originY + r * Math.sin(this.theta));
	}
}
