package com.intuit.project.phlogit.provider;

import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

public class CustomDatabaseHelper extends SQLiteOpenHelper {

	private static final String TAG = "CustomDatabaseHelper";

	private static final int DATABASE_VERSION = 1;

	private static final String DATABASE_NAME = "phlogit.db";

	private final Context mContext;

	private String[] mUnrestrictedPackages;

	private boolean mReopenDatabase = false;

	private static CustomDatabaseHelper sSingleton = null;

	public interface Tables {
		public static final String TRIP_DETAILS = "trip_details";
		public static final String TRIP_PLACES = "trip_places";
		public static final String PHOTOS = "photos";
	}
	
	public interface Views {
        public static final String TRIP_DETAILS_VIEW = "view_trip_details";
	}
	

	public interface TripDetails {

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"	+ Tables.TRIP_DETAILS;
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CustomDataProvider.AUTHORITY_URI, Tables.TRIP_DETAILS);

		public static final String _ID = BaseColumns._ID;
		public static final String PLACE_NAME = "place_name";
		public static final String START_DATE = "start_date";
		public static final String END_DATE = "end_date";
		public static final String NUMBER_PHOTOS = "number_photos";
		public static final String COMMENTS = "comments";
	}
	
	public interface TripPlaces {

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"	+ Tables.TRIP_PLACES;
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CustomDataProvider.AUTHORITY_URI, Tables.TRIP_PLACES);

		public static final String _ID = BaseColumns._ID;
		public static final String PLACE_NAME = "place_name";
		public static final String PLACE_DESCRIPTION = "description";
		public static final String PLACE_RATING = "rating";
		public static final String PLACE_NUMBER_TIMES_VISITED = "number_times_visited";
	}
	
	public interface Photos {

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/"	+ Tables.PHOTOS;
		public static final Uri CONTENT_URI = Uri.withAppendedPath(CustomDataProvider.AUTHORITY_URI, Tables.PHOTOS);

		public static final String _ID = BaseColumns._ID;
		public static final String PHOTO_TRIP_ID = "photo_trip_id";
		public static final String PHOTO_GALLERY_ID = "photo_gallery_id";
		public static final String PHOTO_FACEBOOK_ID = "photo_facebook_id";
		public static final String PHOTO_NAME = "photo_name";
		public static final String PHOTO_LATLON = "photo_latlon";
		public static final String PHOTO_SYNCED = "photo_synced";
		public static final String PHOTO_TAKEN_DATE = "photo_taken_date";
	}

	public static synchronized CustomDatabaseHelper getInstance(Context context) {
		Log.i(TAG, "getInstance");
		if (sSingleton == null) {
			sSingleton = new CustomDatabaseHelper(context);
		}
		return sSingleton;
	}

	CustomDatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		Log.i(TAG, "Creating OpenHelper");

		Resources resources = context.getResources();

		mContext = context;
		int resourceId = resources.getIdentifier("unrestricted_packages",
				"array", context.getPackageName());
		if (resourceId != 0) {
			mUnrestrictedPackages = resources.getStringArray(resourceId);
		} else {
			mUnrestrictedPackages = new String[0];
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

		Log.i(TAG, "CustomDatabaseHelper.onCreate");
		

		db.execSQL("CREATE TABLE " + Tables.TRIP_DETAILS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
				+ TripDetails.PLACE_NAME + " COLLATE NOCASE, "
				+ TripDetails.START_DATE+ " TEXT , "
				+ TripDetails.END_DATE + " TEXT , "
				+ TripDetails.COMMENTS + " TEXT , "
				+ TripDetails.NUMBER_PHOTOS + " TEXT" + ");");
		
		
		db.execSQL("CREATE TABLE " + Tables.TRIP_PLACES + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
				+ TripPlaces.PLACE_NAME + " TEXT , "
				+ TripPlaces.PLACE_DESCRIPTION+ " TEXT , "
				+ TripPlaces.PLACE_NUMBER_TIMES_VISITED + " TEXT , "
				+ TripPlaces.PLACE_RATING + " TEXT" + ");");
		
		ContentValues values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "JAIPUR");
		values.put(TripPlaces.PLACE_DESCRIPTION, "Pink City");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "2");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "MUMBAI");
		values.put(TripPlaces.PLACE_DESCRIPTION, "Finance Capital of India");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "3");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "DELHI");
		values.put(TripPlaces.PLACE_DESCRIPTION, "Capital of India");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "2");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "GOA");
		values.put(TripPlaces.PLACE_DESCRIPTION, "Fun and Beach");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "4");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "CHENNAI");
		values.put(TripPlaces.PLACE_DESCRIPTION, "");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "2");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		values = new ContentValues();
		values.put(TripPlaces.PLACE_NAME, "MYSORE");
		values.put(TripPlaces.PLACE_DESCRIPTION, "");
		values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "0");
		values.put(TripPlaces.PLACE_RATING, "3");
		db.insert(Tables.TRIP_PLACES, null, values);
		
		db.execSQL("CREATE TABLE " + Tables.PHOTOS + " ("
				+ BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT , "
				+ Photos.PHOTO_GALLERY_ID + " TEXT , "
				+ Photos.PHOTO_FACEBOOK_ID + " TEXT , "
				+ Photos.PHOTO_NAME + " TEXT , "
				+ Photos.PHOTO_LATLON+ " TEXT , "
				+ Photos.PHOTO_SYNCED + " TEXT , "
				+ Photos.PHOTO_TAKEN_DATE + " TEXT , "
				+ Photos.PHOTO_TRIP_ID + " TEXT" + ");");
		
		Log.i(TAG, "CustomDatabaseHelper.onCreate : Created tables ");

		mReopenDatabase = true;

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.i(TAG, "Upgrading from version " + oldVersion + " to " + newVersion
				+ ", data will be lost!");

		db.execSQL("DROP TABLE IF EXISTS " + Tables.TRIP_DETAILS + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TRIP_PLACES + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.PHOTOS + ";");
		onCreate(db);

	}

	@Override
	public synchronized SQLiteDatabase getWritableDatabase() {
		Log.i(TAG, "PhlogitDatabaseHelper.getWritableDatabase");
		SQLiteDatabase db = super.getWritableDatabase();
		if (mReopenDatabase) {
			mReopenDatabase = false;
			close();
			db = super.getWritableDatabase();
		}
		return db;
	}

	public void wipeData() {
		SQLiteDatabase db = getWritableDatabase();

		db.execSQL("DROP TABLE IF EXISTS " + Tables.TRIP_DETAILS + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.TRIP_PLACES + ";");
		db.execSQL("DROP TABLE IF EXISTS " + Tables.PHOTOS + ";");

		// Note: we are not removing reference data from Tables.NICKNAME_LOOKUP

		db.execSQL("VACUUM;");
	}

}
