/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package com.intuit.project.phlogit;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLConnection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.me.jstott.jcoord.LatLng;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.intuit.project.phlogit.constants.Constants;
import com.intuit.project.phlogit.data.vo.PlacesVO;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.service.SyncService;
import com.intuit.project.phlogit.socialnetwork.FacebookSocialNetwork;
import com.intuit.project.phlogit.util.NetworkUtil;
import com.intuit.project.phlogit.util.PlacesXmlHandler;
import com.intuit.project.phlogit.util.Utility;
import com.intuit.project.phlogit.widget.CameraView;

public class SnapPictureActivity extends Activity implements Camera.PictureCallback,
		Camera.AutoFocusCallback, Camera.ErrorCallback, SensorEventListener, LocationListener {

	// private AnimationDrawable cameraHelpAnimation;
	private HashMap<Integer, Boolean> isSensorRegistered = new HashMap<Integer, Boolean>();

	private CameraView cameraView;

	private static final String LOG_TAG = "InstantReturn:SnapPictureActivity";

	private SensorManager sensorManager;

	// Constants for testing if each sensor are ok for taking snap.
	public static final int ORIENTATION_SENSOR = 0;

	public static final int ACCELEROMETER_SENSOR = 1;

	public static final int LIGHT_SENSOR = 2;

	private ArrayList<Integer> sensorName = new ArrayList<Integer>();

	// Constants for handler.
	private static final int SNAP_PICTURE = 101;

	private static final int AUTO_FOCUS_FAILED_SNAP = 200;

	private static final int CLEAR_LED = 300;

	private static final int REGISTER_SENSORS = 400;

	protected static final int REMOVE_PHOTO_ERROR_IMAGE = 500;

	// Flag to check if auto focus is successful or not.
	private boolean inAutoFocus = false;

	private boolean isTakingPicture = false;

	private int LED_NOTIFICATION_ID = 102;

	public static final int PROGRESS_DIALOG = 103;

	private ImageView photoErrorImage;

	private ImageProcessAsyncTask asyncTask;

	private long lastUpdate = -1;

	private float last_x = 0, last_y = 0, last_z = 0;

	private final int SHAKE_THRESHOLD = 10;

	private final int STEADY_COUNT = 5;

	private final int TIME_INTERVAL = 10;

	private int counter = 0;

	private ViewGroup poiIndicatorLayout;

	private static final int PROGRESS_STATUS_ACTIVITY_RESULT_CODE = 199;

	private String savedImageFileName;
	
	private Location currentLocation = null;
	
	private String albumName = "";

	private String parentId;

	private int numberPhotos;

	public PlacesVO placesVO;

	private LocationManager location;

	private ViewGroup poiDetailsSection;

	private ListView poiList;

	private TextView poiDetails;
	
	private ArrayList<PlacesVO> places = new ArrayList<PlacesVO>();

	private ViewGroup cameraLayout;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		try {
			super.onCreate(savedInstanceState);

			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
					WindowManager.LayoutParams.FLAG_FULLSCREEN);
			
			//TODO: Check in using Facebook - Auto/Not?
			try {
				setContentView(R.layout.camera_preview);
			} catch (Throwable r) {
				Log.e(Constants.APP_NAME, "Exception in setContentView", r);
				return;
			}
			
			Cursor c = getContentResolver().query(TripDetails.CONTENT_URI, null, null, null, null);
		    c.moveToFirst();
		    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		    while(!c.isAfterLast()) {
		    	String id = c.getString(c.getColumnIndex(TripDetails._ID));
		    	String startDateStr = c.getString(c.getColumnIndex(TripDetails.START_DATE));
		    	String endDateStr = c.getString(c.getColumnIndex(TripDetails.END_DATE));
		    	String placeName = c.getString(c.getColumnIndex(TripDetails.PLACE_NAME));
		    	numberPhotos = c.getInt(c.getColumnIndex(TripDetails.NUMBER_PHOTOS));
				Date startDate = new Date();
				Date endDate = new Date();
				Date currentDate = new Date();
				try {
					startDate = format.parse(startDateStr);
					endDate = format.parse(endDateStr);
				} catch (ParseException e) {
					//Ignore
				}
				
				if(startDate.equals(currentDate) || ( startDate.before(currentDate) && endDate.after(currentDate))) {
					albumName = placeName + "_" + startDateStr;
					parentId = id;
					break;
				}
				c.moveToNext();
		    }
		    c.close();
		    if(TextUtils.isEmpty(albumName)) {
				albumName = "phLogIt_"+format.format(new Date());						
		    }
			
			//TODO: Store this also.
			//Also use this for getting places nearby.
			location = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
			location.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
				
			poiIndicatorLayout = (ViewGroup)findViewById(R.id.poiIndicatorSection);
			poiDetails = (TextView)findViewById(R.id.poiDetails);
			poiDetailsSection = (ViewGroup)findViewById(R.id.poiDetailsSection);
			poiList = (ListView)findViewById(R.id.poiList);
			
			cameraView = (CameraView) this.findViewById(R.id.cameraView);
			cameraView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					if(poiIndicatorLayout != null && poiIndicatorLayout.getVisibility() == View.VISIBLE) {
						poiIndicatorLayout.setVisibility(View.GONE);
						return;
					}
					SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
				    if(prefs.getBoolean("steadyShot", true)) {
				    	addSensorsAndRegister();				    	
				    } else {
				    	snapPicture();
				    }
					cameraView.setOnClickListener(null);
				}
			});
			
			photoErrorImage = (ImageView) findViewById(R.id.photoErrorImage);
			if(Utility.isFirstRun()) {
				Toast.makeText(this, "Touch anywhere to take photo", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
		}
	}

	protected void searchPlaces() {
		PlacesAsyncTask task = new PlacesAsyncTask();
		task.execute(null);
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
	}

	private void snapPicture() {
		try {
			cameraView.getCamera().autoFocus(this);
		} catch (Exception e) {
			Log.e("InstantReturn", "autoFocus failed");
			// Take picture anyway.
			inAutoFocus = false;
			try {
				if (cameraView != null && cameraView.getCamera() != null)
					cameraView.getCamera().takePicture(null, null, this);
				// This should not happen. There is a possibility of this to happen when onPause is
				// called (due to an incoming call)
				// In such a case we are finishing this camera activity and taking them back to the
				// Home screen.
				else
					finish();
			} catch (Exception ex) {
				finish();
			}
		}
	}

	@Override
	public void onAutoFocus(boolean success, Camera camera) {
		try {
			inAutoFocus = true;
			if ((cameraView != null) && (cameraView.getCamera() != null)) {
				cameraView.getCamera().takePicture(null, null, this);
			}
		} catch (Exception e) {
			inAutoFocus = false;
		}
	}

	private boolean analyzeVauesAcc(int x, int y, int z) {
		Log.i(LOG_TAG, "Analyzing Accelerometer values : " + x + ", " + y + ", " + z);
		updateCordinates(x, y, z);
		return false;
	}

	/**
	 * Method to register the sensors in our interest as defined by the variable sensorName.
	 */

	@Override
	protected void onDestroy() {
		super.onDestroy();
		DeRegisterListener();
		location.removeUpdates(this);
	}

	private void addSensorsAndRegister() {
		sensorName = new ArrayList<Integer>();
		sensorName.add(Sensor.TYPE_ACCELEROMETER);
		handler.sendEmptyMessage(REGISTER_SENSORS);
		photoErrorImage.setVisibility(View.VISIBLE);
		photoErrorImage.setImageResource(R.drawable.hold_steady);

	}

	private void registerSensorListeners() {
		try {
			if (sensorName != null) {
				sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
				List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
				Log.i(LOG_TAG, "TOTAL SENSORS = " + (sensors != null ? sensors.size() : 0));
				Sensor ourSensor = null;
				for (int i = 0; i < sensors.size(); ++i) {
					Log.i(LOG_TAG, "Sensors = " + sensors.get(i).getName());

					for (int j = 0; j < sensorName.size(); j++) {
						if (sensors.get(i).getType() == sensorName.get(j)) {
							ourSensor = sensors.get(i);

							Log.i(LOG_TAG, "Registering sensor " + ourSensor.getName());
							sensorManager.registerListener(this, ourSensor,
									SensorManager.SENSOR_DELAY_UI);

							isSensorRegistered.put(j, true);
						}
					}

				}

			}
		} catch (Exception e) {
		}
	}

	private void DeRegisterListener() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(SnapPictureActivity.this);
			sensorManager = null;
			sensorName = null;
		}
	}

	private void updateCordinates(float x, float y, float z) {
		try {
			long curTime = System.currentTimeMillis();
			long diffTime = (curTime - lastUpdate);
			if (diffTime >= TIME_INTERVAL) {
				lastUpdate = curTime;

				float speed = Math.abs(x + y + z - last_x - last_y - last_z) / diffTime * 10000;
				if (speed >= SHAKE_THRESHOLD) {
					counter = 0;
				} else {
					counter++;
					if (counter > STEADY_COUNT) {
						counter = 0;
						DeRegisterListener();
						photoErrorImage.setVisibility(View.GONE);
						handler.sendEmptyMessage(SNAP_PICTURE);
					}
				}
				last_x = x;
				last_y = y;
				last_z = z;

			} else
				lastUpdate = System.currentTimeMillis();
		} catch (Exception e) {
		}
	}

	/**
	 * Method to get values of each sensor and test against our ideal values.
	 */
	@Override
	public void onSensorChanged(SensorEvent sensorEvent) {
		int X = (int) sensorEvent.values[0];
		int Y = (int) sensorEvent.values[1];
		int Z = (int) sensorEvent.values[2];
		analyzeVauesAcc(X, Y, Z);
	}

	public void shutdownGracefully() {
		Log.i(LOG_TAG, "Camera is unavailable..Shutting down gracefully");
		DeRegisterListener();
		this.finish();

	}

	private void clearLED() {
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(LED_NOTIFICATION_ID);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	/**
	 * Handler class to handle some delayed and other UI events.
	 */
	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {

			case SNAP_PICTURE:
				snapPicture();
				break;
			case AUTO_FOCUS_FAILED_SNAP:
				try {
					Log.d(Constants.APP_NAME, "AutoFocus Failed");
					if (!inAutoFocus) {

						if (cameraView != null && cameraView.getCamera() != null) {
							cameraView.getCamera()
									.takePicture(null, null, SnapPictureActivity.this);
						}
					}
				} catch (Exception e) {
				}
				break;
			case CLEAR_LED:
				clearLED();
				break;
			case REGISTER_SENSORS:
				registerSensorListeners();
				break;
			case REMOVE_PHOTO_ERROR_IMAGE:
				photoErrorImage.setVisibility(View.GONE);
				break;
			case 600:
				setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
				poiIndicatorLayout.setVisibility(View.VISIBLE);
		    	poiIndicatorLayout.startAnimation(AnimationUtils.loadAnimation(SnapPictureActivity.this, R.anim.push_up_in));
		    	break;

			}
		}
	};

	private class ImageProcessAsyncTask extends AsyncTask<byte[], Void, Boolean> {

		@Override
		protected Boolean doInBackground(byte[]... params) {
			try {
				FacebookSocialNetwork facebook = new FacebookSocialNetwork(Phlogit.getPhlogitApplicationContext());
				File storageDir = new File(
					    Environment.getExternalStorageDirectory(),
					    "phLogIt"
					);              
				// Create an image file name
			    String timeStamp = 
			        new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			    String imageFileName = "photo_"+timeStamp;
			    if(!storageDir.exists())
			    	storageDir.mkdir();
			    File image = new File(storageDir, imageFileName+".jpg");
			    System.out.println(image.getAbsolutePath());
			    if(!image.exists())
			    	image.createNewFile();
			    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(image));
			    bos.write(params[0]);
			    bos.flush();
			    bos.close();
			    savedImageFileName = image.getAbsolutePath();
			    
			    String fbId = "";
			    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
			    Uri contentUri = Uri.fromFile(image);
			    mediaScanIntent.setData(contentUri);
			    sendBroadcast(mediaScanIntent);
			    
			    
			    String insertedFileName = "";
			    Cursor alreadyInserted = getContentResolver().query(Photos.CONTENT_URI, new String[]{Photos.PHOTO_GALLERY_ID}, "photo_gallery_id = ?", new String[]{savedImageFileName}, null);
			    if(alreadyInserted != null) {
			    	alreadyInserted.moveToFirst();
			    	if(!alreadyInserted.isAfterLast()) {
			    		insertedFileName = alreadyInserted.getString(alreadyInserted.getColumnIndex(Photos.PHOTO_GALLERY_ID));
			    	}
			    	alreadyInserted.close();
			    }
			    Date currentDate = new Date();
			    String currentDateStr = "";
			    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			    currentDateStr = sdf.format(currentDate);
			    String photoId = "";
			    if(!TextUtils.isEmpty(insertedFileName)) {
			    	ContentValues values = new ContentValues();
				    if(!TextUtils.isEmpty(parentId)) {
				    	values.put(Photos.PHOTO_TRIP_ID, parentId);
				    	ContentValues parentValues = new ContentValues();
				    	numberPhotos = numberPhotos + 1;
				    	parentValues.put(TripDetails.NUMBER_PHOTOS, numberPhotos);
				    	getContentResolver().update(TripDetails.CONTENT_URI, parentValues, "_id = ?", new String[]{parentId});
				    }
				    if(currentLocation != null) {
				    	values.put(Photos.PHOTO_LATLON, currentLocation.getLatitude()+","+currentLocation.getLongitude());
				    }
				    values.put(Photos.PHOTO_TAKEN_DATE, currentDateStr);
				    getContentResolver().update(Photos.CONTENT_URI, values, "photo_gallery_id = ?", new String[]{savedImageFileName});
			    } else {
				    ContentValues values = new ContentValues();
				    values.put(Photos.PHOTO_GALLERY_ID, savedImageFileName);
				    if(!TextUtils.isEmpty(fbId)) {
				    	values.put(Photos.PHOTO_FACEBOOK_ID, fbId);
				    	values.put(Photos.PHOTO_SYNCED, "true");
				    }
				    if(!TextUtils.isEmpty(parentId)) {
				    	values.put(Photos.PHOTO_TRIP_ID, parentId);
				    	ContentValues parentValues = new ContentValues();
				    	numberPhotos = numberPhotos + 1;
				    	parentValues.put(TripDetails.NUMBER_PHOTOS, numberPhotos);
				    	getContentResolver().update(TripDetails.CONTENT_URI, parentValues, "_id = ?", new String[]{parentId});
				    }
				    if(currentLocation != null) {
				    	values.put(Photos.PHOTO_LATLON, currentLocation.getLatitude()+","+currentLocation.getLongitude());
				    }
				    values.put(Photos.PHOTO_TAKEN_DATE, currentDateStr);
				    Uri uri = getContentResolver().insert(Photos.CONTENT_URI, values);
				    photoId = uri.getLastPathSegment();
				    System.out.println("^^^^^^^^ PHOTOID = "+photoId);
			    }
			    SharedPreferences prefs = getSharedPreferences("Settings", MODE_PRIVATE);
			    if(prefs.getBoolean("autoSync", true)) {
			    	startService(new Intent(SnapPictureActivity.this, SyncService.class).putExtra("ID", photoId).putExtra("PARENT_ID", parentId));
			    }
			    finish();
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
	}
	
	private class PlacesAsyncTask extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			Date now = new Date();
			String types = "";
			//Relax. Take time to eat :)
			if(now.getHours() >= 12 && now.getHours() <= 15) {
				types = "food%7catm%7clodging%7cboarding%7cgas_station";
			} else {
				types = "point_of_interest%7catm%7cgas_station%7cbank";				
			}
			String url = "https://maps.googleapis.com/maps/api/place/search/json?location="+currentLocation.getLatitude()+","+currentLocation.getLongitude()+"&radius=2500&types="+types+"&sensor=false&key=AIzaSyBtlTHEzOrlvd8AbKGmr_Np2I1_5W988pQ";
			String response = NetworkUtil.get(url);
			places = parseJSONResponse(response);
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			updatePlacesUI();
		}

		private ArrayList<PlacesVO> parseJSONResponse(String response) {
			
			ArrayList<PlacesVO> places = new ArrayList<PlacesVO>();
			
			try {
				JSONObject json = new JSONObject(response);
				
				JSONArray results = json.getJSONArray("results");
				
				for(int i=0;i<results.length();i++) {
					PlacesVO vo = new PlacesVO();
					JSONObject obj = results.getJSONObject(i);
					vo.lat = obj.getJSONObject("geometry").getJSONObject("location").getString("lat");
					vo.lng = obj.getJSONObject("geometry").getJSONObject("location").getString("lng");
					
					vo.icon = obj.getString("icon");
					vo.id = obj.getString("id");
					vo.name = obj.getString("name");
					
					JSONArray types = obj.getJSONArray("types");
					for(int j=0;j<types.length();j++) {
						vo.types.add(types.getString(j));
					}
					
					try {
						vo.rating = obj.getString("rating");
					} catch (Exception e) {
						// Ignore
					}
					vo.vicinity = obj.getString("vicinity");
					places.add(vo);
				}
				
			} catch (JSONException e) {
				e.printStackTrace();
			}
			
			return places;
		}
		
	}

	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		try {
			finishActivity(PROGRESS_DIALOG);
			asyncTask = new ImageProcessAsyncTask();
			asyncTask.execute(data);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePlacesUI() {
		if(places != null && places.size() > 0) {
			System.out.println(places.toString());
			poiIndicatorLayout.setVisibility(View.VISIBLE);
			poiDetails.setText(places.size()+" Places (POIs)");
			poiIndicatorLayout.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					poiDetailsSection.setVisibility(View.VISIBLE);
					poiList.setDivider(getResources().getDrawable(R.drawable.horizontal_separator));
					Date now = new Date();
					TextView tv = new TextView(SnapPictureActivity.this);
					tv.setTextSize(15);
					tv.setTypeface(Typeface.DEFAULT_BOLD);
					tv.setGravity(Gravity.CENTER);
					if(now.getHours() >= 12 && now.getHours() <= 15) {
						tv.setText("Relax and take time out to eat and rest!");	
					} else {
						tv.setText("Check out POIs near you!");
					}
					poiList.addHeaderView(tv);
					POIAdapter adapter = new POIAdapter(SnapPictureActivity.this, places);
					adapter.notifyDataSetChanged();
					poiList.setAdapter(adapter);
					poiList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
						@Override
						public void onItemClick(AdapterView<?> adapter, View v,
								int pos, long duration) {
							String url = "";
							if(currentLocation != null) {
								url = "http://maps.google.com/maps?saddr="+currentLocation.getLatitude()+","+currentLocation.getLongitude()+"&daddr="+places.get(pos).lat+","+places.get(pos).lng;
							} else {
								url = "http://maps.google.com/maps?daddr="+places.get(pos).lat+","+places.get(pos).lng;
							}
							Intent intent = new Intent(android.content.Intent.ACTION_VIEW,  Uri.parse(url));
							intent.setClassName("com.google.android.apps.maps", "com.google.android.maps.MapsActivity");
							startActivity(intent);
							setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
							finish();
						}
					});
				}
			});
		}
	}

	@Override
	public void onError(int error, Camera camera) {
		finish();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_CAMERA)
			return true;
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onLocationChanged(Location location) {
		currentLocation = location;
		this.location.removeUpdates(this);
		searchPlaces();
	}

	@Override
	public void onProviderDisabled(String provider) {
	}

	@Override
	public void onProviderEnabled(String provider) {
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	/**
     * Adapter for our image files.
     */
    private class POIAdapter extends BaseAdapter {

        private Context context;
		private ArrayList<PlacesVO> places;
		private LayoutInflater layoutInflater;

        public POIAdapter(Context localContext, ArrayList<PlacesVO> places) {
            context = localContext;
            this.places = places;
            layoutInflater = (LayoutInflater)localContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return places.size();
        }
        
        public Object getItem(int position) {
            return position;
        }
        
        public long getItemId(int position) {
            return position;
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
        	ViewHolder holder;
            if (convertView == null) {
                convertView = (LinearLayout)layoutInflater.inflate(R.layout.poi_list_item, null);
                holder = new ViewHolder();
                holder.icon = (ImageView)convertView.findViewById(R.id.poiIcon);
                holder.name = (TextView)convertView.findViewById(R.id.poiName);
                holder.vicinity = (TextView)convertView.findViewById(R.id.poiVicinity);
                holder.name.setTextSize(15);
                holder.name.setTextColor(Color.WHITE);
                holder.name.setTypeface(Typeface.DEFAULT_BOLD);
                holder.vicinity.setTextSize(15);
                holder.vicinity.setTextColor(Color.WHITE);
                convertView.setTag(holder);
            }
            else {     
                holder = (ViewHolder) convertView.getTag();
            }
            double distance = distTo(places.get(position).lat, places.get(position).lng);
            holder.name.setText(places.get(position).name+" ("+distance+" km)");
            holder.vicinity.setText(places.get(position).vicinity);
            int drawable = R.drawable.generic;
            if(places.get(position).types.contains("lodging")) {
        		drawable = R.drawable.lodging;
            } else if(places.get(position).types.contains("gas_station")) {
            	drawable = R.drawable.gas_station;
            }
            holder.icon.setImageResource(drawable);
            return convertView;
        }
    }
    
    private Bitmap getImageBitmap(String url) {
        Bitmap bm = null;
        try {
            URL aURL = new URL(url);
            URLConnection conn = aURL.openConnection();
            conn.connect();
            InputStream is = conn.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            bm = BitmapFactory.decodeStream(bis);
            bis.close();
            is.close();
        } catch (IOException e) {
            Log.e("phLogIt!", "Error getting bitmap", e);
        }
        return bm;
    } 
    
    class ViewHolder {
    	ImageView icon;
    	TextView name;
    	TextView vicinity;
    }

	public double calculateDistance(String lat, String lng) {
		double otherLat = Double.parseDouble(lat);
		double otherLng = Double.parseDouble(lng);
		
		double myLat = currentLocation.getLatitude();
		double myLng = currentLocation.getLongitude();
		
		LatLng lld1 = new LatLng(myLat, myLng);
	    LatLng lld2 = new LatLng(otherLat, otherLng);
	    double d = lld1.distance(lld2)  * 1000;
	    d = d / 1000;
	    d = round(d, 2, BigDecimal.ROUND_CEILING);
	    return d;
	}	
	
	public static double round(double unrounded, int precision, int roundingMode)
	{
	    BigDecimal bd = new BigDecimal(unrounded);
	    BigDecimal rounded = bd.setScale(precision, roundingMode);
	    return rounded.doubleValue();
	}
	
	@Override
	public void onBackPressed() {
		if(poiIndicatorLayout != null && poiIndicatorLayout.getVisibility() == View.VISIBLE) {
			if(poiDetailsSection != null && poiDetailsSection.getVisibility() == View.VISIBLE) {
				poiDetailsSection.setVisibility(View.GONE);
			}
			poiIndicatorLayout.setVisibility(View.GONE);
			return;
		}
		super.onBackPressed();
	}
	
	public double distTo(String lat1, String lng1) {
	    double earthRadius = 6371;  //km
	    
	    double otherLat = Double.parseDouble(lat1);
		double otherLng = Double.parseDouble(lng1);
	    
		double myLat = currentLocation.getLatitude();
		double myLng = currentLocation.getLongitude();
		
	    double dLat = Math.toRadians(myLat-otherLat);
	    double dLng = Math.toRadians(myLng-otherLng);
	    double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
	               Math.cos(Math.toRadians(otherLat)) * Math.cos(Math.toRadians(myLat)) *
	               Math.sin(dLng/2) * Math.sin(dLng/2);
	    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
	    double dist = earthRadius * c;
	    dist = round(dist, 2, BigDecimal.ROUND_CEILING);
	    return dist;
	}
}
