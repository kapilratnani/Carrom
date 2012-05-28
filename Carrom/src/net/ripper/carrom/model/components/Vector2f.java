package net.ripper.carrom.model.components;

public class Vector2f {
	public float x;
	public float y;

	public Vector2f(float x, float y) {
		this.x = x;
		this.y = y;
	}

	public void scale(float v) {
		x = x * v;
		y = y * v;
	}

	public float dot(Vector2f vector) {
		return vector.x * this.x + vector.y * this.y;
	}

	public float mag() {
		return (float) Math.sqrt(x * x + y * y);
	}

	public void normalize() {
		float mag = this.mag();
		x = x / mag;
		y = y / mag;
	}

	@Override
	public String toString() {
		return "< " + x + "i + " + y + "j >";
	}

	public Vector2f mulScalar(float scalar) {
		return new Vector2f(this.x * scalar, this.y * scalar);
	}

	public Vector2f sum(Vector2f vector) {
		return new Vector2f(this.x + vector.x, this.y + vector.y);
	}

	public Vector2f sub(Vector2f vector) {
		return new Vector2f(this.x - vector.x, this.y - vector.y);
	}

	public Vector2f divScalar(float scalar) {
		if (scalar == 0)
			throw new RuntimeException("Divide by zero!!");
		return new Vector2f(this.x / scalar, this.y / scalar);
	}

	public Vector2f unitVector() {
		float mag = this.mag();
		return new Vector2f(this.x / mag, this.y / mag);
	}

	//projection of w on v is given by
	// |w| (vu . wu) . vu : where vu, wu are unit vectors
	public Vector2f projectionOn(Vector2f v) {
		Vector2f vu = v.unitVector();
		return vu.mulScalar(this.mag() * vu.dot(this));
	}

}
