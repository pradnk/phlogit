package com.intuit.project.phlogit;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class AboutActivity extends Activity {

	private static final int HOUR_DISTANCE = 126;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.about);

		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
		actionbar.setHomeAction(new Action() {
			@Override
			public void performAction(View view) {
				finish();
			}

			@Override
			public int getDrawable() {
				return R.drawable.home_actionbar;
			}
		});
		// ******** End of Action Bar configuration
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
