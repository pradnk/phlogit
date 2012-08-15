package com.intuit.project.phlogit;

import com.intuit.project.phlogit.data.vo.Profile;
import com.intuit.project.phlogit.socialnetwork.FacebookSocialNetwork;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.Toast;

public class SettingsActivity extends PreferenceActivity {

	private Preference profile;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle("phLogIt! - Settings");
		addPreferencesFromResource(R.xml.settings);

		final SharedPreferences sharedPrefs = getSharedPreferences("Settings",
				MODE_PRIVATE);
		
		final CheckBoxPreference sync = (CheckBoxPreference) findPreference("autoSync");
		sync.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean value = (Boolean) newValue;
				sharedPrefs.edit().putBoolean("autoSync", value).commit();
				if (value) {
					sync.setSummary("Auto Sync ON");
				} else {
					sync.setSummary("Auto Sync OFF");
				}
				return true;
			}
		});

		final CheckBoxPreference network = (CheckBoxPreference) findPreference("network");
		network.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean value = (Boolean) newValue;
				sharedPrefs.edit().putBoolean("network", value).commit();
				if (value) {
					network.setSummary("Any Network");
				} else {
					network.setSummary("WiFi Only");
				}
				return true;
			}
		});
		
		final CheckBoxPreference steadyShot = (CheckBoxPreference) findPreference("steadyShot");
		steadyShot.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean value = (Boolean) newValue;
				sharedPrefs.edit().putBoolean("steadyShot", value).commit();
				if (value) {
					network.setSummary("Steady Shot ON");
				} else {
					network.setSummary("Steady Shot OFF");
				}
				return true;
			}
		});
		
		final CheckBoxPreference notifications = (CheckBoxPreference) findPreference("notifications");
		notifications.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {

			@Override
			public boolean onPreferenceChange(Preference preference,
					Object newValue) {
				boolean value = (Boolean) newValue;
				sharedPrefs.edit().putBoolean("notifications", value).commit();
				if (value) {
					network.setSummary("Notifications OFF");
				} else {
					network.setSummary("Notifications ON");
				}
				return true;
			}
		});
		
		profile = (Preference) findPreference("profile");
		final FacebookSocialNetwork facebook = new FacebookSocialNetwork(getApplicationContext());
		Profile profileVO = facebook.getProfile();
		if(profileVO != null && !TextUtils.isEmpty(profileVO.name)) {
			profile.setTitle(profileVO.name);
			profile.setSummary("Clear Profile");
		} else {
			profile.setTitle("Add Profile");
			profile.setSummary("");
		}
		profile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				if(TextUtils.isEmpty(profile.getSummary())) {
					facebook.fetchProfile();
				} else {
					new AlertDialog.Builder(SettingsActivity.this)
					.setMessage("Do you want to clear your profile?")
					.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if(facebook.clearProfile()) {
								profile.setTitle("Add Profile");
								profile.setSummary("");
							}								
						}
					})
					.setNegativeButton("No", null)
					.show();
				}
				return true;
			}
		});
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		
		final FacebookSocialNetwork facebook = new FacebookSocialNetwork(getApplicationContext());
		Profile profileVO = facebook.getProfile();
		if(profileVO != null && !TextUtils.isEmpty(profileVO.name)) {
			profile.setTitle(profileVO.name);
			profile.setSummary("Clear Profile");
		} else {
			profile.setTitle("Add Profile");
		}
	}
}