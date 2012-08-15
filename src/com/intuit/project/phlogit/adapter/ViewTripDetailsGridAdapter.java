package com.intuit.project.phlogit.adapter;

import java.io.File;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.intuit.project.phlogit.R;

public class ViewTripDetailsGridAdapter extends BaseAdapter {

	private Context mContext;
	private ArrayList<File> photos;
	private ArrayList<String> synced;
	Drawable[] layers = new Drawable[2];
	
	public ViewTripDetailsGridAdapter(Context c, ArrayList<File> photos, ArrayList<String> synced) {
		mContext = c;
		this.photos = photos;
		this.synced = synced;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ImageView picturesView;
		if (convertView == null) { // if it's not recycled, initialize some vars
			picturesView = new ImageView(mContext);
		} else {
			picturesView = (ImageView) convertView;
		}
		
		layers[0] = new BitmapDrawable(getPreview(photos.get(position)));
		layers[1] = new BitmapDrawable();
		if(!"true".equals(synced.get(position))) {
			layers[1] = new BitmapDrawable(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image_sync_indicator));
		}
		LayerDrawable layerDrawable = new LayerDrawable(layers);
		layerDrawable.setLayerInset(1, layers[0].getIntrinsicWidth() - 10, layers[0].getIntrinsicHeight() - 10, 0, 0);
		picturesView.setImageDrawable(layerDrawable);
		picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
		GridView.LayoutParams layout = new GridView.LayoutParams(100, 100);
		picturesView.setLayoutParams(layout);
		return picturesView;

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
	    opts.inSampleSize = originalSize / 100;
	    return BitmapFactory.decodeFile(file.getPath(), opts);     
	}
}