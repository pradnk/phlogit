package com.intuit.project.phlogit.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

public class PlacesTextView extends TextView {
	
	int[] colors = new int[] {Color.BLUE, Color.GREEN, Color.GRAY, Color.YELLOW, Color.CYAN, Color.DKGRAY, Color.LTGRAY, Color.MAGENTA, Color.RED};
	
	int[] fgColors = new int[] {Color.WHITE, Color.BLUE, Color.WHITE, Color.BLACK, Color.BLACK, Color.WHITE, Color.BLACK, Color.BLACK, Color.BLACK};

	private boolean isRandomized;

	private int mRandom;

	private Context mContext;

	public PlacesTextView(Context context) {
		this(context, null);
	}
	
	public PlacesTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if(!isRandomized) {
			setTextSize(20);
			setShadowLayer(1, 0.3f, 0.3f, Color.BLACK);
			setPadding(2, 2, 2, 2);
			startAnimation(AnimationUtils.loadAnimation(mContext, android.R.anim.fade_in));
			Rect rect = new Rect();
			getGlobalVisibleRect(rect);
			randomizeColor();
			Rect bound = rect;
			Paint paint = new Paint();
			paint.setColor(Color.BLACK);
			paint.setStrokeWidth(2);
			paint.setStyle(Style.FILL_AND_STROKE);
			canvas.drawRect(bound, paint);
		}
	}

	private void randomizeColor() {
		mRandom = (int) (Math.random() * (colors.length - 1));
		setTextColor(fgColors[mRandom]);		
		setBackgroundColor(colors[mRandom]);
		isRandomized = true;
	}

}
