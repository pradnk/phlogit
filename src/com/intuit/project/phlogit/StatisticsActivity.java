package com.intuit.project.phlogit;

import java.util.ArrayList;
import java.util.List;

import org.achartengine.ChartFactory;
import org.achartengine.renderer.DefaultRenderer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripPlaces;
import com.intuit.project.phlogit.util.AbstractDemoChart;
import com.intuit.project.phlogit.util.IDemoChart;
import com.markupartist.android.widget.ActionBar;
import com.markupartist.android.widget.ActionBar.Action;

public class StatisticsActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.statistics);
		
		TextView noOfPhotos = (TextView)findViewById(R.id.noOfPhotos);
		TextView noOfPhotosSynced = (TextView)findViewById(R.id.noOfPhotosSynced);
		TextView noOfPhotosNotSynced = (TextView)findViewById(R.id.noOfPhotosNotSynced);
		TextView noOfAlbums = (TextView)findViewById(R.id.noOfAlbums);
		TextView mostVisitedPlace = (TextView)findViewById(R.id.mostVisitedPlace);
		ImageView showGraph = (ImageView)findViewById(R.id.graph);
		
		// ******** Start of Action Bar configuration
		ActionBar actionbar = (ActionBar) findViewById(R.id.actionBar1);
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
		actionbar.setTitle("Statistics");
		// ******** End of Action Bar configuration

		showGraph.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				addCharts();				
			}
		});
		
		int counter = 0;
		Cursor c = getContentResolver().query(TripDetails.CONTENT_URI, null, null, null, null);
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				counter++;
				c.moveToNext();
			}
		}
		c.close();
		noOfAlbums.setText("Number of Albums : "+counter);
		counter = 0;
		c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				counter++;
				c.moveToNext();
			}
		}
		c.close();
		int totalPhotos = counter;
		noOfPhotos.setText("Number of Photos : "+totalPhotos);
		
		c = getContentResolver().query(TripPlaces.CONTENT_URI, null, null, null, TripPlaces.PLACE_NUMBER_TIMES_VISITED+" DESC");
		String mostVisitedPlaceStr = "";
		String number = "";
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			String name = c.getString(c.getColumnIndex(TripPlaces.PLACE_NAME));
			number = c.getString(c.getColumnIndex(TripPlaces.PLACE_NUMBER_TIMES_VISITED));
			if("1".equals(number)) {
				mostVisitedPlaceStr = "N/A";
			} else {
				mostVisitedPlaceStr = name;
			}
		}
		c.close();
		if("N/A".equals(mostVisitedPlaceStr)) {
			mostVisitedPlace.setText("Most Visited Place : "+mostVisitedPlaceStr);
		} else {
			mostVisitedPlace.setText("Most Visited Place : "+mostVisitedPlaceStr + " "+number+" Visits ");
		}
		
		counter = 0;
		c = getContentResolver().query(Photos.CONTENT_URI, null, null, null, null);
		if(c != null && c.getCount() > 0) {
			c.moveToFirst();
			while(!c.isAfterLast()) {
				String synced = c.getString(c.getColumnIndex(Photos.PHOTO_SYNCED));
				if("true".equals(synced)) {
					counter++;
				}
				c.moveToNext();
			}
		}
		c.close();
		noOfPhotosSynced.setText("Number of Synced Photos : "+counter);
		noOfPhotosNotSynced.setText("Number of UnSynced Photos : "+(totalPhotos - counter));
	}

	private void addCharts() {
		IDemoChart chart = new BudgetPieChart();
		Intent intent = chart.execute(this);
		startActivity(intent);
	}
	
	class BudgetPieChart extends AbstractDemoChart {
		  /**
		   * Returns the chart name.
		   * 
		   * @return the chart name
		   */
		  public String getName() {
		    return "Budget chart";
		  }

		  /**
		   * Returns the chart description.
		   * 
		   * @return the chart description
		   */
		  public String getDesc() {
		    return "The budget per project for this year (pie chart)";
		  }

		  /**
		   * Executes the chart demo.
		   * 
		   * @param context the context
		   * @return the built intent
		   */
		  public Intent execute(Context context) {
			List<Double> values = new ArrayList<Double>();
			List<String> titles = new ArrayList<String>();
			    
			Cursor c = getContentResolver().query(TripDetails.CONTENT_URI, null, null, null, null);
			if(c != null && c.getCount() > 0) {
				c.moveToFirst();
				while(!c.isAfterLast()) {
					titles.add(c.getString(c.getColumnIndex(TripDetails.PLACE_NAME)));
					values.add(Double.parseDouble(c.getString(c.getColumnIndex(TripDetails.NUMBER_PHOTOS))));
					c.moveToNext();
				}
			}
			c.close();
			  
			int[] randomColors = new int[]{Color.BLUE, Color.WHITE, Color.CYAN, Color.MAGENTA, Color.RED, Color.DKGRAY, R.color.actionbar_background_start, Color.GREEN};
			
		    List<Integer> colors = new ArrayList<Integer>();
		    for(int i=0;i<titles.size();i++) {
		    	int random = (int) (Math.random() * randomColors.length);
		    	colors.add(randomColors[random]);
		    }
		    DefaultRenderer renderer = buildCategoryRenderer(colors);
		    renderer.setZoomButtonsVisible(true);
		    renderer.setZoomEnabled(true);
		    renderer.setChartTitleTextSize(20);
		    return ChartFactory.getPieChartIntent(context, buildCategoryDataset("City", titles, values),
		        renderer, "Budget");
		  }

		}
}
