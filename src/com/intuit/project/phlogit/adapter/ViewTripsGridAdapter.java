package com.intuit.project.phlogit.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.intuit.project.phlogit.R;
import com.intuit.project.phlogit.widget.FolderImageView;

public class ViewTripsGridAdapter extends BaseAdapter {

	private static List<String> mDescStrings = null;
	private Context mContext;
	private LayoutInflater layoutInflaterService;
	private Cursor cursor;
	private ArrayList<String> places;
	private ArrayList<String> photos;
	private ArrayList<String> fileName;
	private ArrayList<String> tripStartDate;

	public ViewTripsGridAdapter(Context c, boolean upcoming, ArrayList<String> places, ArrayList<String> photos, ArrayList<String> fileName, ArrayList<String> tripStartDate) {
		mContext = c;
		layoutInflaterService = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mDescStrings = new ArrayList<String>();
		this.places = places;
		this.photos = photos;
		this.fileName = fileName;
		this.tripStartDate = tripStartDate;
	}

	@Override
	public int getCount() {
		return photos.size();
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public static final int THUMBNAIL_HEIGHT = 60;
	public static final int THUMBNAIL_WIDTH = 46;
	
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
		if("0".equals(photos.get(position))) {
			image.setImageResource(R.drawable.pictures_empty);
		} else {
			String fileNameStr = fileName.get(position);
			Bitmap imageBitmap = BitmapFactory.decodeFile(fileNameStr);
			Float width  = new Float(imageBitmap.getWidth());
			Float height = new Float(imageBitmap.getHeight());
			imageBitmap = Bitmap.createScaledBitmap(imageBitmap, THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT, true);
			
			Resources r = mContext.getResources();
			Drawable[] layers = new Drawable[2];
			layers[0] = r.getDrawable(R.drawable.pictures_full);
			layers[1] = new BitmapDrawable(imageBitmap);
			LayerDrawable layerDrawable = new LayerDrawable(layers);
			layerDrawable.setLayerInset(1, 30, 10, 35, 20);
			image.setImageDrawable(layerDrawable);
		}
		if(tripStartDate != null && !TextUtils.isEmpty(tripStartDate.get(position))) {
			((TextView) itemView.findViewById(R.id.textView1))
				.setText(places.get(position)+"_"+tripStartDate.get(position)+"\n("+photos.get(position)+")");
		} else {
			((TextView) itemView.findViewById(R.id.textView1))
			.setText(places.get(position)+"\n("+photos.get(position)+")");
		}
		return itemView;

	}
	
	Bitmap getPreview(File file) {
	    BitmapFactory.Options bounds = new BitmapFactory.Options();
	    bounds.inJustDecodeBounds = true;
	    BitmapFactory.decodeFile(file.getPath(), bounds);
	    if ((bounds.outWidth == -1) || (bounds.outHeight == -1))
	        return null;

	    int originalSize = (bounds.outHeight > bounds.outWidth) ? bounds.outHeight
	            : bounds.outWidth;

	    BitmapFactory.Options opts = new BitmapFactory.Options();
	    opts.inSampleSize = originalSize / 1000;
	    return BitmapFactory.decodeFile(file.getPath(), opts);     
	}
}