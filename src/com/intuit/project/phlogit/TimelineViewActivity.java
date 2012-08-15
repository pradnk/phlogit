package com.intuit.project.phlogit;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.TextView;

import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.widget.CoverFlow;
import com.intuit.project.phlogit.widget.ResourceImageAdapter;

/**
 * The Class CoverFlowTestingActivity.
 */
public class TimelineViewActivity extends Activity {

    private TextView yearView;
	private ArrayList<File> photos;
	private ArrayList<String> dates;
	private TextView monthView;
	private TextView dayView;
	private ViewGroup timelineView;
	private ViewGroup emptyView;
	private CoverFlow coverFlow1;
	private ProgressDialog dialog;
	private ViewGroup rootView;

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.timeline);
        timelineView = (ViewGroup) findViewById(R.id.timelineView);
        emptyView = (ViewGroup) findViewById(R.id.emptyView);
        rootView = (ViewGroup) findViewById(R.id.rootView);
        yearView = (TextView) findViewById(R.id.yearText);
        monthView = (TextView) findViewById(R.id.monthText);
        dayView = (TextView) findViewById(R.id.dateText);
        // note resources below are taken using getIdentifier to allow importing
        // this library as library.
        coverFlow1 = (CoverFlow) findViewById(this.getResources().getIdentifier("coverflow", "id",
                "com.intuit.project.phlogit"));
        
        coverFlow1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> adapter, View v,
					int pos, long duration) {
				if(v!=null)
                {
					if(pos == adapter.getSelectedItemPosition()) {
						v.setLayoutParams(new Gallery.LayoutParams(200, 250)); 
					}
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				//Ignore
			}
        	
		});
        
        dialog = ProgressDialog.show(this, "", "Loading Timeline", true, true, null);
        
        LoadPhotosTask task = new LoadPhotosTask();
        task.execute(null);
    }

    /**
     * Setup cover flow.
     * 
     * @param mCoverFlow
     *            the m cover flow
     * @param reflect
     *            the reflect
     */
    private void setupCoverFlow(final CoverFlow mCoverFlow, final boolean reflect) {
        BaseAdapter coverImageAdapter = new ResourceImageAdapter(this, photos);
        mCoverFlow.setAdapter(coverImageAdapter);
        mCoverFlow.setSelection(0);
        mCoverFlow.setSelected(true);
        mCoverFlow.performClick();
        setupListeners(mCoverFlow);
    }

    /**
     * Sets the up listeners.
     * 
     * @param mCoverFlow
     *            the new up listeners
     */
    private void setupListeners(final CoverFlow mCoverFlow) {
        mCoverFlow.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(final AdapterView< ? > parent, final View view, final int position, final long id) {
            	formatTimelineDate(position);
            }

        });
        mCoverFlow.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(final AdapterView< ? > parent, final View view, final int position, final long id) {
            	formatTimelineDate(position);
            }

            @Override
            public void onNothingSelected(final AdapterView< ? > parent) {
                //Ignore
            }
        });
    }

	private void formatTimelineDate(final int position) {
		String dateStr = dates.get(position);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
		Date date = null;
		try {
			date = format.parse(dateStr);
		} catch (ParseException e) {
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		yearView.setText(""+cal.get(Calendar.YEAR));
		monthView.setText(getMonthName(cal.get(Calendar.MONTH)));
		dayView.setText(""+cal.get(Calendar.DAY_OF_MONTH));
	}

	private CharSequence getMonthName(int month) {
		switch(month) {
			case Calendar.JANUARY:
				return "January";
			case Calendar.FEBRUARY:
				return "February";
			case Calendar.MARCH:
				return "March";
			case Calendar.APRIL:
				return "April";
			case Calendar.MAY:
				return "May";
			case Calendar.JUNE:
				return "June";
			case Calendar.JULY:
				return "July";
			case Calendar.AUGUST:
				return "August";
			case Calendar.SEPTEMBER:
				return "September";
			case Calendar.OCTOBER:
				return "October";
			case Calendar.NOVEMBER:
				return "November";
			case Calendar.DECEMBER:
				return "December";
			default:
				break;
		}
		return "";
	}
	
	class LoadPhotosTask extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			Cursor cursor = getContentResolver().query(Photos.CONTENT_URI, null, null, null, Photos.PHOTO_TAKEN_DATE);
	        cursor.moveToFirst();
	        
	        photos = new ArrayList<File>();
	        dates = new ArrayList<String>();
	        
	        while(!cursor.isAfterLast()) {
	        	String path = cursor.getString(cursor.getColumnIndex(Photos.PHOTO_GALLERY_ID));
	        	String date = cursor.getString(cursor.getColumnIndex(Photos.PHOTO_TAKEN_DATE));
	        	File file = new File(path);
	        	
	        	photos.add(file);
	        	dates.add(date);
	        	
	        	cursor.moveToNext();
	        }
	        cursor.close();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			
			if(photos.size() > 0) {
	        	setupCoverFlow(coverFlow1, false);
	        } else {
	        	setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
	        	timelineView.setVisibility(View.GONE);
	        	emptyView.startAnimation(AnimationUtils.loadAnimation(TimelineViewActivity.this, R.anim.zoom_center));
	        	coverFlow1.setVisibility(View.GONE);
	        	emptyView.setVisibility(View.VISIBLE);
	        }	
			if(dialog != null && dialog.isShowing()) {
				dialog.dismiss();
			}
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	    System.gc();
	}
}