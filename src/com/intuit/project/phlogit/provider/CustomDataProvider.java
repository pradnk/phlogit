package com.intuit.project.phlogit.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Tables;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripPlaces;

public class CustomDataProvider extends CustomContentProvider {

	private static final String TAG = "PhlogitProvider";
	private static final boolean VERBOSE_LOGGING = Log.isLoggable(TAG,
			Log.VERBOSE);

	public static final String AUTHORITY = "com.intuit.project.phlogit";
	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	private static final UriMatcher sUriMatcher = new UriMatcher(
			UriMatcher.NO_MATCH);

	private static final HashMap<String, String> sCountProjectionMap;
	private static final HashMap<String, String> sTripDetailsProjectionMap;
	private static final HashMap<String, String> sTripPlacesProjectionMap;
	private static final HashMap<String, String> sPhotosProjectionMap;

	private volatile CountDownLatch mAccessLatch;

	private static final int TRIP_DETAILS = 1000;
	private static final int TRIP_PLACES = 1001;
	private static final int PHOTOS = 1002;

	static {
		final UriMatcher matcher = sUriMatcher;
		matcher.addURI(AUTHORITY, Tables.TRIP_DETAILS, TRIP_DETAILS);
		matcher.addURI(AUTHORITY, Tables.TRIP_PLACES, TRIP_PLACES);
		matcher.addURI(AUTHORITY, Tables.PHOTOS, PHOTOS);
	}

	static {

		sCountProjectionMap = new HashMap<String, String>();
		sCountProjectionMap.put(BaseColumns._COUNT, "COUNT(*)");

		sTripDetailsProjectionMap = new HashMap<String, String>();
		sTripDetailsProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

		sTripDetailsProjectionMap.put(TripDetails.PLACE_NAME,
				TripDetails.PLACE_NAME);
		
		sTripDetailsProjectionMap.put(TripDetails.START_DATE,
				TripDetails.START_DATE);

		sTripDetailsProjectionMap.put(TripDetails.END_DATE,
				TripDetails.END_DATE);
		
		sTripDetailsProjectionMap.put(TripDetails.COMMENTS,
				TripDetails.COMMENTS);
		
		sTripDetailsProjectionMap.put(TripDetails.NUMBER_PHOTOS,
				TripDetails.NUMBER_PHOTOS);
		
		sTripPlacesProjectionMap = new HashMap<String, String>();
		sTripPlacesProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

		sTripPlacesProjectionMap.put(TripPlaces.PLACE_NAME,
				TripPlaces.PLACE_NAME);
		
		sTripPlacesProjectionMap.put(TripPlaces.PLACE_DESCRIPTION,
				TripPlaces.PLACE_DESCRIPTION);

		sTripPlacesProjectionMap.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED,
				TripPlaces.PLACE_NUMBER_TIMES_VISITED);
		
		sTripPlacesProjectionMap.put(TripPlaces.PLACE_RATING,
				TripPlaces.PLACE_RATING);
		
		
		sPhotosProjectionMap = new HashMap<String, String>();
		sPhotosProjectionMap.put(BaseColumns._ID, BaseColumns._ID);

		sPhotosProjectionMap.put(Photos.PHOTO_FACEBOOK_ID,
				Photos.PHOTO_FACEBOOK_ID);
		
		sPhotosProjectionMap.put(Photos.PHOTO_GALLERY_ID,
				Photos.PHOTO_GALLERY_ID);
		
		sPhotosProjectionMap.put(Photos.PHOTO_NAME,
				Photos.PHOTO_NAME);
		
		sPhotosProjectionMap.put(Photos.PHOTO_LATLON,
				Photos.PHOTO_LATLON);
		
		sPhotosProjectionMap.put(Photos.PHOTO_SYNCED,
				Photos.PHOTO_SYNCED);
		
		sPhotosProjectionMap.put(Photos.PHOTO_TAKEN_DATE,
				Photos.PHOTO_TAKEN_DATE);
		
