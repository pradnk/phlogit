package com.intuit.project.phlogit.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.intuit.project.phlogit.R;

public class HomeScreenGridAdapter extends BaseAdapter {

	private static final int[] mThumbIds = { R.drawable.ic_home_plan_trip,
			R.drawable.ic_home_view_albums, R.drawable.ic_home_timeline, R.drawable.ic_home_settings, R.drawable.ic_home_statistics, R.drawable.ic_home_about};
	private static String[] mDescStrings = null;
	private Context mContext;
	private LayoutInflater layoutInflaterService;

	public HomeScreenGridAdapter(Context c) {
		mContext = c;
		layoutInflaterService = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDescStrings = new String[]{ "Plan Trip", "View Albums",
				"Timeline View", "Settings", "Statistics", "About"};
	}

	@Override
	public int getCount() {
		return mThumbIds.length;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View itemView;
		if (convertView == null) { // if it's not recycled, initialize some
									// attributes
			itemView = layoutInflaterService.inflate(
					R.layout.home_screen_grid_item, null);
			GridView.LayoutParams layout = new GridView.LayoutParams(200, GridView.AUTO_FIT);
			itemView.setLayoutParams(layout);
		} else {
			itemView = convertView;
		}
		ImageView image = ((ImageView) itemView.findViewById(R.id.imageView1));
		image.setImageResource(mThumbIds[position]);
		
		((TextView) itemView.findViewById(R.id.textView1))
				.setText(mDescStrings[position]);

		return itemView;

	}
}