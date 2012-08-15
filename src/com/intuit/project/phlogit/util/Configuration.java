/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package com.intuit.project.phlogit.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import com.intuit.project.phlogit.Phlogit;

import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.AssetManager;
import android.content.res.Resources;

/**
 * Class representing global configuration settings.
 * Some notes on usage.
 * Whenever a new configuration is added on the server, introduce a static variable with a default
 * value. Assign the server returned value to the variable in the load() method
 */
public class Configuration {

	private static final String APP_ENV = "app_env";
	private static final Object ENV_DEV = "dev";
	private static final String UNDER_SCORE = "_";
	
	/********** Properties start ******************/
	
	public static String PROVIDERS = "providers";
	public static String FACEBOOK_AUTH_KEY = "facebook_auth_key";
	public static String FACEBOOK_AUTH_SECRET = "facebook_auth_secret";
	public static String TWITTER_AUTH_KEY = "twitter_auth_key";
	public static String TWITTER_AUTH_SECRET = "twitter_auth_secret";
	public static String TWITTER_CALLBACK_URL = "twitter_callback_url";
	
	/********** Properties end ********************/

	private static String appVersionName = null;

	private static int appVersionCode = -1;

	public static String getAppVersionName() {
		if (appVersionName == null) {
			loadAppVersionName();
		}
		return appVersionName;
	}

	private static void loadAppVersionName() {
		Context context = Phlogit.getPhlogitApplicationContext();
		String packageName = context.getPackageName();
		try {
			appVersionName = context.getPackageManager().getPackageInfo(packageName, 0).versionName;
		} catch (NameNotFoundException e) {
		}
	}

	public static int getAppVersionCode() {
		if (appVersionCode == -1) {
			loadAppVersionCode();
		}
		return appVersionCode;
	}

	private static void loadAppVersionCode() {
		Context context = Phlogit.getPhlogitApplicationContext();
		String packageName = context.getPackageName();
		try {
			appVersionCode = context.getPackageManager().getPackageInfo(packageName, 0).versionCode;
		} catch (NameNotFoundException e) {
		}

	}

	private static Properties props = null;

	public static void loadProperties(Context context) {
		Resources resources = context.getResources();
		AssetManager manager = resources.getAssets();
		Properties prop = new Properties();
		try {
			InputStream in = manager.open("config.properties");
			prop.load(in);
		} catch (IOException e) {
			e.printStackTrace();
		}
		props = prop;
	}

	/**
	 * Returns the key for the current applicable environment. The current applicable environment is
	 * based on
	 * the property app_env. Valid values are dev, qa, perf and prod.
	 * 
	 * @param key
	 * @return
	 */
	private static String getCurrentKey(String key) {
		String applicableEnv = props.getProperty(APP_ENV);
		if (null == applicableEnv) {
			// Environment variable is missing. Must be production.
			return key;
		} else if (ENV_DEV.equals(applicableEnv)) {
			key = key + UNDER_SCORE + ENV_DEV;
		} else {
			// production configuration
		}
		return key;
	}

	public static final boolean isProductionConfiguration() {
		boolean isProd = false;
		String applicableEnv = props.getProperty(APP_ENV);
		if (null == applicableEnv) {
			isProd = true;
		} else if (ENV_DEV.equals(applicableEnv)) {
			isProd = false;
		} else {
			// production configuration
			isProd = true;
		}
		return isProd;

	}

	private static String getProperty(String key) {
		String currentKey = getCurrentKey(key);
		String value = props.getProperty(currentKey);
		return value;
	}
	
	public static String getProviders() {
		return getProperty(PROVIDERS);
	}

	public static String getFacebookAuthKey() {
		return getProperty(FACEBOOK_AUTH_KEY);
	}
	
	public static String getFacebookAuthSecret() {
		return getProperty(FACEBOOK_AUTH_SECRET);
	}
	
	public static String getTwitterAuthKey() {
		return getProperty(TWITTER_AUTH_KEY);
	}
	
	public static String getTwitterAuthSecret() {
		return getProperty(TWITTER_AUTH_SECRET);
	}
	
	public static String getTwitterCallbackUrl() {
		return getProperty(TWITTER_CALLBACK_URL);
	}
}