package com.intuit.project.phlogit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.intuit.project.phlogit.adapter.ViewTripsGridAdapter;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;
import com.markupartist.android.widget.ActionBar.IntentAction;

public class ViewTripsActivity extends Activity {
	private GridView upcomingGrid;
	private GridView archivedGrid;
	private ArrayList<String> upPlaces = new ArrayList<String>();
	private ArrayList<String> upPhotos = new ArrayList<String>();
	private ArrayList<String> arPlaces = new ArrayList<String>();
	private ArrayList<String> arPhotos = new ArrayList<String>();
	final List<String> upcomingIds = new ArrayList<String>();
	final List<String> archivedIds = new ArrayList<String>();
	private ArrayList<String> tripStartDate = new ArrayList<String>();
	private ArrayList<String> tripEndDate = new ArrayList<String>();

	private ProgressDialog dialog;
	private TextView emptyUp;
	private TextView emptyAr;
	private TextView archivedTitle;
	private TextView upcomingTitle;
	
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch(msg.what) {
			case 100:
				if(dialog != null && dialog.isShowing()) {
					dialog.dismiss();
				}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.view_trips);
		
		upcomingGrid = (GridView) findViewById(R.id.gridViewUpcoming);
		archivedGrid = (GridView) findViewById(R.id.gridViewArchived);
		emptyUp = (TextView)findViewById(R.id.emptyUp);
		emptyAr = (TextView)findViewById(R.id.emptyAr);
		
		upcomingTitle = (TextView)findViewById(R.id.upcomingTitle);
		archivedTitle = (TextView)findViewById(R.id.archivedTitle);
		
		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
		actionbar.setTitle("View your Albums");
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
		actionbar.addAction(createClickAction());
		// ******** End of Action Bar configuration
		dialog = ProgressDialog.show(ViewTripsActivity.this, "", "Loading Albums...", true, false, null);
		LoadAlbumsTask task = new LoadAlbumsTask();
		task.execute(null);
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if(v.getId() == R.id.gridViewUpcoming) {
			AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
		    menu.add(0, 0, 0, "Details");
		}
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		  
		Toast.makeText(this,
				"Place Name: " + upPlaces.get(info.position) + "\nStart Date: "
						+ tripStartDate.get(info.position) + "\nEnd Date: "
						+ tripEndDate.get(info.position), Toast.LENGTH_LONG).show();
		  
