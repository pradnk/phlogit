/*
 * Copyright 2010 Emmanuel Astier & Kevin Gaudin
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.acra;

import static org.acra.ACRA.LOG_TAG;

import java.lang.Thread.UncaughtExceptionHandler;

import android.content.Context;
import android.os.Looper;
import android.util.Log;

/**
 * <p>
 * The ErrorReporter is a Singleton object in charge of collecting crash context data and sending
 * crash reports. It registers itself as the Application's Thread default
 * {@link UncaughtExceptionHandler}.
 * </p>
 * <p>
 * When a crash occurs, it collects data of the crash context (device, system, stack trace...) and
 * writes a report file in the application private directory. This report file is then sent :
 * <ul>
 * <li>immediately if {@link #mReportingInteractionMode} is set to
 * {@link ReportingInteractionMode#SILENT} or {@link ReportingInteractionMode#TOAST},</li>
 * <li>on application start if in the previous case the transmission could not technically be made,</li>
 * <li>when the user accepts to send it if {@link #mReportingInteractionMode} is set to
 * {@link ReportingInteractionMode#NOTIFICATION}.</li>
 * </ul>
 * </p>
 * <p>
 * If an error occurs while sending a report, it is kept for later attempts.
 * </p>
 */
public class CustomErrorReporter implements Thread.UncaughtExceptionHandler {

	// A reference to the system's previous default UncaughtExceptionHandler
	// kept in order to execute the default exception handling after sending
	// the report.
	private Thread.UncaughtExceptionHandler mDfltExceptionHandler;

	// The application context
	private static Context mContext;

	// Our singleton instance.
	private static CustomErrorReporter mInstanceSingleton;

	/**
	 * Create or return the singleton instance.
	 * 
	 * @return the current instance of ErrorReporter.
	 */
	public static CustomErrorReporter getInstance() {
		if (mInstanceSingleton == null) {
			mInstanceSingleton = new CustomErrorReporter();
		}
		return mInstanceSingleton;
	}

	/**
	 * <p>
	 * This is where the ErrorReporter replaces the default {@link UncaughtExceptionHandler}.
	 * </p>
	 * 
	 * @param context
	 *            The android application context.
	 */
	public void init(Context context) {
		// If mDfltExceptionHandler is not null, initialization is already done.
		// Don't do it twice to avoid losing the original handler.
		if (mDfltExceptionHandler == null) {
			mDfltExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
			Thread.setDefaultUncaughtExceptionHandler(this);
			mContext = context;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang .Thread,
	 * java.lang.Throwable)
	 */
	public void uncaughtException(Thread t, Throwable e) {
		Log.e(ACRA.LOG_TAG,
				"ACRA caught a " + e.getClass().getSimpleName() + " exception for "
						+ mContext.getPackageName());

		new Thread() {

			/*
			 * (non-Javadoc)
			 * @see java.lang.Thread#run()
			 */
			@Override
			public void run() {
				Looper.prepare();
				new BummerToast(mContext).show();
				// Toast.makeText(mContext, ACRA.getConfig().resToastText(), Toast.LENGTH_LONG)
				// .show();
				Looper.loop();
			}

		}.start();

		try {
			// Wait a bit to let the user read the toast
			Thread.sleep(6000);
		} catch (InterruptedException e1) {
			Log.e(LOG_TAG, "Error : ", e1);
		}

		// Initiate an orderly shutdown of the database tasks so that the database is not
		// left in an inconsistent state when a crash happens.
		mDfltExceptionHandler.uncaughtException(t, e);
	}
}