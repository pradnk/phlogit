package com.intuit.project.phlogit;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.intuit.project.phlogit.adapter.HomeScreenGridAdapter;
import com.intuit.project.phlogit.util.Configuration;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class HomeActivity extends Activity {
	private GridView grid;
	private static final Class<?> classes[] = { PlanTripActivity2.class,
			ViewTripsActivity.class, TimelineViewActivity.class, SettingsActivity.class,
			StatisticsActivity.class, AboutActivity.class};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Remove if any notifications.
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.cancel(101);
		
		Configuration.loadProperties(this);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.home);
		//Utility.getAppSignature(this);
		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
		actionbar.setHomeLogo(R.drawable.home_logo);
		actionbar.addAction(createClickAction());
		actionbar.addAction(createCheckinAction());
		// ******** End of Action Bar configuration
		
		grid = (GridView) findViewById(R.id.gridView1);
		grid.setAdapter(new HomeScreenGridAdapter(this));

		grid.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(HomeActivity.this,classes[position]);
				startActivity(i);				
			}
		});
		
		grid.refreshDrawableState();
	}

	private Action createClickAction() {
		IntentAction clickAction = new IntentAction(this, createClickIntent(),
				R.drawable.camera_actionbar);

		return clickAction;
	}
	
	private Intent createClickIntent() {
		Intent intent = new Intent(this, SnapPictureActivity.class);		
		return intent;

	}
	
	private Action createCheckinAction() {
		IntentAction clickAction = new IntentAction(this, createCheckinIntent(),
				R.drawable.checkin);

		return clickAction;
	}
	
	private Intent createCheckinIntent() {
		Intent intent = new Intent(this, Places.class);		
		return intent;

	}
	
	@Override
	public void onConfigurationChanged(
			android.content.res.Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
}
