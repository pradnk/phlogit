package com.intuit.project.phlogit.socialnetwork;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OptionalDataException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.net.MalformedURLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.facebook.android.AsyncFacebookRunner;
import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.intuit.project.SyncCompleteListener;
import com.intuit.project.phlogit.Phlogit;
import com.intuit.project.phlogit.constants.Constants;
import com.intuit.project.phlogit.data.SharedPreferenceManager;
import com.intuit.project.phlogit.data.vo.FacebookCommentsLike;
import com.intuit.project.phlogit.data.vo.Profile;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.util.Configuration;
import com.intuit.project.phlogit.util.NetworkUtil;

public class FacebookSocialNetwork extends SocialNetwork {

	private static final String NAME = "Facebook";
	private Context context;
	String facebookId = "";
	private String photoPath;
	private SyncCompleteListener syncCompleteListener;

	public FacebookSocialNetwork(Context context) {
		this.context = context;
	}
	
	@Override
	public void authenticate() {
		context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
	}

	@Override
	public void authenticate(boolean fetchProfile) {
		context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
	}

	@Override
	public void postStatus(String status) {
		Facebook facebook = new Facebook(Configuration.getFacebookAuthKey());
		if(getToken() != null) {
			Bundle params = new Bundle();
			params.putString("status", status);
			facebook.dialog(context, "stream.publish", params,
					new WallPostDialogListener());
		}
		
	}
	
	public void postStatus(String status, Activity act) {
		Facebook facebook = new Facebook(Configuration.getFacebookAuthKey());
		if(getToken() != null) {
			Bundle params = new Bundle();
			params.putString("status", status);
			facebook.dialog(act, "stream.publish", params,
					new WallPostDialogListener());
		}
		
	}
	
	public final class WallPostDialogListener implements DialogListener {

		public void onComplete(Bundle values) {
			final String postId = values.getString("post_id");
			if (postId != null) {
				Toast.makeText(context, "Successfully posted to Facebook", Toast.LENGTH_LONG).show();
			} else {
				Toast.makeText(context, "Failure posting to Facebook", Toast.LENGTH_LONG).show();
			}
		}

		public void onCancel() {
		}

		@Override
		public void onFacebookError(FacebookError e) {
			
		}

		@Override
		public void onError(DialogError e) {
			
		}

	}

