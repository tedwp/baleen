package org.baleen.data;

import java.net.URLDecoder;
import java.util.ArrayList;

import org.baleen.Marker;
import org.baleen.MixView;
import org.baleen.image.ImageUtilities;

import android.graphics.Bitmap;
import android.util.Log;

/**
 * DataHandler is the model which provides the Marker Objects.
 * 
 * DataHandler is also the Factory for new Marker objects.
 */
public class DataHandler {

	private static final int MAX_OBJECTS = 50;
	private ArrayList<Marker> markerList = new ArrayList<Marker>();

	public void createMarker(String title, double latitude, double longitude, double elevation, String link) {
		Log.d(MixView.TAG, "Debug: DataHandler - createMarker");
		if (markerList.size() < MAX_OBJECTS) {
			String URL = null;
			if (link != null && link.length() > 0)
				URL = "webpage:" + URLDecoder.decode(link);
			

            
            
			Marker ma = new Marker(title, latitude, longitude, elevation, URL);
			
			

            
            
			markerList.add(ma);
			Log.d(MixView.TAG, "Debug: DataHandler - markerList.add");
		}
	}
	
	public void createMarker(String title, double latitude, double longitude, double elevation, String link, String imageURL) {
		Log.d(MixView.TAG, "Debug: DataHandler - createMarker");
		if (markerList.size() < MAX_OBJECTS) {
			String URL = null;
			if (link != null && link.length() > 0)
				URL = "webpage:" + URLDecoder.decode(link);
			
			ImageUtilities.ExpiringBitmap expiring;
            expiring = ImageUtilities.load(imageURL);
            Bitmap bitmap = expiring.bitmap;
            bitmap = ImageUtilities.createProfileImage(bitmap, 88, 88);            
            
			Marker ma = new Marker(title, latitude, longitude, elevation, URL, bitmap);
			
			

            
            
			markerList.add(ma);
			Log.d(MixView.TAG, "Debug: DataHandler - markerList.add");
		}
	}
	
	public void createMarker(String title, double latitude, double longitude, double elevation, String link, String imageURL, String userName) {
		Log.d(MixView.TAG, "Debug: DataHandler - createMarker");
		if (markerList.size() < MAX_OBJECTS) {
			String URL = null;
			if (link != null && link.length() > 0)
				URL = "webpage:" + URLDecoder.decode(link);
			
			ImageUtilities.ExpiringBitmap expiring;
            expiring = ImageUtilities.load(imageURL);
            Bitmap bitmap = expiring.bitmap;
            bitmap = ImageUtilities.createProfileImage(bitmap, 88, 88);            
            
			Marker ma = new Marker(title, latitude, longitude, elevation, URL, bitmap, userName);
			
			

            
            
			markerList.add(ma);
			Log.d(MixView.TAG, "Debug: DataHandler - markerList.add");
		}
	}
	
	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public ArrayList getMarkerList() {
		return markerList;
	}
	
	/**
	 * @deprecated Nobody should get direct access to the list
	 */
	public void setMarkerList(ArrayList markerList) {
		this.markerList = markerList;
	}

	public int getMarkerCount() {
		return markerList.size();
	}
	
	public Marker getMarker(int index) {
		return markerList.get(index);
	}
}
