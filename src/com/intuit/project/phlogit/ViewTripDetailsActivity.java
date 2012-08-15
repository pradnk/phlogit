package com.intuit.project.phlogit;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.intuit.project.phlogit.adapter.ViewTripDetailsGridAdapter;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.service.SyncService;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class ViewTripDetailsActivity extends Activity {
	private GridView grid;
	private ArrayList<String> actualPhotos = new ArrayList<String>();
	private String photoParentId;
	private ActionBar actionbar;
	private ArrayList<String> galleryIds = new ArrayList<String>();
	private ArrayList<String> facebookIds = new ArrayList<String>();
	private ArrayList<String> synced = new ArrayList<String>();
	private ViewGroup emptyView;
	private String startDate;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_trip_details);

		grid = (GridView)findViewById(R.id.details);
		grid.setScrollingCacheEnabled(false);
		
		emptyView = (ViewGroup)findViewById(R.id.emptyView);
		
		photoParentId = getIntent().getStringExtra("ID");
		startDate = getIntent().getStringExtra("START_DATE");
		
		ArrayList<File> photos = new ArrayList<File>();
		if(!"-1".equals(photoParentId)) {
			Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, "photo_trip_id = ?", new String[]{photoParentId}, null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				actualPhotos.add(c.getString(c.getColumnIndex(Photos._ID)));
				String galleryId = c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID));
				galleryIds.add(galleryId);
				facebookIds.add(c.getString(c.getColumnIndex(Photos.PHOTO_FACEBOOK_ID)));
				synced.add(c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED)));
				File file = new File(galleryId);
				photos.add(file);
				
				c.moveToNext();
			}
			c.close();
		} else {
			Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String tripId = c.getString(c.getColumnIndex(Photos.PHOTO_TRIP_ID));
				if(TextUtils.isEmpty(tripId)) {
					actualPhotos.add(c.getString(c.getColumnIndex(Photos._ID)));
					String galleryId = c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID));
					galleryIds.add(galleryId);
					facebookIds.add(c.getString(c.getColumnIndex(Photos.PHOTO_FACEBOOK_ID)));
					synced.add(c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED)));
					File file = new File(galleryId);
					photos.add(file);
				}
				
				c.moveToNext();
			}
			c.close();
		}
		
		String placeName = "";
		Cursor c = getContentResolver().query(TripDetails.CONTENT_URI, null, "_id = ?", new String[]{photoParentId}, null);
		if(c != null) {
			c.moveToFirst();
			if(!c.isAfterLast()) {
				placeName = c.getString(c.getColumnIndex(TripDetails.PLACE_NAME));
			}
		}
		c.close();
		
		// ******** Start of Action Bar configuration
		actionbar = (ActionBar) findViewById(R.id.actionBar1);
		if(TextUtils.isEmpty(placeName)) {
			actionbar.setTitle("Others");
		} else {
			if(!TextUtils.isEmpty(startDate)) {
				actionbar.setTitle(placeName+"_"+startDate);
			} else {
				actionbar.setTitle(placeName);
			}
		}
		actionbar.setHomeAction(new Action() {
			@Override
			public void performAction(View view) {
				finish();
			}

			@Override
			public int getDrawable() {
				return R.drawable.back_actionbar;
			}
		});
		actionbar.addAction(createClickAction());
		actionbar.addAction(new Action() {
			
			@Override
			public void performAction(View view) {
				Intent intent = new Intent(ViewTripDetailsActivity.this,
						SyncService.class).putExtra("ID", photoParentId)
						.putExtra("ALBUM", "true");
				startService(intent);
			}
			
			@Override
			public int getDrawable() {
				return R.drawable.sync;
			}
		});
		// ******** End of Action Bar configuration

		if(photos != null && photos.size() > 0) {
			grid.setAdapter(new ViewTripDetailsGridAdapter(this, photos, synced));
			grid.setOnItemClickListener(new OnItemClickListener() {
	
				@Override	
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					Intent i = new Intent(ViewTripDetailsActivity.this,
							ImageViewerActivity.class).putExtra("ID",
							actualPhotos.get(position))
							.putExtra("PARENT_ID", photoParentId)
							.putExtra("FACEBOOK_ID", facebookIds.get(position))
							.putExtra("GALLERY_ID", galleryIds.get(position))
							.putExtra("SYNCED", synced.get(position));
					startActivity(i);
				}
			});
			grid.refreshDrawableState();
		} else {
			grid.setVisibility(View.GONE);
			emptyView.setVisibility(View.VISIBLE);
			emptyView.startAnimation(AnimationUtils.loadAnimation(ViewTripDetailsActivity.this, R.anim.zoom_center));
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
}