	@Override
	public Profile fetchProfile() {
		if(!NetworkUtil.isConnected()) {
			return null;
		}
		try {
			if(isAuthenticated()) {
				Facebook facebook = new Facebook(Configuration.getFacebookAuthKey());
				String accessToken = getToken();
				long expires = getExpires();
				if(accessToken != null)
					facebook.setAccessToken(accessToken);
				if(expires != 0)
					facebook.setAccessExpires(expires);
				if(facebook.isSessionValid()) {
					String temp = facebook.request("me");
					Profile profile = new Profile();
					JSONObject me = new JSONObject(temp);
					System.out.println(me);
					if(me.has("name")) {
						profile.name = me.getString("name");
					}
					if(me.has("email")) {
						profile.email = me.getString("email");
					}
					if(me.has("bio")) {
						profile.bio= me.getString("bio");
					}
					SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(Phlogit.getPhlogitApplicationContext());
					sharedPreferenceManager.save("ProfileName", profile.name);
					saveProfile(profile);
				} else {
					context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
				}
			} else {
				context.startActivity(new Intent(context, WebViewClientActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK).putExtra("isfacebook", true));
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public void uploadPhoto(final String photoPath, String albumName, final SyncCompleteListener listener) {
		if(!NetworkUtil.isConnected()) {
			return;
		}
		if(listener != null && syncCompleteListener == null) {
			this.syncCompleteListener = listener;
		}
		try {
			this.photoPath = photoPath;
			if (isAuthenticated()) {
				Facebook facebook = new Facebook(
						Configuration.getFacebookAuthKey());
				String accessToken = getToken();
				long expires = getExpires();
				if (accessToken != null)
					facebook.setAccessToken(accessToken);
				if (expires != 0)
					facebook.setAccessExpires(expires);
				if (facebook.isSessionValid()) {
					byte[] data = null;
					if (TextUtils.isEmpty(photoPath)) {
						Bitmap bi = BitmapFactory
								.decodeFile("/mnt/sdcard/test.jpg");
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						bi.compress(Bitmap.CompressFormat.JPEG, 100, baos);
						data = baos.toByteArray();
					} else {
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] one = new byte[1];
						BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(photoPath)));
						while(bis.read(one) != -1) {
							baos.write(one);
						}
						bis.close();
						baos.close();
						data = baos.toByteArray();
					}

					Bundle params = new Bundle();
					
					//Use already created album if present else create new one.
					String response = facebook.request("me/albums");
					System.out.println("RESP: "+response);
					JSONObject responseJSON = new JSONObject(response);
					String albumId = "";
					try {
						JSONArray array = responseJSON.getJSONArray("data");
						for(int i=0;i<array.length();i++) {
							if(array.getJSONObject(i).getString("name").equals(albumName)) {
								albumId = array.getJSONObject(i).getString("id");
								break;
							}
						}
					} catch (Exception e) {
						//If album not existing create one.
						params.putString("name", albumName);
						response = facebook.request("me/albums",params,"POST");
						Log.i(Constants.APP_NAME, "JSON ALBUM RESP: "+response);
						responseJSON = new JSONObject(response);
						albumId=responseJSON.getString("id");
					} finally {
						if(TextUtils.isEmpty(albumId)) {
							//If album not existing create one.
							params.putString("name", albumName);
							response = facebook.request("me/albums",params,"POST");
							Log.i(Constants.APP_NAME, "JSON ALBUM RESP: "+response);
							responseJSON = new JSONObject(response);
							albumId=responseJSON.getString("id");
						}
					}
					params = new Bundle();
//					params.putString("message", "An interesting Movie");
					params.putByteArray("picture", data);

					AsyncFacebookRunner mAsyncRunner = new AsyncFacebookRunner(
							facebook);
					mAsyncRunner.request("/"+albumId+"/photos", params, "POST", new AsyncFacebookRunner.RequestListener() {
						
						@Override
						public void onMalformedURLException(MalformedURLException e, Object state) {
							facebookId = null;
						}
						
						@Override
						public void onIOException(IOException e, Object state) {
							facebookId = null;
						}
						
						@Override
						public void onFileNotFoundException(FileNotFoundException e, Object state) {
							facebookId = null;
						}
						
						@Override
						public void onFacebookError(FacebookError e, Object state) {
							Log.i("Pradeep", "FBError: "+e);
							facebookId = null;
						}
						
						@Override
						public void onComplete(String response, Object state) {
							Log.e(Constants.APP_NAME, "Upload to Facebook Done! "+response);
							facebookId = response;
							String id = null;
							try {
								JSONObject json = new JSONObject(facebookId);
								id = json.getString("id");
							} catch(Exception e) {
								e.printStackTrace();
							}
							System.out.println("ID: ********** "+id);
							if(id != null) {
								String galleryValueFromDb = "";
								Cursor c = context.getContentResolver().query(Photos.CONTENT_URI, null, "photo_gallery_id = ?", new String[]{photoPath}, null);
								if(c != null) {
									c.moveToFirst();
									
									if(!c.isAfterLast()) {
										galleryValueFromDb = c.getString(c.getColumnIndex(Photos.PHOTO_GALLERY_ID));
									}
								}
								c.close();
								
								if(galleryValueFromDb.equals(photoPath)) {
									ContentValues inValues = new ContentValues();
							    	inValues.put(Photos.PHOTO_FACEBOOK_ID, id);
							    	inValues.put(Photos.PHOTO_SYNCED, "true");
								    context.getContentResolver().update(Photos.CONTENT_URI, inValues, "photo_gallery_id = ?", new String[]{photoPath});
								} else {
									ContentValues inValues = new ContentValues();
							    	inValues.put(Photos.PHOTO_FACEBOOK_ID, id);
							    	inValues.put(Photos.PHOTO_SYNCED, "true");
								    context.getContentResolver().insert(Photos.CONTENT_URI, inValues);
								}
							}
							if(syncCompleteListener != null) {
								syncCompleteListener.onSyncComplete();
							}
						}
					}, new WallPostDialogListener());
				} else {
					context.startActivity(new Intent(context,
							WebViewClientActivity.class).addFlags(
							Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
							"isfacebook", true).putExtra("Photo", photoPath).putExtra("Album", albumName));
				}
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("Photo", photoPath).putExtra("Album", albumName));
			}
		} catch (Exception e) {
			e.printStackTrace();
			facebookId = null;
		}
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
	
