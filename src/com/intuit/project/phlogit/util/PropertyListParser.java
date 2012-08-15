/*
 * Copyright 2010 Intuit, Inc. All rights reserved.
 */
package com.intuit.project.phlogit.util;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import org.xmlpull.v1.XmlPullParser;

import android.util.Xml;

/**
 * This class will parse a plist xml file and store the contents in a
 * hierarchical HashMap of <String, Object> tuples, where the Object value
 * could be another HashMap, an ArrayList, String or Integer value.
 * Use the getConfiguration methods to retrieve the values from the parsed plist file.
 * The key names for nested dictionary references must be of the form :
 * Dict1KeyName.Dict2KeyName.ElementKeyName
 */
public class PropertyListParser {

	private static final String TAG = "PropertyListParser";

	/**
	 * The nested (hierarchical) HashMap which holds our key-value pairs of our plist file.
	 */
	protected HashMap<String, Object> mPlistHashMap;

	/**
	 * Constructor.
	 * Parse a plist file from the given InputStream.
	 * 
	 * @param inputStream The InputStream which has the bytes of the plist file we need to parse.
	 */
	public PropertyListParser(InputStream inputStream) {
		mPlistHashMap = new HashMap<String, Object>();
		parse(inputStream);
	}

	/**
	 * Get an String configuration value for the given key.
	 * 
	 * @param keyName The name of the key to look up in the configuration dictionary.
	 * @return The String value of the specified key.
	 */
	public String getConfiguration(String keyName) {
		return (String) getConfigurationObject(keyName);
	}

	/**
	 * Get a String configuration value for the given key.
	 * If there is no value for the given key, then return the default value.
	 * 
	 * @param keyName The name of the key to look up in the configuration dictionary.
	 * @param defaultValue The default value to return if they key has no associated value.
	 * @return The String value of the specified key, or defaultValue if the value for keyName is
	 *         null.
	 */
	public String getConfigurationWithDefault(String keyName, String defaultValue) {
		String value = getConfiguration(keyName);
		if (value == null) {
			return defaultValue;
		}

		return value;
	}

	/**
	 * Get a Hashmap for keys also having a set of key/value pairs instead of just a String value.
	 * 
	 * @param keyName The name of the key to look up in the configuration dictionary.
	 * @return Hashmap of the key/value pairs value of this specific keyName.
	 */
	public HashMap<?, ?> getConfigurationForHashMap(String keyName) {
		Object value = getConfigurationObject(keyName);
		if (value instanceof HashMap<?, ?>)
			return (HashMap<?, ?>) value;
		return null;
	}

	/**
	 * Get an Integer configuration value for the given key.
	 * 
	 * @param keyName The name of the key to look up in the configuration dictionary.
	 * @return The Integer value of the specified key.
	 */
	public Integer getConfigurationInteger(String keyName) {
		return (Integer) getConfigurationObject(keyName);
	}

	/**
	 * Get an Integer configuration value for the given key.
	 * If there is no value for the given key, then return the default value.
	 * 
	 * @param keyName The name of the key to look up in the configuration dictionary.
	 * @param defaultValue The default value to return if they key has no associated value.
	 * @return The Integer value of the specified key, or defaultValue if the value for keyName is
	 *         null.
	 */
	public Integer getConfigurationIntegerWithDefault(String keyName, Integer defaultValue) {
		Integer value = getConfigurationInteger(keyName);
		if (value == null) {
			return defaultValue;
		}

		return value;
	}

	/**
	 * Utility method which uses a XmlPullParser to iterate through the XML elements
	 * and build up a hierarchical HashMap representing the key-value pairs of the
	 * plist configuration file.
	 * 
	 * @param inputStream The InputStream which contains the plist XML file.
	 */
	public void parse(InputStream inputStream) {
		XmlPullParser parser = Xml.newPullParser();

		try {
			parser.setInput(inputStream, null);

			int eventType = parser.getEventType();

			boolean done = false;
			boolean parsingArray = false;

			String name = null;
			String key = null;

			Stack<HashMap<String, Object>> stack = new Stack<HashMap<String, Object>>();
			HashMap<String, Object> dict = null;
			ArrayList<Object> array = null;

			while (eventType != XmlPullParser.END_DOCUMENT && !done) {
				switch (eventType) {
				case XmlPullParser.START_DOCUMENT:
					// Log.d(TAG, "START_DOCUMENT");
					break;
				case XmlPullParser.START_TAG:
					name = parser.getName();

					if (name.equalsIgnoreCase("dict")) {
						// root dict element
						if (key == null) {
							mPlistHashMap.clear();
							dict = mPlistHashMap;
						} else if (parsingArray) {
							// Log.d(TAG, "START_TAG dict : inside array");
							HashMap<String, Object> childDict = new HashMap<String, Object>();
							array.add(childDict);
							stack.push(dict);
							dict = childDict;
						} else {
							// Log.d(TAG, "START_TAG dict : " + key);
							HashMap<String, Object> childDict = new HashMap<String, Object>();
							dict.put(key, childDict);
							stack.push(dict);
							dict = childDict;
						}
					} else if (name.equalsIgnoreCase("key")) {
						key = parser.nextText();
					} else if (name.equalsIgnoreCase("integer")) {
						dict.put(key, new Integer(parser.nextText()));
					} else if (name.equalsIgnoreCase("string")) {
						if (parsingArray) {
							array.add(parser.nextText());
						} else {
							dict.put(key, parser.nextText());
						}
					} else if (name.equalsIgnoreCase("array")) {
						parsingArray = true;
						array = new ArrayList<Object>();
						dict.put(key, array);
					}

					break;
				case XmlPullParser.END_TAG:
					name = parser.getName();

					if (name.equalsIgnoreCase("dict")) {
						// Log.d(TAG, "END_TAG dict");
						if (!stack.empty()) {
							dict = stack.pop();
						}
					} else if (name.equalsIgnoreCase("array")) {
						parsingArray = false;
					} else if (name.equalsIgnoreCase("plist")) {
						done = true;
					}

					break;
				case XmlPullParser.END_DOCUMENT:
					// Log.d(TAG, "END_DOCUMENT");
					break;

				}
				eventType = parser.next();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Utility method which tokenizes the given keyName using
	 * the "." delimiter and then looks up each token in the
	 * configuration dictionary. If the token key points to a
	 * dictionary then it proceeds to the next token key and
	 * looks up value of the token key in the dictionary it found
	 * from the previous token key.
	 * 
	 * @param keyName The fully qualified key name.
	 * @return The Object value associated with the given key, or null if the key does not exist.
	 */
	@SuppressWarnings("unchecked")
	private Object getConfigurationObject(String keyName) {
		String[] tokens = keyName.split("\\.");

		if (tokens.length > 1) {
			HashMap<String, Object> dict = mPlistHashMap;
			Object obj;
			for (int i = 0; i < tokens.length; i++) {
				obj = dict.get(tokens[i]);
				if (obj instanceof HashMap<?, ?>) {
					dict = (HashMap<String, Object>) obj;
					continue;
				}
				return obj;
			}
		}

		return mPlistHashMap.get(keyName);
	}
}
