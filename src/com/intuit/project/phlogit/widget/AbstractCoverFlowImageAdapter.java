package com.intuit.project.phlogit.widget;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

/**
 * This class is an adapter that provides base, abstract class for images
 * adapter.
 * 
 */
public abstract class AbstractCoverFlowImageAdapter extends BaseAdapter {

    /** The Constant TAG. */
    private static final String TAG = AbstractCoverFlowImageAdapter.class.getSimpleName();

    /** The width. */
    private float width = 0;

    /** The height. */
    private float height = 0;

    /** The bitmap map. */
    private final Map<Integer, WeakReference<Bitmap>> bitmapMap = new HashMap<Integer, WeakReference<Bitmap>>();

    public AbstractCoverFlowImageAdapter() {
        super();
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
     * @see android.widget.Adapter#getItemId(int)
     */
    @Override
    public final synchronized long getItemId(final int position) {
        return position;
    }
}