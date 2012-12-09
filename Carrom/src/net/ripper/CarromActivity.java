package net.ripper;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;

public class CarromActivity extends Activity {

	private static final String TAG = CarromActivity.class.getSimpleName();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		MainGamePanel.PANEL_HEIGHT=this.getWindowManager().getDefaultDisplay().getHeight();
		MainGamePanel.PANEL_WIDTH=this.getWindowManager().getDefaultDisplay().getWidth();
		setContentView(new MainGamePanel(this));
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		//ignore config changes
		super.onConfigurationChanged(newConfig);
	}
	
}