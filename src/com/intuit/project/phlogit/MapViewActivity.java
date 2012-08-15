package com.intuit.project.phlogit;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.intuit.project.phlogit.util.NetworkUtil;
import com.intuit.project.phlogit.widget.PhlogItemOverlay;

public class MapViewActivity extends MapActivity {

	protected Road mRoad;
	protected List<Overlay> mapOverlays;

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			TextView textView = (TextView) findViewById(R.id.description);
			textView.setText(mRoad.mName + " " + mRoad.mDescription);
			MapOverlay mapOverlay = new MapOverlay(mRoad, mapView);
            List<Overlay> listOfOverlays = mapView.getOverlays();
            listOfOverlays.clear();
            listOfOverlays.add(mapOverlay);
            mapView.invalidate();
		}
	};
	private MapView mapView;
	private PhlogItemOverlay itemizedoverlay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.map_view);
		mapView = (MapView) findViewById(R.id.mapview);
		mapView.setBuiltInZoomControls(true);
		
		mapOverlays = mapView.getOverlays();
		Drawable drawable = MapViewActivity.this.getResources().getDrawable(R.drawable.about);
		itemizedoverlay = new PhlogItemOverlay(drawable, this);
		
		GeoPoint point = new GeoPoint(19240000,-99120000);
		OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
		
		itemizedoverlay.addOverlay(overlayitem);
		mapOverlays.add(itemizedoverlay);
		
		new Thread() {
			@Override
			public void run() {
				double fromLat = 12.983333, fromLon = 77.583333;
				double toLat = 13.083333, toLon = 80.283333;
//				double toLat = 50.45, toLon = 30.523333;
				String url = getUrl(fromLat, fromLon, toLat, toLon);
				String route = NetworkUtil.get(url);

				ByteArrayInputStream bais = new ByteArrayInputStream(
						route.getBytes());
				InputSource inputSource = new InputSource(bais);

				mRoad = getRoute(inputSource);
				mHandler.sendEmptyMessage(0);
			}
		}.start();
	}

	public static String getUrl(double fromLat, double fromLon, double toLat,
			double toLon) {// connect to map web service
		StringBuffer urlString = new StringBuffer();
		urlString.append("http://maps.google.com/maps?f=d&hl=en");
		urlString.append("&saddr=");// from
		urlString.append(Double.toString(fromLat));
		urlString.append(",");
		urlString.append(Double.toString(fromLon));
		urlString.append("&daddr=");// to
		urlString.append(Double.toString(toLat));
		urlString.append(",");
		urlString.append(Double.toString(toLon));
		urlString.append("&ie=UTF8&0&om=0&output=kml");
		return urlString.toString();
	}

	public static Road getRoute(InputSource is) {
		KMLHandler handler = new KMLHandler();
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(is, handler);
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return handler.mRoad;
	}

}

class KMLHandler extends DefaultHandler {
	Road mRoad;
	boolean isPlacemark;
	boolean isRoute;
	boolean isItemIcon;
	private Stack mCurrentElement = new Stack();
	private String mString;

	public KMLHandler() {
		mRoad = new Road();
	}

	public void startElement(String uri, String localName, String name,
			Attributes attributes) throws SAXException {
		mCurrentElement.push(localName);
		if (localName.equalsIgnoreCase("Placemark")) {
			isPlacemark = true;
			mRoad.mPoints = addPoint(mRoad.mPoints);
		} else if (localName.equalsIgnoreCase("ItemIcon")) {
			if (isPlacemark)
				isItemIcon = true;
		}
		mString = new String();
	}

	public void characters(char[] ch, int start, int length)
			throws SAXException {
		String chars = new String(ch, start, length).trim();
		mString = mString.concat(chars);
	}

	public void endElement(String uri, String localName, String name)
			throws SAXException {
		if (mString.length() > 0) {
			if (localName.equalsIgnoreCase("name")) {
				if (isPlacemark) {
					isRoute = mString.equalsIgnoreCase("Route");
					if (!isRoute) {
						mRoad.mPoints[mRoad.mPoints.length - 1].mName = mString;
					}
				} else {
					mRoad.mName = mString;
				}
			} else if (localName.equalsIgnoreCase("color") && !isPlacemark) {
				mRoad.mColor = Integer.parseInt(mString, 16);
			} else if (localName.equalsIgnoreCase("width") && !isPlacemark) {
				mRoad.mWidth = Integer.parseInt(mString);
			} else if (localName.equalsIgnoreCase("description")) {
				if (isPlacemark) {
					String description = cleanup(mString);
					if (!isRoute)
						mRoad.mPoints[mRoad.mPoints.length - 1].mDescription = description;
					else
						mRoad.mDescription = description;
				}
			} else if (localName.equalsIgnoreCase("href")) {
				if (isItemIcon) {
					mRoad.mPoints[mRoad.mPoints.length - 1].mIconUrl = mString;
				}
			} else if (localName.equalsIgnoreCase("coordinates")) {
				if (isPlacemark) {
					if (!isRoute) {
						String[] xyParsed = split(mString, ",");
						double lon = Double.parseDouble(xyParsed[0]);
						double lat = Double.parseDouble(xyParsed[1]);
						mRoad.mPoints[mRoad.mPoints.length - 1].mLatitude = lat;
						mRoad.mPoints[mRoad.mPoints.length - 1].mLongitude = lon;
					} else {
						String[] coodrinatesParsed = split(mString, " ");
						mRoad.mRoute = new double[coodrinatesParsed.length][2];
						for (int i = 0; i < coodrinatesParsed.length; i++) {
							String[] xyParsed = split(coodrinatesParsed[i], ",");
							for (int j = 0; j < 2 && j < xyParsed.length; j++)
								mRoad.mRoute[i][j] = Double
										.parseDouble(xyParsed[j]);
						}
					}
				}
			}
		}
		mCurrentElement.pop();
		if (localName.equalsIgnoreCase("Placemark")) {
			isPlacemark = false;
			if (isRoute)
				isRoute = false;
		} else if (localName.equalsIgnoreCase("ItemIcon")) {
			if (isItemIcon)
				isItemIcon = false;
		}
	}

