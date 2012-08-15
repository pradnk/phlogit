package com.intuit.project.phlogit.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import com.intuit.project.SyncCompleteListener;
import com.intuit.project.phlogit.HomeActivity;
import com.intuit.project.phlogit.Phlogit;
import com.intuit.project.phlogit.R;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.socialnetwork.FacebookSocialNetwork;
import com.intuit.project.phlogit.util.NetworkUtil;

public class SyncService extends IntentService {
	
	private static final String TAG = "SyncService";
	
	public SyncService() {
		super(TAG);
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		System.out.println("Sync Service Started with Intent "+intent);
		
		SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
    	if(prefs.getBoolean("network", true)) {
    		if(!NetworkUtil.isConnectedWiFi()) {
    			return;
    		}    		
    	} else {
    		if(!NetworkUtil.isConnected()) {
    			return;
    		}
    	}
		
		String albumSync = intent.getStringExtra("ALBUM");
		String id = intent.getStringExtra("ID");
		String parentId = intent.getStringExtra("PARENT_ID");
		
		NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		CharSequence contentTitle = "phLogIt! - Syncing";
		Notification notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
		Intent notificationIntent = new Intent(this, HomeActivity.class);
		
		PendingIntent operation = PendingIntent.getActivity(this, 100, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
		
		MySyncComplete syncComplete = new MySyncComplete();
		
		if("true".equals(albumSync)) {
			ArrayList<String> galleryIds = new ArrayList<String>(); 
			//Get the trip_photo_id passed and get all photos belonging to that trip.
			//See which has been synced if not then try to sync based on the date taken value.
			Cursor c = getContentResolver().query(TripDetails.CONTENT_URI, null, "_id = ?", new String[]{id}, null);
			String albumName = "";
			if(c!= null && c.getCount() > 0) {
				c.moveToFirst();
				String placeName = c.getString(c.getColumnIndex(TripDetails.PLACE_NAME));
				String startDate = c.getString(c.getColumnIndex(TripDetails.START_DATE));
				albumName = placeName+"_"+startDate;
			} else {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date now = new Date();
				String dateStr = format.format(now);
				albumName = "phLogIt_"+dateStr;
			}
			c.close();
			
			c = getContentResolver().query(Photos.CONTENT_URI, null, "photo_trip_id = ?", new String[]{id}, null);
			if(c != null && c.getCount() > 0) {
				c.moveToFirst();
				while(!c.isAfterLast()) {
					
					String synced = c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED));
					if(!"true".equals(synced)) {
						galleryIds.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));
					}
					c.moveToNext();
				}
				c.close();
			} else {
				c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
				if(c != null && c.getCount() > 0) {
					c.moveToFirst();
					while(!c.isAfterLast()) {
						String synced = c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED));
						if(!"true".equals(synced)) {
							galleryIds.add(c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID)));
						}
						c.moveToNext();
					}
					c.close();
				}
			}
			
			syncComplete.setTotalFiles(galleryIds.size());
			
			FacebookSocialNetwork facebook = new FacebookSocialNetwork(this);
			for(int i=0;i<galleryIds.size();i++) {
				notificationManager.cancel(101);
				CharSequence contentText = "Syncing "+(i+1)+"/"+galleryIds.size();
				contentTitle = "phLogIt! - "+contentText;
				notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
				notification.setLatestEventInfo(this, contentTitle, contentText, operation);
				if(prefs.getBoolean("notifications", true)) {
					notificationManager.notify(101, notification);
				}
				facebook.uploadPhoto(galleryIds.get(i), albumName, syncComplete);
			}
			Log.i("SyncService", "No Photos To Sync");
		} else {
			//Get the exact photoId passed.
			//See its not synced and try to based on the date taken attribute.
			String albumName = "";
			Cursor c = null;
			if (parentId != null && !"-1".equals(parentId)) {
				c = getContentResolver().query(TripDetails.CONTENT_URI, null,
						"_id = ?", new String[] { parentId }, null);
				if (c != null && c.getCount() > 0) {
					c.moveToFirst();
					String placeName = c.getString(c
							.getColumnIndex(TripDetails.PLACE_NAME));
					String startDate = c.getString(c
							.getColumnIndex(TripDetails.START_DATE));
					albumName = placeName + "_" + startDate;
				}
				c.close();
			} else {
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
				Date now = new Date();
				String dateStr = format.format(now);
				albumName = "phLogIt_" + dateStr;
			}
			String galleryId = "";
			c = getContentResolver().query(Photos.CONTENT_URI, null, "_id = ?", new String[]{id}, null);
			if(c != null) {
				c.moveToFirst();
				if(!c.isAfterLast()) {
					String synced = c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED));
					if(!"true".equals(synced)) {
						galleryId = c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID));
					}
				}
				c.close();
			}
			syncComplete.setTotalFiles(1);
			notificationManager.cancel(101);
			CharSequence contentText = "Syncing 1/1";
			contentTitle = "phLogIt! - "+contentText;
			notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
			notification.setLatestEventInfo(this, contentTitle, contentText, operation);
			if(prefs.getBoolean("notifications", true)) {
				notificationManager.notify(101, notification);
			}
			
			FacebookSocialNetwork facebook = new FacebookSocialNetwork(this);
			facebook.uploadPhoto(galleryId, albumName, syncComplete);
		}
	}
	
	class MySyncComplete implements SyncCompleteListener {
		private int total;
		private int count = 0;
		CharSequence contentTitle = "";
		CharSequence contentText = "";
		private Notification notification;
		private Intent notificationIntent;
		private PendingIntent operation;
		
		@Override
		public void onSyncComplete() {
			count++;
			SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
			NotificationManager notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
			contentTitle = "phLogIt! - Syncing";
			contentText = "Syncing "+count+"/"+total+" complete";
			
			notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
			notificationIntent = new Intent(Phlogit.getPhlogitApplicationContext(), HomeActivity.class);
			operation = PendingIntent.getActivity(Phlogit.getPhlogitApplicationContext(), 100, notificationIntent, Intent.FLAG_ACTIVITY_NEW_TASK);
			if(count < total) {
				if(prefs.getBoolean("notifications", true)) {
					notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
					notification.setLatestEventInfo(Phlogit.getPhlogitApplicationContext(), contentTitle, contentText, operation);
					notificationManager.notify(101, notification);
				}
			} else {
				contentTitle = "phLogIt! - Sync Complete";
				if(prefs.getBoolean("notifications", true)) {
					notification = new Notification(R.drawable.sync, contentTitle, new Date().getTime());
					notification.setLatestEventInfo(Phlogit.getPhlogitApplicationContext(), contentTitle, contentText, operation);
					notificationManager.notify(101, notification);
				}
			}
		}
		
		public void setTotalFiles(int total) {
			this.total = total;
		}
	}
}