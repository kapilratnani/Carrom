package net.ripper;

import net.ripper.carrom.managers.GameManager;
import net.ripper.carrom.managers.GameManager.GameState;
import net.ripper.carrom.managers.clients.IGameManagerClient;
import net.ripper.carrom.model.Piece;
import net.ripper.carrom.model.components.PolarLine;
import net.ripper.carrom.renderer.RenderThread;
import net.ripper.util.Clock;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback, OnGestureListener, IGameManagerClient {
	private static final String TAG = MainGamePanel.class.getSimpleName();

	RenderThread renderThread;
	GameManager gameManager;
	Clock clock;
	Clock clock2;

	static float startX;
	static float startY;
	static float endX;
	static float endY;

	Bitmap carromBoard;
	Bitmap striker;
	Bitmap queen;
	Bitmap black;
	Bitmap white;
	Paint fpsPaint;

	GestureDetector gesture;

	PolarLine guideLine;
	Paint guidePaint;

	public MainGamePanel(Context context) {
		super(context);

		this.getHolder().addCallback(this);
		this.setFocusable(true);

		gameManager = new GameManager(2);

		renderThread = new RenderThread(this.getHolder(), this);

		clock = new Clock();
		clock2 = new Clock();
		gesture = new GestureDetector(this);

		carromBoard = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.carromboard);
		striker = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.striker);
		queen = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.queen_12);

		black = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.black_12);

		white = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.white_12);

		fpsPaint = new Paint();
		fpsPaint.setARGB(255, 255, 255, 255);

		guideLine = new PolarLine(
				this.gameManager.board.shootingRect[2].centerX(),
				this.gameManager.board.shootingRect[2].centerY(),
				this.gameManager.board.boundsRect.height(),
				(float) (235 * Math.PI / 180));

		guidePaint = new Paint();
		guidePaint.setColor(Color.BLACK);
		guidePaint.setAntiAlias(true);
		guidePaint.setPathEffect(new DashPathEffect(new float[] { 10, 10 }, 0));

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		renderThread.running = true;
		renderThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		while (true) {
			try {
				renderThread.join();
				break;
			} catch (InterruptedException e) {
			}
		}

		// try {
		// FileOutputStream outs = this.getContext().openFileOutput("pos", 0);
		// for (Piece piece : this.gameManager.blackPieces) {
		// String wstr = piece.region.x + "," + piece.region.y + "\n";
		// outs.write(wstr.getBytes());
		// }
		// for (Piece piece : this.gameManager.whitePieces) {
		// String wstr = piece.region.x + "," + piece.region.y + "\n";
		// outs.write(wstr.getBytes());
		// }
		//
		// String wstr = this.gameManager.queen.region.x + ","
		// + this.gameManager.queen.region.y + "\n";
		// outs.write(wstr.getBytes());
		//
		// outs.close();
		// } catch (Exception e) {
		//
		// e.printStackTrace();
		// }
	}

	// the fps to be displayed
	private String avgFps;

	public void setAvgFps(String avgFps) {
		this.avgFps = avgFps;
	}

	private void displayFps(Canvas canvas, String fps) {
		if (canvas != null && fps != null) {
			canvas.drawText(fps, this.getWidth() - 50, 20, fpsPaint);
		}
	}

	// Piece touchedPiece = null;

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getY() > this.getHeight() - 50) {
			renderThread.running = false;
			((Activity) this.getContext()).finish();
		}

		// for (Piece piece : this.gameManager.blackPieces) {
		// if (piece.region.isPointInCircle(e.getX(), e.getY())) {
		// touchedPiece = piece;
		// }
		// }
		//
		// for (Piece piece : this.gameManager.whitePieces) {
		// if (piece.region.isPointInCircle(e.getX(), e.getY())) {
		// touchedPiece = piece;
		// }
		// }
		//
		// if (e.getAction() == MotionEvent.ACTION_MOVE) {
		// if (touchedPiece != null) {
		// touchedPiece.region.x = e.getX();
		// touchedPiece.region.y = e.getY();
		// }
		// }
		return gesture.onTouchEvent(e);
	}

	public float update() {
		return this.gameManager.update();
	}

	@Override
	public void onDraw(Canvas canvas) {
		// draw board
		// Bitmap bmp = Bitmap.createBitmap(300, 300, Config.ARGB_8888);
		// Canvas c = new Canvas(bmp);
		canvas.drawColor(Color.BLACK);
		canvas.drawBitmap(carromBoard, 0, 0, null);

		canvas.drawBitmap(striker, this.gameManager.striker.region.x
				- this.gameManager.striker.region.radius,
				this.gameManager.striker.region.y
						- this.gameManager.striker.region.radius, null);

		if (!this.gameManager.queen.inHole) {
			canvas.drawBitmap(queen, this.gameManager.queen.region.x
					- this.gameManager.queen.region.radius,
					this.gameManager.queen.region.y
							- this.gameManager.queen.region.radius, null);
		}

		for (Piece piece : this.gameManager.blackPieces) {
			if (!piece.inHole)
				canvas.drawBitmap(black, piece.region.x - piece.region.radius,
						piece.region.y - piece.region.radius, null);
		}

		for (Piece piece : this.gameManager.whitePieces) {
			if (!piece.inHole)
				canvas.drawBitmap(white, piece.region.x - piece.region.radius,
						piece.region.y - piece.region.radius, null);
		}

		displayFps(canvas, avgFps);

		if (gameManager.gameState == GameState.STRIKER_AIMING
				|| gameManager.gameState == GameState.STRIKER_SHOT_POWER) {
			drawGuide(canvas);
		}
	}

	public void drawGuide(Canvas canvas) {
		canvas.drawLine(guideLine.originX, guideLine.originY,
				guideLine.getFinalX(), guideLine.getFinalY(), guidePaint);
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (this.gameManager.gameState == GameState.STRIKER_SHOT_POWER) {
			if (this.gameManager.striker.region.isPointInCircle(e1.getX(),
					e1.getY())) {
				// TODO move striker in the direction pointed by guide line
				float vx = Math.abs(velocityX) > 500 ? Math.signum(velocityX) * 500
						: velocityX;

				float vy = Math.abs(velocityY) > 500 ? Math.signum(velocityY) * 500
						: velocityY;
				float resultant = (float) Math.sqrt(vx * vx + vy * vy);

				vx = (float) (resultant * Math.cos(guideLine.theta));
				vy = (float) (resultant * Math.sin(guideLine.theta));

				this.gameManager.takeShot(vx / 50, vy / 50);

				this.gameManager.gameState = GameState.STRIKER_SHOT_TAKEN;
			}
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		if (this.gameManager.gameState != GameState.STRIKER_SHOT_TAKEN) {
			if (this.gameManager.striker.region.isPointInCircle(e.getX(),
					e.getY())) {
				if (this.gameManager.gameState == GameState.STRIKER_AIMING) {
					this.gameManager.gameState = GameState.STRIKER_POSITIONING;
					Log.d(TAG, "State:" + this.gameManager.gameState.toString());
				} else if (this.gameManager.gameState == GameState.STRIKER_SHOT_POWER) {
					this.gameManager.gameState = GameState.STRIKER_AIMING;
					Log.d(TAG, "State:" + this.gameManager.gameState.toString());
				}
			}
		}
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {

		if (!this.gameManager.striker.region.isPointInCircle(e1.getX(),
				e1.getY())) {

			if (this.gameManager.gameState == GameState.STRIKER_AIMING) {
				// TODO don't allow shooting backward
				float chX = e2.getX() - guideLine.originX;
				float chY = e2.getY() - guideLine.originY;
				float theta = (float) Math.atan(chY / chX);

				if (chX < 0 && chY >= 0) {
					theta = theta + (float) Math.PI;
				} else if (chX < 0 && chY < 0) {
					theta = theta - (float) Math.PI;
				}
				guideLine.rotateTo(theta);
			} else if (this.gameManager.gameState == GameState.STRIKER_POSITIONING) {
				// TODO restrict striker movement within shooting rect
				// TODO don't allow regions where a coin is present
				this.gameManager.striker.region.x = e2.getX();
				guideLine.originX = this.gameManager.striker.region.x;
			}

			return true;
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (this.gameManager.gameState != GameState.STRIKER_SHOT_TAKEN) {
			if (this.gameManager.striker.region.isPointInCircle(e.getX(),
					e.getY())) {
				if (this.gameManager.gameState == GameState.STRIKER_POSITIONING) {
					this.gameManager.gameState = GameState.STRIKER_AIMING;
					Log.d(TAG, "State:" + this.gameManager.gameState.toString());
				} else if (this.gameManager.gameState == GameState.STRIKER_AIMING) {
					this.gameManager.gameState = GameState.STRIKER_SHOT_POWER;
					Log.d(TAG, "State:" + this.gameManager.gameState.toString());
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void shotFinished() {
		// shot finished
		// reset guide position as of now
		this.guideLine.originX = this.gameManager.striker.region.x;
		this.guideLine.originY = this.gameManager.striker.region.y;
	}

}