	private String cleanup(String value) {
		String remove = "<br/>";
		int index = value.indexOf(remove);
		if (index != -1)
			value = value.substring(0, index);
		remove = "&#160;";
		index = value.indexOf(remove);
		int len = remove.length();
		while (index != -1) {
			value = value.substring(0, index).concat(
					value.substring(index + len, value.length()));
			index = value.indexOf(remove);
		}
		return value;
	}

	public Point[] addPoint(Point[] points) {
		Point[] result = new Point[points.length + 1];
		for (int i = 0; i < points.length; i++)
			result[i] = points[i];
		result[points.length] = new Point();
		return result;
	}

	private static String[] split(String strString, String strDelimiter) {
		String[] strArray;
		int iOccurrences = 0;
		int iIndexOfInnerString = 0;
		int iIndexOfDelimiter = 0;
		int iCounter = 0;
		if (strString == null) {
			throw new IllegalArgumentException("Input string cannot be null.");
		}
		if (strDelimiter.length() <= 0 || strDelimiter == null) {
			throw new IllegalArgumentException(
					"Delimeter cannot be null or empty.");
		}
		if (strString.startsWith(strDelimiter)) {
			strString = strString.substring(strDelimiter.length());
		}
		if (!strString.endsWith(strDelimiter)) {
			strString += strDelimiter;
		}
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {
			iOccurrences += 1;
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
		}
		strArray = new String[iOccurrences];
		iIndexOfInnerString = 0;
		iIndexOfDelimiter = 0;
		while ((iIndexOfDelimiter = strString.indexOf(strDelimiter,
				iIndexOfInnerString)) != -1) {
			strArray[iCounter] = strString.substring(iIndexOfInnerString,
					iIndexOfDelimiter);
			iIndexOfInnerString = iIndexOfDelimiter + strDelimiter.length();
			iCounter += 1;
		}

		return strArray;
	}
}

class Point {
	String mName;
	String mDescription;
	String mIconUrl;
	double mLatitude;
	double mLongitude;
}

class Road {
	public String mName;
	public String mDescription;
	public int mColor;
	public int mWidth;
	public double[][] mRoute = new double[][] {};
	public Point[] mPoints = new Point[] {};
}

class MapOverlay extends com.google.android.maps.Overlay {
    Road mRoad;
    ArrayList<GeoPoint> mPoints;

    public MapOverlay(Road road, MapView mv) {
            mRoad = road;
            if (road.mRoute.length > 0) {
                    mPoints = new ArrayList<GeoPoint>();
                    for (int i = 0; i < road.mRoute.length; i++) {
                            mPoints.add(new GeoPoint((int) (road.mRoute[i][1] * 1000000),
                                            (int) (road.mRoute[i][0] * 1000000)));
                    }
                    int moveToLat = (mPoints.get(0).getLatitudeE6() + (mPoints.get(
                                    mPoints.size() - 1).getLatitudeE6() - mPoints.get(0)
                                    .getLatitudeE6()) / 2);
                    int moveToLong = (mPoints.get(0).getLongitudeE6() + (mPoints.get(
                                    mPoints.size() - 1).getLongitudeE6() - mPoints.get(0)
                                    .getLongitudeE6()) / 2);
                    GeoPoint moveTo = new GeoPoint(moveToLat, moveToLong);

                    MapController mapController = mv.getController();
                    mapController.animateTo(moveTo);
                    mapController.setZoom(10);
            }
    }

    @Override
    public boolean draw(Canvas canvas, MapView mv, boolean shadow, long when) {
            super.draw(canvas, mv, shadow);
            drawPath(mv, canvas);
            return true;
    }

    public void drawPath(MapView mv, Canvas canvas) {
            int x1 = -1, y1 = -1, x2 = -1, y2 = -1;
            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(2);
            for (int i = 0; i < mPoints.size(); i++) {
                    android.graphics.Point point = new android.graphics.Point();
                    mv.getProjection().toPixels(mPoints.get(i), point);
                    x2 = point.x;
                    y2 = point.y;
                    if (i > 0) {
                            canvas.drawLine(x1, y1, x2, y2, paint);
                    }
                    x1 = x2;
                    y1 = y2;
            }
    }
}
