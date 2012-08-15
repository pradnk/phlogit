package com.intuit.project.phlogit.util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.xml.sax.InputSource;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.intuit.project.phlogit.Phlogit;

public class NetworkUtil {

	private static final int DEFAULT_TIMEOUT_SECONDS = 60000;

	private static final String CONTENT_TYPE = "Content-Type";

	private static final String DEFAULT_CONTENT_TYPE = "application/xml";

	private static final String PDF_CONTENT_TYPE = "application/pdf";

	private static final String SOAP_ACTION = "SOAPAction";

	private static final String ENCODING_UTF_8 = "UTF-8";

	private static final String KEY_CODE = "code";

	private static final String KEY_HAND_SHAKE = "HAND_SHAKE";

	private static final String APPLICATION_NAME = "SnapTax";

	private static final String BEACON_SOAP_ACTION_URL = "beacon_soap_action_url";

	private static boolean connected  = false;

	private static boolean connectedWifi = false;

	public static String get(String uri) {
		HttpGet request = new HttpGet(uri);

		return executeGetStringRequest(request);
	}
	
	public static byte[] getRawData(String uri) {
		HttpGet request = new HttpGet(uri);

		return executeGetRawRequest(request);
	}
	

	public static String getDisqualificationContent(String uri) {
		HttpGet request = new HttpGet(uri);
		return executeGetStringRequest(request);
	}

