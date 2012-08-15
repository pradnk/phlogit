package com.intuit.project.phlogit.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.Configuration;
import android.text.TextUtils;
import android.util.Log;

import com.intuit.project.phlogit.Phlogit;
import com.intuit.project.phlogit.R;
import com.intuit.project.phlogit.constants.Constants;

public class Utility {
	
	/**
	 * Get app signature to use in Facebook etc.
	 * @param context
	 */
	public static void getAppSignature(Activity context) {
		try {
			PackageInfo info = context.getPackageManager()
					.getPackageInfo("com.intuit.project.phlogit",
							PackageManager.GET_SIGNATURES);
			for (Signature signature : info.signatures) {
				MessageDigest md = MessageDigest.getInstance("SHA");
				md.update(signature.toByteArray());
				Log.i(Constants.APP_NAME, "" + Base64.encodeBytes(md.digest()));
			}
		} catch (NameNotFoundException e) {
		} catch (NoSuchAlgorithmException e) {
		}
	}
	
	public static int getDialogWidth(Context context, int displayWidth, int displayHeight) {
		Configuration configuration = context.getResources().getConfiguration();
		int sizefactor = 100;
		int width = displayWidth;

		if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
			sizefactor = context.getResources().getInteger(R.integer.sizefactor);
			width = ((displayWidth) * sizefactor) / 100;
		} else if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
			sizefactor = context.getResources().getInteger(R.integer.landscapesizefactor);
			width = ((displayHeight) * sizefactor) / 100;
		} else if (configuration.orientation == Configuration.ORIENTATION_SQUARE) {
			sizefactor = context.getResources().getInteger(R.integer.squaresizefactor);
			width = ((displayWidth) * sizefactor) / 100;
		}

		return width;
	}
	
	/**
	 * Get a SharedPreferences file and check for a value.
	 * It will be empty for first time 
	 * After that we will add a value to ensure its not first run.
	 * 
	 * @return boolean
	 */
	public static boolean isFirstRun() {
		SharedPreferences firstRun = Phlogit.getPhlogitApplicationContext().getSharedPreferences("FirstRun", Context.MODE_PRIVATE);
		String firstRunString = firstRun.getString("FirstRun", "");
		if(TextUtils.isEmpty(firstRunString)) {
			firstRun.edit().putString("FirstRun", "Yes").commit();
			return true;
		}
		return false;
	}
}
