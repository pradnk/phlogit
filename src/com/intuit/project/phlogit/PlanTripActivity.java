package com.intuit.project.phlogit;

import java.util.ArrayList;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripPlaces;

public class PlanTripActivity extends Activity {
	private EditText cityNameInput;
	private ListView addViews;
	ArrayList<String> places = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("Where are you going to?");
		setContentView(R.layout.plantrip);
		addViews = (ListView)findViewById(R.id.addViews);
		cityNameInput = (EditText)findViewById(R.id.cityNameInput);
		
		Button save = (Button)findViewById(R.id.save);
		save.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if(cityNameInput != null && TextUtils.isEmpty(cityNameInput.getText())) {
					Toast.makeText(PlanTripActivity.this, "Please select a city", Toast.LENGTH_LONG).show();
					return;
				}
				//TODO:If the city name is unique add it to our database.
				//Next time it will be displayed.
				Cursor cursor = getContentResolver().query(
						TripPlaces.CONTENT_URI, null, null, null, null);
				cursor.moveToFirst();
				
				boolean found = false;
				String foundId = "";
				int number = 0;
				while(!cursor.isAfterLast()) {
					String name = cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME));
					if(name.equals(cityNameInput.getText().toString())) {
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
					values.put(TripPlaces.PLACE_NAME, cityNameInput.getText()
							.toString());
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
				
				Intent intent = new Intent();
				intent.putExtra("CITY", cityNameInput.getText().toString());
				intent.setClass(PlanTripActivity.this, PlanTripActivity2.class);
				startActivity(intent);
				finish();
			}
		});
		
		cityNameInput.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				ArrayList<String> subPlaces = new ArrayList<String>();
				if(!TextUtils.isEmpty(cityNameInput.getText().toString())) {
					for(int i=0;i<places.size();i++) {
						if(cityNameInput.getText().toString().contains(places.get(i))) {
							subPlaces.add(places.get(i));
						}
					}
				}
				if(subPlaces.size() > 0) {
					PlacesAdapter adapter = new PlacesAdapter(PlanTripActivity.this, subPlaces);
					adapter.notifyDataSetChanged();
					addViews.setAdapter(adapter);
				} else {
					PlacesAdapter adapter = new PlacesAdapter(PlanTripActivity.this, places);
					adapter.notifyDataSetChanged();
					addViews.setAdapter(adapter);
				}
				return false;
			}
		});
		
		Button cancel = (Button)findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		readPlaces();
		
		addViews.setAdapter(new PlacesAdapter(this, places));
		addViews.setDivider(getResources().getDrawable(R.drawable.horizontal_separator));
		addViews.setCacheColorHint(0);
		addViews.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int pos,
					long duration) {
				cityNameInput.setText(places.get(pos));
			}
			
		});
	}

	private void readPlaces() {
		Cursor cursor = getContentResolver().query(
				TripPlaces.CONTENT_URI, null, null, null, null);
		cursor.moveToFirst();
		
		while(!cursor.isAfterLast()) {
			String name = cursor.getString(cursor.getColumnIndex(TripPlaces.PLACE_NAME));
			cursor.moveToNext();
			places.add(name);
		}
		cursor.close();
	}
	
	/**
     * Adapter for our image files.
     */
    private class PlacesAdapter extends BaseAdapter {

        private Context context;
		private ArrayList<String> places;
		private LayoutInflater layoutInflater;

        public PlacesAdapter(Context localContext, ArrayList<String> places) {
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
                convertView = (TextView)layoutInflater.inflate(R.layout.places_list_item, null);
                holder = new ViewHolder();
                holder.places = (TextView)convertView;
                holder.places.setTextSize(18);
                holder.places.setTypeface(Typeface.DEFAULT_BOLD);
                holder.places.setTextColor(Color.BLACK);
                convertView.setTag(holder);
            }
            else {     
                holder = (ViewHolder) convertView.getTag();
            }
            holder.places.setText(places.get(position));
            return convertView;
        }
    }
    
    class ViewHolder {
    	TextView places;
    }
}