		sPhotosProjectionMap.put(Photos.PHOTO_TRIP_ID,
				Photos.PHOTO_TRIP_ID);
	}

	@Override
	protected SQLiteOpenHelper getDatabaseHelper(Context context) {
		return CustomDatabaseHelper.getInstance(context);
	}

	@Override
	protected Uri insertInTransaction(Uri uri, ContentValues values) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "insertInTransaction: " + uri);
		}
		final int match = sUriMatcher.match(uri);
		long id = 0;

		switch (match) {
			case TRIP_DETAILS: {
				id = mDb.insert(Tables.TRIP_DETAILS, null, values);
				break;
			}
			
			case TRIP_PLACES: {
				id = mDb.insert(Tables.TRIP_PLACES, null, values);
				break;
			}
			
			case PHOTOS: {
				id = mDb.insert(Tables.PHOTOS, null, values);
				break;
			}
		}

		return ContentUris.withAppendedId(uri, id);
	}

	@Override
	protected int updateInTransaction(Uri uri, ContentValues values,
			String selection, String[] selectionArgs) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "updateInTransaction: " + uri);
		}

		int count = 0;

		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TRIP_DETAILS: {
				count = mDb.update(Tables.TRIP_DETAILS, values, selection,
						selectionArgs);
				break;
			}
			
			case TRIP_PLACES: {
				count = mDb.update(Tables.TRIP_PLACES, values, selection,
						selectionArgs);
				break;
			}
			
			case PHOTOS: {
				count = mDb.update(Tables.PHOTOS, values, selection,
						selectionArgs);
				break;
			}
		}
		return count;
	}

	@Override
	protected int deleteInTransaction(Uri uri, String selection,
			String[] selectionArgs) {

		int count = 0;
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "deleteInTransaction: " + uri + "\n selection = "
					+ selection);
		}
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TRIP_DETAILS: {
				count = mDb.delete(Tables.TRIP_DETAILS, selection, selectionArgs);
				break;
			}
			
			case TRIP_PLACES: {
				count = mDb.delete(Tables.TRIP_PLACES, selection, selectionArgs);
				break;
			}
			
			case PHOTOS: {
				count = mDb.delete(Tables.PHOTOS, selection, selectionArgs);
				break;
			}
		}
		return count;
	}

	@Override
	protected void notifyChange() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getType(Uri uri) {
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TRIP_DETAILS: {
				return TripDetails.CONTENT_TYPE;
			}
			
			case TRIP_PLACES: {
				return TripPlaces.CONTENT_TYPE;
			}
			
			case PHOTOS: {
				return Photos.CONTENT_TYPE;
			}
		}
		return null;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (VERBOSE_LOGGING) {
			Log.v(TAG, "query: " + uri);
		}

		final SQLiteDatabase db = mDbHelper.getReadableDatabase();

		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		String groupBy = null;
		String limit = getLimit(uri);

		// TODO: Consider writing a test case for RestrictionExceptions when you
		// write a new query() block to make sure it protects restricted data.
		final int match = sUriMatcher.match(uri);
		switch (match) {
			case TRIP_DETAILS: {
				qb.setTables(Tables.TRIP_DETAILS);
				qb.setProjectionMap(sTripDetailsProjectionMap);
	
				break;
			}
			
			case TRIP_PLACES: {
				qb.setTables(Tables.TRIP_PLACES);
				qb.setProjectionMap(sTripPlacesProjectionMap);
	
				break;
			}
			
			case PHOTOS: {
				qb.setTables(Tables.PHOTOS);
				qb.setProjectionMap(sPhotosProjectionMap);
	
				break;
			}
		}
		return query(db, qb, projection, selection, selectionArgs, sortOrder,
				groupBy, limit);
	}

	private Cursor query(final SQLiteDatabase db, SQLiteQueryBuilder qb,
			String[] projection, String selection, String[] selectionArgs,
			String sortOrder, String groupBy, String limit) {
		if (projection != null && projection.length == 1
				&& BaseColumns._COUNT.equals(projection[0])) {
			qb.setProjectionMap(sCountProjectionMap);
		}
		final Cursor c = qb.query(db, projection, selection, selectionArgs,
				groupBy, null, sortOrder, limit);
		if (c != null) {
			c.setNotificationUri(getContext().getContentResolver(),
					AUTHORITY_URI);
		}
		return c;
	}

	private CustomDatabaseHelper mDbHelper;

	@Override
	public boolean onCreate() {
		super.onCreate();
		try {
			return initialize();
		} catch (RuntimeException e) {
			Log.e(TAG, "Cannot start provider", e);
			return false;
		}
	}

	private boolean initialize() {
		// final Context context = getContext();
		mDbHelper = (CustomDatabaseHelper) getDatabaseHelper();

		final SQLiteDatabase db = mDbHelper.getReadableDatabase();

		return true;
	}

	private void waitForAccess() {
		CountDownLatch latch = mAccessLatch;
		if (latch != null) {
			while (true) {
				try {
					latch.await();
					mAccessLatch = null;
					return;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			}
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		Log.i(TAG, "insert " + uri + "\n ContentValues " + values);
		waitForAccess();
		return super.insert(uri, values);
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		Log.i(TAG, "update " + uri + "\n ContentValues = " + values
				+ "\n selection =" + selection);
		waitForAccess();
		return super.update(uri, values, selection, selectionArgs);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		Log.i(TAG, "delete " + uri + "\n selection =" + selection);
		waitForAccess();
		return super.delete(uri, selection, selectionArgs);
	}

	@Override
	public ContentProviderResult[] applyBatch(
			ArrayList<ContentProviderOperation> operations)
			throws OperationApplicationException {
		waitForAccess();
		return super.applyBatch(operations);
	}

	/* package */void wipeData() {
		mDbHelper.wipeData();
	}

	/**
	 * Gets the value of the "limit" URI query parameter.
	 * 
	 * @return A string containing a non-negative integer, or <code>null</code>
	 *         if the parameter is not set, or is set to an invalid value.
	 */
	private String getLimit(Uri url) {
		String limitParam = url.getQueryParameter("limit");
		if (limitParam == null) {
			return null;
		}
		// make sure that the limit is a non-negative integer
		try {
			int l = Integer.parseInt(limitParam);
			if (l < 0) {
				Log.w(TAG, "Invalid limit parameter: " + limitParam);
				return null;
			}
			return String.valueOf(l);
		} catch (NumberFormatException ex) {
			Log.w(TAG, "Invalid limit parameter: " + limitParam);
			return null;
		}
	}
}
