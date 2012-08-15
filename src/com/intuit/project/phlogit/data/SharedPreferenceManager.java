package com.intuit.project.phlogit.data;

import android.content.Context;
import android.content.SharedPreferences;

import com.intuit.project.phlogit.constants.Constants;

public class SharedPreferenceManager {
	Context context = null;
	
	public SharedPreferenceManager(Context context) {
		this.context = context;
	}
	
	public void save(String key, String value) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(key, value).commit();
	}
	
	public String getString(String key, String defValue) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
		return prefs.getString(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
		return prefs.getBoolean(key, defValue);
	}

	public void save(String key, boolean value) {
		SharedPreferences prefs = context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putBoolean(key, value).commit();		
	}

	public SharedPreferences getSharedPreferences() {
		return context.getSharedPreferences(Constants.APP_NAME, Context.MODE_PRIVATE);
	}
}