	public Profile getProfile() {
		ObjectInputStream ois = null;
		Profile profile = null;
		try {
			ois = new ObjectInputStream(context.openFileInput(Constants.PROFILE));
			profile = (Profile)ois.readObject();
			ois.close();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return profile;
	}

	@Override
	public void addFriend(String id) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public int getId() {
		return SocialNetwork.SOCIAL_NETWORK_FACEBOOK;
	}

	@Override
	public boolean isAuthenticated() {
		if(getToken() != null)
			return true;
		return false;
	}

	@Override
	public void setTokens(Object... params) {
		FacebookToken token = new FacebookToken();
		token.accessToken = (String) params[0];
		token.id = (String) params[1];
		token.expiresIn = (Long)params[2];
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
	
	public String getToken() {
		FacebookToken token = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(context.openFileInput(NAME));
			token = (FacebookToken)ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(token != null) {
			return token.accessToken;
		}
		return null;
	}
	
	public long getExpires() {
		FacebookToken token = null;
		try {
			ObjectInputStream ois = new ObjectInputStream(context.openFileInput(NAME));
			token = (FacebookToken)ois.readObject();
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (OptionalDataException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		if(token != null) {
			return token.expiresIn;
		}
		return 0;
	}

	public FacebookCommentsLike getComments(String facebookId) {
		FacebookCommentsLike commentsLike = new FacebookCommentsLike();
		if (isAuthenticated()) {
			Facebook facebook = new Facebook(
					Configuration.getFacebookAuthKey());
			String accessToken = getToken();
			long expires = getExpires();
			if (accessToken != null)
				facebook.setAccessToken(accessToken);
			if (expires != 0)
				facebook.setAccessExpires(expires);
			if (facebook.isSessionValid()) {
				try {
					String commentsResponse = facebook.request("/"+facebookId+"/comments");
					JSONObject json = new JSONObject(commentsResponse);
					JSONArray data = json.getJSONArray("data");
					for(int i=0;i<data.length();i++) {
						JSONObject obj = (JSONObject)data.get(i);
						JSONObject from = (JSONObject) obj.get("from");
						commentsLike.comments.add(obj.getString("message"));
						commentsLike.from.add(from.getString("name")); 
					}
					String likesResponse = facebook.request("/"+facebookId+"/likes");
					json = new JSONObject(likesResponse);
					data = json.getJSONArray("data");
					StringBuffer likes = new StringBuffer();
					for(int i=0;i<data.length();i++) {
						JSONObject obj = (JSONObject)data.get(i);
						likes.append(obj.getString("name")).append(", ");
					}
					commentsLike.likedBy = likes.toString();
					commentsLike.likeNumber = data.length();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("GetComments", "true").putExtra("PhotoId", facebookId));
			}
		} else {
			context.startActivity(new Intent(context,
					WebViewClientActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
							"isfacebook", true).putExtra("GetComments", "true").putExtra("PhotoId", facebookId));
		}
		return commentsLike;
	}
	
	public void addComment(String facebookId, String comments) {
		if (isAuthenticated()) {
			Facebook facebook = new Facebook(
					Configuration.getFacebookAuthKey());
			String accessToken = getToken();
			long expires = getExpires();
			if (accessToken != null)
				facebook.setAccessToken(accessToken);
			if (expires != 0)
				facebook.setAccessExpires(expires);
			if (facebook.isSessionValid()) {
				try {
					Bundle params = new Bundle();
					params.putString("message", comments);
					String response = facebook.request("/"+facebookId+"/comments", params, "POST");
					System.out.println("?????????????? Comments: "+response);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("AddComments", "true").putExtra("Comments", comments).putExtra("PhotoId", facebookId));
			}
		} else {
			context.startActivity(new Intent(context,
					WebViewClientActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
							"isfacebook", true).putExtra("AddComments", "true").putExtra("Comments", comments).putExtra("PhotoId", facebookId));
		}
	}
	
	public void like(String facebookId) {
		if (isAuthenticated()) {
			Facebook facebook = new Facebook(
					Configuration.getFacebookAuthKey());
			String accessToken = getToken();
			long expires = getExpires();
			if (accessToken != null)
				facebook.setAccessToken(accessToken);
			if (expires != 0)
				facebook.setAccessExpires(expires);
			if (facebook.isSessionValid()) {
				try {
					String response = facebook.request("/"+facebookId+"/likes", new Bundle(), "POST");
					System.out.println(")()(())() Comments: "+response);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("Likes", "true").putExtra("PhotoId", facebookId));
			}
		} else {
			context.startActivity(new Intent(context,
					WebViewClientActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("Likes", "true").putExtra("PhotoId", facebookId));
		}
	}

	public void unLike(String facebookId) {
		if (isAuthenticated()) {
			Facebook facebook = new Facebook(
					Configuration.getFacebookAuthKey());
			String accessToken = getToken();
			long expires = getExpires();
			if (accessToken != null)
				facebook.setAccessToken(accessToken);
			if (expires != 0)
				facebook.setAccessExpires(expires);
			if (facebook.isSessionValid()) {
				try {
					String response = facebook.request("/"+facebookId+"/likes", new Bundle(), "DELETE");
					System.out.println(")()(())() Comments: "+response);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				context.startActivity(new Intent(context,
						WebViewClientActivity.class).addFlags(
						Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("Unlikes", "true").putExtra("PhotoId", facebookId));
			}
		} else {
			context.startActivity(new Intent(context,
					WebViewClientActivity.class).addFlags(
					Intent.FLAG_ACTIVITY_NEW_TASK).putExtra(
						"isfacebook", true).putExtra("Unlikes", "true").putExtra("PhotoId", facebookId));
		}		
	}

	public boolean clearProfile() {
		if(context.deleteFile(Constants.PROFILE)) {
			SharedPreferenceManager sharedPreferenceManager = new SharedPreferenceManager(Phlogit.getPhlogitApplicationContext());
			sharedPreferenceManager.save("isProfile", false);
			FacebookToken token = new FacebookToken();
			token.accessToken = null;
			token.id = null;
			token.expiresIn = 0;
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
			return true;
		}
		return false;
	}
}

class FacebookToken implements Serializable {
	/**
	 * Generated SerialVersionUID.
	 */
	private static final long serialVersionUID = -1358009140296532510L;
	public String accessToken;
	public String id;
	public long expiresIn;
}
