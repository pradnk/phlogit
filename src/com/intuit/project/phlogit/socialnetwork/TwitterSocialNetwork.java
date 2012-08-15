package com.intuit.project.phlogit.socialnetwork;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.URI;

import winterwell.jtwitter.OAuthSignpostClient;
import winterwell.jtwitter.Twitter;
import winterwell.jtwitter.Twitter.Status;
import winterwell.jtwitter.Twitter.User;
import winterwell.jtwitter.TwitterAccount;
import winterwell.jtwitter.TwitterException;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.intuit.project.phlogit.Phlogit;
import com.intuit.project.phlogit.constants.Constants;
import com.intuit.project.phlogit.data.SharedPreferenceManager;
import com.intuit.project.phlogit.data.vo.Profile;
import com.intuit.project.phlogit.util.NetworkUtil;

public class TwitterSocialNetwork extends SocialNetwork {

	private static final String NAME = "Twitter";
	
	private Context context;

	public static final String KEY = "BRtAmvSyCkmUmHQQBEhzeA";

	public static final String SECRET = "z91vwbCjeAJEhzgqzr7J15xcGh889fnNDJnzHOWL40";

	protected static final String CALLBACK_URL = "http://appsforandroid.blogspot.com";

	private static OAuthSignpostClient client;

	public TwitterSocialNetwork(Context context) {
		this.context = context;
	}
	
	@Override
	public void authenticate() {
		client = new  OAuthSignpostClient(KEY, SECRET, CALLBACK_URL);
		String url = null;
		Intent i = null;
		try {
			url = client.authorizeUrl().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(url != null) {
			i = new Intent(context, WebViewClientActivity.class).putExtra("authUrl", url).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			i = new Intent(context, ErrorActivity.class).putExtra("error", "Please check your internet settings. Failure to communicate.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
        context.startActivity(i);
	}
	
	@Override
	public void authenticate(boolean fetchProfile) {
		client = new  OAuthSignpostClient(KEY, SECRET, CALLBACK_URL);
		String url = null;
		Intent i = null;
		try {
			url = client.authorizeUrl().toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if(url != null) {
			i = new Intent(context, WebViewClientActivity.class).putExtra("authUrl", client.authorizeUrl().toString()).putExtra("fetchProfile", fetchProfile).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		} else {
			i = new Intent(context, ErrorActivity.class).putExtra("error", "Please check your internet settings. Failure to communicate.").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
        context.startActivity(i);
	}
	
	@Override
	public void postStatus(String status) {
		Status success = null;
		String[] accessTokenAndSecret = getToken();
		OAuthSignpostClient client = new OAuthSignpostClient(KEY, SECRET, accessTokenAndSecret[0], accessTokenAndSecret[1]);
		Twitter t = new Twitter(null, client);
		try {
			success = t.setStatus(status);
		} catch (TwitterException e) {
			e.printStackTrace();
		}
		if(success != null)
			Toast.makeText(context, "Successfully posted a status message to "+NAME, Toast.LENGTH_LONG).show();
		else
			Toast.makeText(context, "Failure to postStatus to "+NAME+". Try again later", Toast.LENGTH_LONG).show();
	}

	@Override
	public Profile fetchProfile() {
		String[] tokenSecret = getToken();
		if(tokenSecret != null) {
			OAuthSignpostClient client = new OAuthSignpostClient(KEY, SECRET, tokenSecret[0], tokenSecret[1]);
			Twitter t = new Twitter(null, client);
			//We can't directly access t.getUser(screenName) as we do not know the screenName of the user
			//as we set it to null while creating Twitter object.
			User user = new TwitterAccount(t).verifyCredentials();
			Profile profile = new Profile();
			profile.name = user.name;
			profile.id = user.id;
			URI s = user.profileImageUrl;
			byte[] image = NetworkUtil.getRawData(s.toString());
			if(image != null) {
				profile.image = image;
			}
			profile.bio = user.description;
			profile.bio += "\n--Profile Imported from Twitter--\n";
			SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(Phlogit.getPhlogitApplicationContext());
			sharedPreferenceManager.save("ProfileName", profile.name);
			saveProfile(profile);
			Toast.makeText(context, "Welcome "+profile.name, Toast.LENGTH_LONG).show();
		}
		return null;
	}

	@Override
	public void addFriend(String id) {
		//TODO:
	}
	
	private void saveProfile(Profile profile) {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(context.openFileOutput(Constants.PROFILE, Context.MODE_PRIVATE));
			oos.writeObject(profile);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(oos != null)
				try {
					oos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(Phlogit.getPhlogitApplicationContext());
		sharedPreferenceManager.save("isProfile", true);
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getId() {
		return SocialNetwork.SOCIAL_NETWORK_TWITTER;
	}

	@Override
	public boolean isAuthenticated() {
		if(getToken() != null)
			return true;
		return false;
	}

	@Override
	public void setTokens(Object... params) {
		MyTwitterAccessToken token = new MyTwitterAccessToken();
		client.setAuthorizationCode((String)params[0]);
		String[] accessTokenAndSecret = client.getAccessToken();
		token.accessTokenAndSecret = accessTokenAndSecret;
		try {
			ObjectOutputStream oos = new ObjectOutputStream(context.openFileOutput(NAME, Context.MODE_PRIVATE));
			oos.writeObject(token);
			oos.flush();
			oos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	private String[] getToken() {
		ObjectInputStream ois;
		try {
			ois = new ObjectInputStream(context.openFileInput(NAME));
			MyTwitterAccessToken token = (MyTwitterAccessToken)ois.readObject();
			ois.close();
			if(token != null) {
				System.out.println("TWITTER Token == "+token.accessTokenAndSecret);
				return token.accessTokenAndSecret;
			}
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}

class MyTwitterAccessToken implements Serializable {
	/**
	 * Generated SerialVersionID
	 */
	private static final long serialVersionUID = 6117313476826479814L;
	String[] accessTokenAndSecret;
}
