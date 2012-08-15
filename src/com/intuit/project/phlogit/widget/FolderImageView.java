package com.intuit.project.phlogit.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.intuit.project.phlogit.R;

public class FolderImageView extends ImageView {

	private Bitmap preview;

	public FolderImageView(Context context) {
		this(context, null);
	}
	
	public FolderImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
//		setImageResource(R.drawable.pictures_full);
		Rect rect = new Rect();
		getGlobalVisibleRect(rect);
		
		if(preview != null) {
			preview = Bitmap.createScaledBitmap(preview, 30, 30, false);
			canvas.drawBitmap(preview, 30, 30, null);
		}
	}

	public void setImagePreview(Bitmap preview) {
		this.preview = preview;
		postInvalidate();
		refreshDrawableState();
	}
	
}