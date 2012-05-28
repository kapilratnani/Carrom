package net.ripper;

import net.ripper.carrom.managers.GameManager;
import net.ripper.carrom.model.components.Circle;
import net.ripper.carrom.renderer.RenderThread;
import net.ripper.util.Clock;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainGamePanel extends SurfaceView implements
		SurfaceHolder.Callback, OnGestureListener {
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
	Paint fpsPaint;

	GestureDetector gesture;

	public MainGamePanel(Context context) {
		super(context);

		this.getHolder().addCallback(this);
		this.setFocusable(true);

		gameManager = new GameManager();

		renderThread = new RenderThread(this.getHolder(), this);

		clock = new Clock();
		clock2 = new Clock();
		gesture = new GestureDetector(this);

		carromBoard = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.carromboard);
		striker = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.striker);
		queen = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.queen);

		fpsPaint = new Paint();
		fpsPaint.setARGB(255, 255, 255, 255);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		// TODO Auto-generated method stub

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

	@Override
	public boolean onTouchEvent(MotionEvent e) {
		if (e.getY() > this.getHeight() - 50) {
			renderThread.running = false;
			((Activity) this.getContext()).finish();
		}
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

		canvas.drawBitmap(queen, this.gameManager.queen.region.x
				- this.gameManager.queen.region.radius,
				this.gameManager.queen.region.y
						- this.gameManager.queen.region.radius, null);

		displayFps(canvas, avgFps);
	}

	@Override
	public boolean onDown(MotionEvent e) {

		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (this.gameManager.striker.region.isPointInCircle(e1.getX(),
				e1.getY())) {
			float vx = Math.abs(velocityX) > 400 ? Math.signum(velocityX) * 400
					: velocityX;

			float vy = Math.abs(velocityY) > 400 ? Math.signum(velocityY) * 500
					: velocityY;
			this.gameManager.striker.velocity.x = vx / 50;
			this.gameManager.striker.velocity.y = vy / 50;
		}
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

}