	private static String executeGetStringRequest(HttpGet request) {
		HttpClient httpClient = getSecureHttpClient();

		String response = "";

		try {

			HttpResponse httpResponse = httpClient.execute(request);
			HttpResponseHandler responseHandler = new SimpleStringResponseHandler();
			response = responseHandler.processResponse(httpResponse);

		} catch (ClientProtocolException e) {
			Log.e("Exception while Executing the getRequest", ""+e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Exception while Executing the getRequest", ""+e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return response;
	}
	
	private static byte[] executeGetRawRequest(HttpGet request) {
		HttpClient httpClient = getSecureHttpClient();

		byte[] response = null;

		try {

			HttpResponse httpResponse = httpClient.execute(request);
			HttpRawResponseHandler responseHandler = new RawResponseHandler();
			response = responseHandler.processResponse(httpResponse);

		} catch (ClientProtocolException e) {
			Log.e("Exception while Executing the getRequest", ""+e);
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("Exception while Executing the getRequest", ""+e);
		} finally {
			httpClient.getConnectionManager().shutdown();
		}
		return response;
	}


	public static String post(String uri, String body) {
		return post(uri, body, DEFAULT_CONTENT_TYPE, DEFAULT_TIMEOUT_SECONDS);
	}

	public static String post(String uri, String body, String contentType) {
		return post(uri, body, contentType, DEFAULT_TIMEOUT_SECONDS);
	}

	public static String post(String uri, String body, int timeoutInSeconds) {
		return post(uri, body, DEFAULT_CONTENT_TYPE, timeoutInSeconds);
	}

	public static String post(String uri, String body, String contentType, int timeoutInSeconds) {
		{
			HttpClient httpClient = getSecureHttpClient();

			HttpPost request = new HttpPost(uri);
			
			String response = "";

			try {
				request.setEntity(new StringEntity(body));
				HttpResponse httpResponse = httpClient.execute(request);

				HttpResponseHandler responseHandler = new SimpleStringResponseHandler();
				response = responseHandler.processResponse(httpResponse);

			} catch (ClientProtocolException e) {
				Log.e("Exception while executing the postRequest", e.getMessage());
			} catch (IOException e) {
				Log.e("Exception while executing the postRequest", e.getMessage());
			} catch (Exception e) {
				Log.e("Exception while executing the postRequest", e.getMessage());
			} finally {
				httpClient.getConnectionManager().shutdown();
			}

			return response;
		}

	}

	public static String postPDFRequest(String uri, String body, ByteArrayOutputStream outputStream) {

		HttpClient httpClient = getSecureHttpClient();
		
		String responseBody = "";
		HttpPost postRequest = new HttpPost(uri);
		try {

			postRequest.setEntity(new StringEntity(body));

			HttpResponse response = httpClient.execute(postRequest);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				for (int i = 0; i < response.getAllHeaders().length; i++) {
					if (response.getAllHeaders()[i].getName().equals(CONTENT_TYPE)) {
						if (response.getAllHeaders()[i].getValue().equals(PDF_CONTENT_TYPE)) {
							InputSource insrc = new InputSource(response.getEntity().getContent());
							byte buf[] = new byte[1024];

							int len = 0;
							while ((len = insrc.getByteStream().read(buf)) > 0)
								outputStream.write(buf, 0, len);

							outputStream.close();

						} else {
							HttpResponseHandler responseHandler = new SimpleStringResponseHandler();
							responseBody = responseHandler.processResponse(response);
						}
					}
				}
			} else {
				Log.e(APPLICATION_NAME, "Post call failed with error code: ");
			}
		} catch (UnsupportedEncodingException e) {
			responseBody = "error";
			Log.e(APPLICATION_NAME, e.getMessage());
		} catch (IOException e) {
			responseBody = "error";
			Log.e(APPLICATION_NAME, e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return responseBody;
	}

	/**
	 * Constructs and returns a secure http client.
	 * @return
	 */
	private static HttpClient getSecureHttpClient() {
		DefaultHttpClient httpClient = new DefaultHttpClient();
		return httpClient;
	}

	public static byte[] postData(String uri, String body, String contentType) {
		String responseData = post(uri, body);
		return responseData.getBytes();// responseBody;
	}

	public static String postImage(String uri, byte[] imageData, String contentType, String authid,
			String ticket, boolean usingHighQualityImage) {
		HttpPost postRequest = new HttpPost(uri);
		postRequest.setHeader("Content-Type", "multipart/form-data");
		postRequest.setHeader("AuthId", authid);
		postRequest.setHeader("TicketNumber", ticket);

		HttpClient httpClient = getSecureHttpClient();

		String responseString = "";

		if (usingHighQualityImage)
			postRequest.setHeader("Template", "Dynamic");
		else
			postRequest.setHeader("Template", "Custom");


		try {
			ByteArrayEntity arrayEntity = new ByteArrayEntity(imageData);

			arrayEntity.setContentType(contentType);
			postRequest.setEntity(arrayEntity);

			HttpResponse httpResponse = httpClient.execute(postRequest);

			HttpResponseHandler responseHandler = new SimpleStringResponseHandler();
			responseString = responseHandler.processResponse(httpResponse);

		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
		} catch (Exception e) {
			Log.e("Exception >>>>", e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return responseString;

	}

	public static String postBeacon(String uri, String body) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpPost request = new HttpPost(uri);
		request.addHeader(CONTENT_TYPE, "xml");
		request.addHeader(SOAP_ACTION, BEACON_SOAP_ACTION_URL);

		String responseString = "";

		try {

			byte[] utf8Bytes = body.getBytes("UTF8");
			String utfStr = new String(utf8Bytes, "UTF8");
			request.setEntity(new StringEntity(utfStr));

			HttpResponse httpResponse = httpClient.execute(request);
			HttpResponseHandler responseHandler = new SimpleStringResponseHandler();
			responseString = responseHandler.processResponse(httpResponse);
			
		} catch (ClientProtocolException e) {
			Log.e("ClientProtocolException", e.getMessage());
		} catch (IOException e) {
			Log.e("IOException", e.getMessage());
		} catch (Exception e) {
			Log.e("Exception >>>>", e.getMessage());
		} finally {
			httpClient.getConnectionManager().shutdown();
		}

		return responseString;
	}
	
	public static interface HttpResponseHandler {
		public String processResponse(HttpResponse response) throws IllegalStateException, IOException;
	}
	
	public static class SimpleStringResponseHandler implements HttpResponseHandler
	{

		@Override
		public String processResponse(HttpResponse httpResponse) throws IllegalStateException, IOException {
			StringBuilder responseString = new StringBuilder();
			InputSource inputSrc = new InputSource(httpResponse.getEntity().getContent());
			InputStreamReader reader = new InputStreamReader(inputSrc.getByteStream());
			BufferedReader rd = new BufferedReader(reader);
			String line = null;

			while ((line = rd.readLine()) != null) {
				responseString.append(line);
			}			
			return responseString.toString();
		}
	}
	
	public static interface HttpRawResponseHandler {
		public byte[] processResponse(HttpResponse response) throws IllegalStateException, IOException;
	}
	
	public static class RawResponseHandler implements HttpRawResponseHandler
	{

		@Override
		public byte[] processResponse(HttpResponse httpResponse) throws IllegalStateException, IOException {
			InputSource inputSrc = new InputSource(httpResponse.getEntity().getContent());
			BufferedInputStream bis = new BufferedInputStream(inputSrc.getByteStream());
			byte[] one = new byte[1];
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			while (bis.read(one) != -1) {
				baos.write(one);
			}
			baos.close();
			bis.close();
			return baos.toByteArray();
		}
	}
	
	public static boolean isConnected() {
		ConnectivityManager cm = (ConnectivityManager) Phlogit.getPhlogitApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);		
		try {
			NetworkInfo netInfo = cm.getActiveNetworkInfo();
		    connected = (netInfo != null) && netInfo.isConnected();		
		} catch (Exception e) {
			e.printStackTrace();
		}			
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				if(!connected) {
					Toast.makeText(Phlogit.getPhlogitApplicationContext(), "No network connection to sync", Toast.LENGTH_LONG).show();
				}				
				Looper.loop();
			}
		}.start();
		return connected;
    }

	public static boolean isConnectedWiFi() {
		ConnectivityManager cm = (ConnectivityManager) Phlogit.getPhlogitApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		try {
			connectedWifi = cm.getActiveNetworkInfo().isConnected();
			if (connectedWifi) {
				// check for wifi vs 3g (data connection)
				NetworkInfo netInfo = cm.getActiveNetworkInfo();
				connectedWifi = (netInfo != null) && (netInfo.getTypeName().equals("WIFI"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		new Thread() {
			@Override
			public void run() {
				Looper.prepare();
				if(!connectedWifi) {
					Toast.makeText(Phlogit.getPhlogitApplicationContext(), "No WiFi connection to sync", Toast.LENGTH_LONG).show();
				}				
				Looper.loop();
			}
		}.start();
		return connectedWifi;
    }
}