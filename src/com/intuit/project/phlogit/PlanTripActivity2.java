package com.intuit.project.phlogit;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Filterable;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripPlaces;
import com.intuit.project.phlogit.util.Utility;

public class PlanTripActivity2 extends Activity {

	private LayoutInflater inflater;
	protected Dialog dialog;
	private ArrayList<String> places = new ArrayList<String>();
	protected String city;
	protected TextView dialogInfoView;
	private AutoCompleteTextView destination;
	private EditText from;
	private EditText to;
	private EditText comments;
	private String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.plantrip2);
		
		inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		
		destination = (AutoCompleteTextView)findViewById(R.id.destination); 
		from = (EditText)findViewById(R.id.from);
		to = (EditText)findViewById(R.id.to);
		comments = (EditText)findViewById(R.id.comments);
		final Date now = new Date();
		final Calendar cal = Calendar.getInstance();
		cal.setTime(now);
		
		destination.setThreshold(1);
		Cursor cursor = getContentResolver().query(
				TripPlaces.CONTENT_URI, null, null, null, null);
		
		destination.setAdapter(new PlacesAdapter(this, cursor));
		final InputMethodManager imm = (InputMethodManager)
				getSystemService(Context.INPUT_METHOD_SERVICE);
		from.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if(hasFocus) {
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
					DatePickerDialog dialog = new DatePickerDialog(PlanTripActivity2.this, new DatePickerDialog.OnDateSetListener() {
						
						@Override
						public void onDateSet(DatePicker view, int year, int monthOfYear,
								int dayOfMonth) {
							int month = (monthOfYear + 1);
							String monthStr = String.valueOf(month);
							if(month < 10) {
								monthStr = "0"+month;
							}
							from.setText(year+"-"+monthStr+"-"+dayOfMonth);
						}
					}, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
					dialog.show();
					
				}
			}
		});
		
		Button save = (Button)findViewById(R.id.save);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(!validate()) {
					Toast.makeText(PlanTripActivity2.this, message, Toast.LENGTH_LONG).show();
					return;
				}
				city = destination.getText().toString();
				final View view = inflater.inflate(R.layout.save_dialog_trip, null);
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
				final String fromStr = from.getText().toString();
				Calendar cal = Calendar.getInstance();
				try {
					cal.setTime(dateFormat.parse(fromStr));
				} catch (ParseException e) {
					e.printStackTrace();
				}
				cal.add(Calendar.DATE, Integer.parseInt(to.getText().toString()));
				Date endDate = cal.getTime();
				
				final String toStr = dateFormat.format(endDate);
				
				final String commentsStr = comments.getText().toString();
				popuplateUI(view, city, fromStr, toStr);
				view.findViewById(R.id.saveTrip).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						saveTripDetails(city, fromStr, toStr, commentsStr);
						Toast.makeText(PlanTripActivity2.this, "Your trip details to "+city+" saved.", Toast.LENGTH_LONG).show();
						dialog.dismiss();
						finish();
					}
				});
				view.findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
					
					@Override
					public void onClick(View v) {
						if(dialog != null && dialog.isShowing()) {
							dialog.dismiss();
						}
					}
				});
				
				dialog = new Dialog(PlanTripActivity2.this, R.style.TranslucentThemeDialog);
				LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
				params.setMargins(10, 10, 10, 10);
				dialog.addContentView(view, params);
				dialog.getWindow().setLayout(
						Utility.getDialogWidth(PlanTripActivity2.this, getWindowManager().getDefaultDisplay()
								.getWidth(), getWindowManager().getDefaultDisplay().getHeight()),
						LayoutParams.WRAP_CONTENT);
				dialog.show();
			}

			private void popuplateUI(final View view, final String city, final String from, final String to) {
				((TextView)view.findViewById(R.id.destination)).setText(city);
				((TextView)view.findViewById(R.id.startDate)).setText(from);
				((TextView)view.findViewById(R.id.endDate)).setText(to);
			}
		});
		
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
	}
	
	protected boolean validate() {
		boolean flag = true;
		String destStr = destination.getText().toString();
		String fromStr = from.getText().toString();
		String toStr = to.getText().toString();
		if(TextUtils.isEmpty(destStr) ||
				TextUtils.isEmpty(fromStr) ||
				TextUtils.isEmpty(toStr)) {
			message = "Enter valid values for all mandatory fields";
			return false;
		}
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		Date startDate = null;
		try {
			startDate = dateFormat.parse(fromStr);
			cal.setTime(startDate);
		} catch (ParseException e) {
			message = "Start Date in wrong format. Use yyyy-MM-dd only";
			return false;
		}
		cal.add(Calendar.DATE, Integer.parseInt(to.getText().toString()));
		Date endDate = cal.getTime();
		
		Cursor c = getContentResolver().query(
				TripDetails.CONTENT_URI,
				new String[] { TripDetails._ID, TripDetails.START_DATE,
						TripDetails.END_DATE }, null, null, null);
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
			while(!c.isAfterLast()) {
				String startDateStr = c.getString(c.getColumnIndex(TripDetails.START_DATE));
				String endDateStr = c.getString(c.getColumnIndex(TripDetails.END_DATE));
				Date nStartDate = null;
				Date nEndDate = null;
				try {
					nStartDate = format.parse(startDateStr);
					nEndDate = format.parse(endDateStr);
				} catch (ParseException e) {
					//Ignore
				}
				if(startDate.equals(nStartDate) || (startDate.after(nStartDate) && startDate.before(nEndDate)) ||
						(endDate.after(nStartDate) && endDate.before(nEndDate)) || endDate.equals(nStartDate)) {
					flag = false;
					message = "There is a clash with another trip. Choose valid dates";
					break;
				}
				c.moveToNext();
			}
			c.close();
		}
		return flag;
	}

	protected void addCityIfUniqueElseSave(String value) {
		Cursor cursor = getContentResolver().query(
				TripPlaces.CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		
		boolean found = false;
		String foundId = "";
		int number = 0;
		while(!cursor.isAfterLast()) {
			String name = cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME));
			if(name.equalsIgnoreCase(value)) {
				foundId = cursor.getString(cursor.getColumnIndex(TripPlaces._ID));
				number = cursor.getInt(cursor.getColumnIndex(TripPlaces.PLACE_NUMBER_TIMES_VISITED));
				found = true;
				break;
			}
			cursor.moveToNext();
		}
		cursor.close();
		
		if (!found) {
			ContentValues values = new ContentValues();
			values.put(TripPlaces.PLACE_NAME, value.toUpperCase());
			values.put(TripPlaces.PLACE_DESCRIPTION, "");
			values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, "1");
			values.put(TripPlaces.PLACE_RATING, "2");
			getContentResolver().insert(TripPlaces.CONTENT_URI, values);
		} else {
			ContentValues values = new ContentValues();
			values.put(TripPlaces.PLACE_NUMBER_TIMES_VISITED, ""
					+ (number + 1));
			getContentResolver().update(TripPlaces.CONTENT_URI, values, "_id = ?", new String[]{foundId});
		}		
	}

	private void saveTripDetails(final String city, final String from, final String to, final String comments) {
		addCityIfUniqueElseSave(city);
		
		ContentValues values = new ContentValues();
		values.put(TripDetails.PLACE_NAME, city);
		values.put(TripDetails.START_DATE, from);
		values.put(TripDetails.END_DATE, to);
		values.put(TripDetails.COMMENTS, comments);
		values.put(TripDetails.NUMBER_PHOTOS, "0");
		getContentResolver().insert(TripDetails.CONTENT_URI, values);
	}
	
	/**
     * Adapter for our image files.
     */
    private class PlacesAdapter extends CursorAdapter implements Filterable {

		private LayoutInflater layoutInflater;
		private ContentResolver mCR;

        public PlacesAdapter(Context localContext, Cursor c) {
        	super(localContext, c);
            this.layoutInflater = (LayoutInflater)localContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mCR = localContext.getContentResolver();
        }

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			((TextView)view).setText(cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME)));
		}
		
		@Override
		public String convertToString(Cursor cursor) {
			return cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
            final TextView textView = (TextView)layoutInflater.inflate(R.layout.places_list_item, null);
            textView.setText(cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME)));
            return textView;
		}
		
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			System.out.println("CO: " + constraint);
			if (getFilterQueryProvider() != null) {
				return getFilterQueryProvider().runQuery(constraint);
			}

			StringBuilder buffer = null;
			String[] args = null;
			if (constraint != null) {
				buffer = new StringBuilder();
				buffer.append(TripPlaces.PLACE_NAME);
				buffer.append(" GLOB ?");
				args = new String[] {
					constraint.toString().toUpperCase() + "*"
				};
			}

			Cursor c = mCR
					.query(TripPlaces.CONTENT_URI, new String[]{TripPlaces._ID, TripPlaces.PLACE_NAME},
							buffer == null ? null : buffer.toString(), args, "place_name COLLATE NOCASE");
			return c;
		}
    }
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    }
}