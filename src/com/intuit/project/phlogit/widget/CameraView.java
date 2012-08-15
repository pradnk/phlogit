/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package com.intuit.project.phlogit.widget;

import java.io.IOException;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.Size;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.intuit.project.phlogit.constants.Constants;

public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

	private SurfaceHolder holder;

	// private CameraManager cameraManager;

	private Camera camera;

	public final int MAX_8MP_WIDTH = 3264; // fixing it to 8MP as max

	public final int MAX_5MP_WIDTH = 2592; // 5MP size

	public final int MAX_HEIGHT = 1952;

	public CameraView(Context context, AttributeSet attrs) {
		super(context, attrs);

		holder = getHolder();
		holder.addCallback(this);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	/*
	 * private Camera.Size getMaxResolution(Camera.Parameters params) { List<Camera.Size> sizeList =
	 * params.getSupportedPictureSizes(); Camera.Size maxSize = null; if (sizeList.size() > 0) {
	 * maxSize = sizeList.get(0); for (Camera.Size size : sizeList) { if (maxSize.width < size.width
	 * &&
	 * maxSize.height < size.height) { // if (size.width <= MAX_WIDTH && size.height <= MAX_HEIGHT)
	 * maxSize = size; } } if (maxSize.width > MAX_WIDTH || maxSize.height > MAX_HEIGHT) {
	 * Camera.Size tempSize = null; tempSize = sizeList.get(0); for (Camera.Size size : sizeList) {
	 * if (size.width <= MAX_WIDTH && size.height <= MAX_HEIGHT) { if (size.width >= tempSize.width
	 * &&
	 * size.height >= tempSize.height) { maxSize = size; tempSize = size; } } } } } return maxSize;
	 * }
	 */

	private Camera.Size getMaxResolution(Camera.Parameters params, int paramWidth) {
		List<Camera.Size> sizeList = params.getSupportedPictureSizes();
		Camera.Size maxSize = null;
		int index = -1;
		if (paramWidth == -1)
			paramWidth = MAX_8MP_WIDTH;
		if (sizeList.size() > 0) {
			for (int i = 0; i < sizeList.size(); i++) {
				if (sizeList.get(i).width <= paramWidth) {
					if (index != -1) {
						if (sizeList.get(index).height < sizeList.get(i).height
								&& sizeList.get(index).width < sizeList.get(i).width)
							index = i;
					} else
						index = i;
				}
			}
		}
		if (index != -1)
			maxSize = sizeList.get(index);
		return maxSize;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		try {
			if (camera != null)
				camera.startPreview();
		} catch (Exception ex) {
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			/*
			 * camera = Camera.open(); camera.setPreviewDisplay(holder);
			 */

			camera = Camera.open();
			Parameters parameters = camera.getParameters();
			Log.i(Constants.APP_NAME, "Camera Parameters: " + parameters.toString());

			parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
			parameters.setAntibanding(Camera.Parameters.ANTIBANDING_AUTO);

			parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			parameters.setSceneMode(Camera.Parameters.SCENE_MODE_AUTO);
			parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
//			parameters.setColorEffect(Camera.Parameters.EFFECT_NEGATIVE);
			
			Log.i(Constants.APP_NAME, "Picture format: " + parameters.getPictureFormat());
			int paramWidth = -1;
			Size maxSize = getMaxResolution(parameters, paramWidth);

//			Integer quality = Configuration.getCompression();
//			if (quality != null) {
//				parameters.setJpegQuality(quality);
//			} else {
				setCameraCompressionParametersBasedOnSize(parameters, maxSize);
//			}

			if (maxSize != null) {
				parameters.setPictureSize(maxSize.width, maxSize.height);
				camera.setParameters(parameters);
			}

			camera.setPreviewDisplay(holder);
		} catch (IOException e) {
			Log.e(Constants.APP_NAME, "IOExcepion");
		} catch (Exception e) {
			Log.e(Constants.APP_NAME, "RunTimeException "+e);
		}
	}

	private void setCameraCompressionParametersBasedOnSize(Parameters parameters, Size maxSize) {
		if (maxSize.width == MAX_5MP_WIDTH)
			parameters.setJpegQuality(50);
		else if (maxSize.width == MAX_8MP_WIDTH)
			parameters.setJpegQuality(30);
		else
			// probably 3MP
			parameters.setJpegQuality(80);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		try {
			if (camera != null) {
				camera.stopPreview();
				camera.release();
				camera = null;
			}
		} catch (Exception ex) {
			// do nothing
		}
	}

	public Camera getCamera() {
		return camera;
	}

	public void setCamera(Camera camera) {
		this.camera = camera;
	}
}
