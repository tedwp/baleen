package org.mixare.data;

import org.json.JSONException;
import org.mixare.MixView;

import android.util.Log;

public class JsonT4j extends DataHandler {

	public void load(String user, String text, double lat, double lon) throws NumberFormatException, JSONException {
		Log.d(MixView.TAG, "load Twitter4j JSON data");
		Log.d(MixView.TAG, "load Twitter4j JSON data - user: "+user);
		Log.d(MixView.TAG, "load Twitter4j JSON data - text: "+text);
		Log.d(MixView.TAG, "load Twitter4j JSON data - lat: "+lat);
		Log.d(MixView.TAG, "load Twitter4j JSON data - lon: "+lon);
		createMarker( text, lat, lon, 0, null);
	}
}
