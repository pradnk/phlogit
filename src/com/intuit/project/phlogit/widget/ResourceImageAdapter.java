package com.intuit.project.phlogit.widget;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.intuit.project.phlogit.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;

/**
 * This class is an adapter that provides images from a fixed set of resource
 * ids. Bitmaps and ImageViews are kept as weak references so that they can be
 * cleared by garbage collection when not needed.
 * 
 */
public class ResourceImageAdapter extends AbstractCoverFlowImageAdapter {

    /** The Constant TAG. */
    private static final String TAG = ResourceImageAdapter.class.getSimpleName();

    /** The width. */
    private float width = 0;

    /** The height. */
    private float height = 0;

    /** The bitmap map. */
    private final Map<Integer, WeakReference<Bitmap>> bitmapMap = new HashMap<Integer, WeakReference<Bitmap>>();

	private ArrayList<File> photos;

    /**
     * Creates the adapter with default set of resource images.
     * 
     * @param context
     *            context
     */
    public ResourceImageAdapter(final Context context, ArrayList<File> photos) {
        super();
        this.photos = photos;
    }
    
    /**
     * Set width for all pictures.
     * 
     * @param width
     *            picture height
     */
    public synchronized void setWidth(final float width) {
        this.width = width;
    }

    /**
     * Set height for all pictures.
     * 
     * @param height
     *            picture height
     */
    public synchronized void setHeight(final float height) {
        this.height = height;
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getCount()
     */
    @Override
    public synchronized int getCount() {
        return photos.size();
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
	
	@Override
    public final Bitmap getItem(final int position) {
        final WeakReference<Bitmap> weakBitmapReference = bitmapMap.get(position);
        if (weakBitmapReference != null) {
            final Bitmap bitmap = weakBitmapReference.get();
            if (bitmap == null) {
                Log.v(TAG, "Empty bitmap reference at position: " + position + ":" + this);
            } else {
                Log.v(TAG, "Reusing bitmap item at position: " + position + ":" + this);
                return bitmap;
            }
        }
        Log.v(TAG, "Creating item at position: " + position + ":" + this);
        final Bitmap bitmap = getPreview(photos.get(position));
        bitmapMap.put(position, new WeakReference<Bitmap>(bitmap));
        Log.v(TAG, "Created item at position: " + position + ":" + this);
        return bitmap;
    }
	
	/*
     * (non-Javadoc)
     * 
     * @see android.widget.Adapter#getView(int, android.view.View,
     * android.view.ViewGroup)
     */
    @Override
    public final synchronized ImageView getView(final int position, final View convertView, final ViewGroup parent) {
        ImageView imageView;
        if (convertView == null) {
            final Context context = parent.getContext();
            Log.v(TAG, "Creating Image view at position: " + position + ":" + this);
            imageView = new ImageView(context);
            imageView.setLayoutParams(new CoverFlow.LayoutParams((int) width, (int) height));
        } else {
            Log.v(TAG, "Reusing view at position: " + position + ":" + this);
            imageView = (ImageView) convertView;
        }
        Bitmap bitmap = getItem(position);
        imageView.setImageBitmap(bitmap);
        return imageView;
    }
}