		return true;
	}
	
	class LoadAlbumsTask extends AsyncTask<Void, Void, Void> {
		Date currentDate = new Date();

		@Override
		protected Void doInBackground(Void... params) {
			tripStartDate = new ArrayList<String>();
			Cursor cursor = getContentResolver().query(TripDetails.CONTENT_URI, null, null, null, null);
			cursor.moveToFirst();
			while(!cursor.isAfterLast()) {
				String id = cursor.getString(cursor.getColumnIndex(TripDetails._ID));
				String dateStr = cursor.getString(cursor.getColumnIndex(TripDetails.START_DATE));
				tripStartDate.add(dateStr);
				String endDateStr = cursor.getString(cursor.getColumnIndex(TripDetails.END_DATE));
				tripEndDate.add(endDateStr);
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date startDate = new Date();
				Date endDate = new Date();
				try {
					startDate = format.parse(dateStr);
					endDate = format.parse(endDateStr);
				} catch (ParseException e) {
					//Ignore
				}
				System.out.println(dateStr+" "+startDate);
				if(startDate.equals(currentDate) || ( startDate.before(currentDate) && endDate.after(currentDate)) || (startDate.after(currentDate))) {
					upcomingIds.add(id);
					upPlaces.add(cursor.getString(cursor.getColumnIndex(TripDetails.PLACE_NAME)));
					upPhotos.add(cursor.getString(cursor.getColumnIndex(TripDetails.NUMBER_PHOTOS)));
				} else {
					archivedIds.add(id);
					arPlaces.add(cursor.getString(cursor.getColumnIndex(TripDetails.PLACE_NAME)));
					arPhotos.add(cursor.getString(cursor.getColumnIndex(TripDetails.NUMBER_PHOTOS)));
				}
				cursor.moveToNext();
			}
			cursor.close();
			
			//For other photos not associated with any albums/trips.
			cursor = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
			cursor.moveToFirst();
			String miscAlbumName = "Others";
			boolean upcomingOther = false;
			boolean archivedOther = false;
			int upNumber = 0;
			int arNumber = 0;
			while(!cursor.isAfterLast()) {
				String parentId = cursor.getString(cursor.getColumnIndex(Photos.PHOTO_TRIP_ID));
				//No trip associated with this.
				if(TextUtils.isEmpty(parentId)) {
					String dateStr = cursor.getString(cursor.getColumnIndex(Photos.PHOTO_TAKEN_DATE));
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
					Date takenDate = new Date();
					Date now = new Date();
					try {
						takenDate = format.parse(dateStr);
					} catch (ParseException e) {
						//Ignore
					}
					if(takenDate.equals(now) || takenDate.after(currentDate)) {
						upcomingOther = true;
						upNumber++;
					} else {
						archivedOther = true;
						arNumber++;
					}
				}
				cursor.moveToNext();
			}
			cursor.close();
			
			if(upcomingOther) {
				upcomingIds.add("-1");
				upPlaces.add(miscAlbumName);
				tripStartDate.add("");
				tripEndDate.add("");
				upPhotos.add(""+upNumber);
			} 
			if(archivedOther) {
				archivedIds.add("-1");
				arPlaces.add(miscAlbumName);
				tripStartDate.add("");
				tripEndDate.add("");
				arPhotos.add(""+arNumber);
			}
			
			handler.sendEmptyMessageDelayed(100, 1500);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			upcomingTitle.setText("Upcoming "+"("+upcomingIds.size()+")");
			archivedTitle.setText("Archived "+"("+archivedIds.size()+")");
			
			ArrayList<String> fileName = new ArrayList<String>();
			if(upcomingIds.size() > 0) {
				for(int i=0;i<upcomingIds.size();i++) {
					if("-1".equals(upcomingIds.get(i))) {
						Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
						c.moveToFirst();
						while(!c.isAfterLast()) {
							String parentId = c.getString(c.getColumnIndex(Photos.PHOTO_TRIP_ID));
							//No trip associated with this.
							if(TextUtils.isEmpty(parentId)) {
								String dateStr = c.getString(c.getColumnIndex(Photos.PHOTO_TAKEN_DATE));
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
								Date takenDate = new Date();
								Date now = new Date();
								try {
									takenDate = format.parse(dateStr);
								} catch (ParseException e) {
									//Ignore
								}
								if(takenDate.equals(now) || takenDate.after(currentDate)) {
									fileName.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));
									break;
								}
							}
							c.moveToNext();
						}
						c.close();
					} else if(!"0".equals(upPhotos.get(i)) && !"-1".equals(upcomingIds.get(i))) {
						Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, "photo_trip_id = ?", new String[]{upcomingIds.get(i)}, null);
						c.moveToFirst();
						if(!c.isAfterLast()) {
							fileName.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));
						}
						c.close();
					} else {
						fileName.add("-1");
					}
				}
				
				upcomingGrid.setAdapter(new ViewTripsGridAdapter(ViewTripsActivity.this, true, upPlaces, upPhotos, fileName, tripStartDate));
				upcomingGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
		
					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long id) {
						Intent i = new Intent(ViewTripsActivity.this, ViewTripDetailsActivity.class).putExtra("ID", upcomingIds.get(position)).putExtra("START_DATE", tripStartDate.get(position));
						startActivity(i);
					}
				});
				upcomingGrid.refreshDrawableState();
			} else {
				upcomingGrid.setVisibility(View.GONE);
				emptyUp.setVisibility(View.VISIBLE);
			}
			
			fileName = new ArrayList<String>();
			if(archivedIds.size() > 0) {
				for(int i=0;i<archivedIds.size();i++) {
					if("-1".equals(archivedIds.get(i))) {
						Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
						c.moveToFirst();
						while(!c.isAfterLast()) {
							String parentId = c.getString(c.getColumnIndex(Photos.PHOTO_TRIP_ID));
							//No trip associated with this.
							if(TextUtils.isEmpty(parentId)) {
								String dateStr = c.getString(c.getColumnIndex(Photos.PHOTO_TAKEN_DATE));
								SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
								Date takenDate = new Date();
								Date now = new Date();
								try {
									takenDate = format.parse(dateStr);
								} catch (ParseException e) {
									//Ignore
								}
								if(takenDate.before(now)) {
									fileName.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));	
									break;
								}
							}
							c.moveToNext();
						}
						c.close();
					} else if(!"0".equals(arPhotos.get(i)) && !"-1".equals(archivedIds.get(i))) {
						Cursor c = getContentResolver().query(Photos.CONTENT_URI, null, "photo_trip_id = ?", new String[]{archivedIds.get(i)}, null);
						c.moveToFirst();
						if(!c.isAfterLast()) {
							fileName.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));
						}
						c.close();
					} else {
						fileName.add("-1");
					}
				}
				archivedGrid.setAdapter(new ViewTripsGridAdapter(ViewTripsActivity.this, false, arPlaces, arPhotos, fileName, tripStartDate));
				archivedGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> parent, View view,
							int position, long duration) {
						Intent i = new Intent(ViewTripsActivity.this, ViewTripDetailsActivity.class).putExtra("ID", archivedIds.get(position)).putExtra("START_DATE", tripStartDate.get(position));
						startActivity(i);
					}
				});
				archivedGrid.refreshDrawableState();
			} else {
				archivedGrid.setVisibility(View.GONE);
				emptyAr.setVisibility(View.VISIBLE);
			}
			handler.sendEmptyMessageDelayed(100, 1000);
			registerForContextMenu(upcomingGrid);
			registerForContextMenu(archivedGrid);
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
