package com.intuit.project.phlogit.socialnetwork;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.facebook.android.DialogError;
import com.facebook.android.Facebook;
import com.facebook.android.Facebook.DialogListener;
import com.facebook.android.FacebookError;
import com.intuit.project.phlogit.Phlogit;
import com.intuit.project.phlogit.R;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.Photos;
import com.intuit.project.phlogit.provider.CustomDatabaseHelper.TripDetails;
import com.intuit.project.phlogit.util.Configuration;

public class WebViewClientActivity extends Activity {
	protected static final int COMPLETE = 1;

	protected static final int PROGRESS_DIALOG_ACTIVITY = 100;

	private WebView webview;

	Facebook facebook = new Facebook(Configuration.getFacebookAuthKey());
	
	public final Handler webpageDownloadCompleteHandler = new Handler() {

		public void handleMessage(Message msg) {
			if (msg.what == COMPLETE) {
				webview.setFocusable(true);
				webview.requestFocus();
			}

		}
	};

	protected String returnURL;

	private EditText input;

	private Button saveBtn;
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		facebook.authorizeCallback(requestCode, resultCode, data);

		finish();
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		final boolean fetchProfile = getIntent().getBooleanExtra("fetchProfile", true);
		
		final boolean isFacebook = getIntent().getBooleanExtra("isfacebook", false);
		final String photoPath = getIntent().getStringExtra("Photo");
		final String albumName = getIntent().getStringExtra("Album");
		final String addComments = getIntent().getStringExtra("AddComments");
		final String likes = getIntent().getStringExtra("Likes");
		final String unLikes = getIntent().getStringExtra("Unlikes");
		final String photoId = getIntent().getStringExtra("PhotoId");
		final String comments = getIntent().getStringExtra("Comments");
		
		if(isFacebook) {
			facebook.authorize(this, new String[]{ "user_photos,publish_checkins,publish_actions,publish_stream"}, new DialogListener() {
	            @Override
	            public void onComplete(Bundle values) {
	            	FacebookSocialNetwork socialNetwork = new FacebookSocialNetwork(Phlogit.getPhlogitApplicationContext());//(FacebookSocialNetwork)SocialNetwork.getProvider(SocialNetwork.SOCIAL_NETWORK_FACEBOOK);
					socialNetwork.setTokens(facebook.getAccessToken(), (values != null ? values.get("id") : null), facebook.getAccessExpires());
					socialNetwork.fetchProfile();
					if(!TextUtils.isEmpty(photoPath)) {
						socialNetwork.uploadPhoto(photoPath, albumName, null);
					} else if("true".equals(addComments)) {
						socialNetwork.addComment(photoId, comments);
						Toast.makeText(WebViewClientActivity.this, "Comments added", Toast.LENGTH_LONG).show();
					} else if("true".equals(likes)) {
						socialNetwork.like(photoId);
						Toast.makeText(WebViewClientActivity.this, "Photo Liked", Toast.LENGTH_LONG).show();
					} else if("true".equals(unLikes)) {
						socialNetwork.unLike(photoId);
						Toast.makeText(WebViewClientActivity.this, "Photo Unliked", Toast.LENGTH_LONG).show();
					}
					finish();
	            }

	            @Override
	            public void onCancel() {
	            	Toast.makeText(WebViewClientActivity.this, "Cancel!", Toast.LENGTH_LONG).show();
	            }

				@Override
				public void onFacebookError(FacebookError e) {
					Toast.makeText(WebViewClientActivity.this, "FacebookError "+e, Toast.LENGTH_LONG).show();
				}

				@Override
				public void onError(DialogError e) {
					Toast.makeText(WebViewClientActivity.this, "Error "+e, Toast.LENGTH_LONG).show();
					
				}
	        });
		} else {
			setContentView(R.layout.webview_activity);
			
			String authUrl = getIntent().getStringExtra("authUrl");
			
			webview = (WebView) findViewById(R.id.webview);
			webview.getSettings().setJavaScriptEnabled(true);
			WebSettings websettings = webview.getSettings();
			websettings.setLoadWithOverviewMode(true);
	
			websettings.setJavaScriptEnabled(true);
			webview.setWebViewClient(new WebViewClient() {
				
				@Override
				public void onReceivedSslError (WebView view, SslErrorHandler handler, SslError error) {
					 handler.proceed() ;
				}
	
				@Override
				public void onLoadResource(WebView view, String url) {
					//Twitter
					if(url.startsWith("http://appsforandroid.blogspot.com")) {
						Uri uri = Uri.parse(url);
						String verifier = uri.getQueryParameter("oauth_verifier");
						TwitterSocialNetwork socialNetwork = (TwitterSocialNetwork)SocialNetwork.getProvider(SocialNetwork.SOCIAL_NETWORK_TWITTER);
						socialNetwork.setTokens(verifier);
						socialNetwork.fetchProfile();
						finish();
					}
				}
	
			});
			
			webview.loadUrl(authUrl);
		}
	}
}