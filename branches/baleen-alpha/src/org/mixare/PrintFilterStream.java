/*
Copyright (c) 2007-2010, Yusuke Yamamoto
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the Yusuke Yamamoto nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY Yusuke Yamamoto ``AS IS'' AND ANY
EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL Yusuke Yamamoto BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package org.mixare;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.mixare.data.JsonT4j;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import android.util.Log;

/**
 * <p>This is a code example of Twitter4J Streaming API - filter method support.<br>
 * Usage: java twitter4j.examples.PrintFilterStream [<i>TwitterScreenName</i> <i>TwitterPassword</i>] follow(comma separated) track(comma separated)]<br>
 * </p>
 * @author Yusuke Yamamoto - yusuke at mac.com
 */
public final class PrintFilterStream extends StatusAdapter implements Runnable {

    TwitterStream twitterStream;
    int[] followArray;
    String[] trackArray;
    FilterQuery query;
    
    MixContext ctx;
    
    JsonT4j layer = new JsonT4j();
    
    Double initLat;
    Double initLon;
    Double range = 1.0;
    
    String user;
    String text;
    GeoLocation geolocation;
    Double lat;
    Double lon;
    String imageURL;
    
//    String[][] account;
    Map<String, String> acc;
    Iterator<String> iter;

    public PrintFilterStream(MixContext ctx) {
    	Log.d(MixView.TAG, "Debug: PrintFilterStream entered");
    	this.ctx = ctx;
        String filter;
        String track;
        acc = new HashMap<String, String>();
        // Backup Account
        acc.put("tristiaapp", "mjkluio789");
        acc.put("baleenapp", "mjkluio789");
        iter = acc.keySet().iterator();
/*        account[0][0]="baleenapp";
        account[0][1]="mjkluio789";
        account[1][0]="tristiaapp";
        account[1][1]="mjkluio789";*/
//        twitterStream = new TwitterStreamFactory(this).getInstance("tristiaapp", "mjkluio789");
        twitterStream = new TwitterStreamFactory(this).getInstance("baleenapp", "mjkluio789");
/*        String[] filterSplit = filter.split(",");
        filterArray = new int[filterSplit.length];
        for(int i=0; i< filterSplit.length; i++){
            filterArray[i] = Integer.parseInt(filterSplit[i]);

        }*/
        query = new FilterQuery();
        layer.createMarker("layer default marker 1", 36.8, -122.75, 0, null);
//        layer.createMarker("Marker 1 at Melbourne", -38.2, 144.5, 0, null);
        System.out.println("layer default markerList size: "+layer.getMarkerCount());
        
    }
    
    public JsonT4j getLayer() {
		return layer;
	}

	public void setLayer(JsonT4j layer) {
		this.layer = layer;
	}

	public void run() {
    	Log.d(MixView.TAG, "Debug: PrintFilterStream - run entered");
    	layer.createMarker("layer default marker 2", 37.8, -121.75, 0, null);
//    	layer.createMarker("Marker 2 at Melbourne", -38.2, 145.2, 0, null);
        System.out.println("layer default markerList size: "+layer.getMarkerCount());
        
        while (ctx.curLoc == null) {
        	Log.d(MixView.TAG, "Debug: PrintFilterStream - sleep entered");
        	try {
        		Thread.sleep(100);
        	} catch (InterruptedException e) {
        		// TODO Auto-generated catch block
        		e.printStackTrace();		
        	}
        }

        initLat = ctx.curLoc.getLatitude();
        Log.d(MixView.TAG, "Debug: PrintFilterStream - run - initLat: "+initLat);
        initLon = ctx.curLoc.getLongitude();
        Log.d(MixView.TAG, "Debug: PrintFilterStream - run -  initLon: "+initLon);
        
        try {
			startConsuming(initLat, initLon);
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.d(MixView.TAG, "Debug: PrintFilterStream - run -  TwitterException entered");
			if(iter.hasNext()){
				String accName = iter.next();
				String accPsw = acc.get(accName);
				twitterStream = new TwitterStreamFactory(this).getInstance(accName, accPsw);
				this.run();
			}
			Log.d(MixView.TAG, "Debug: PrintFilterStream - run -  TwitterException left");
		}
    }

    private void startConsuming(double initLat, double initLon) throws TwitterException {
        // filter() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
    	Log.d(MixView.TAG, "Debug: PrintFilterStream - startConsuming entered");
    	layer.createMarker("layer default marker 3", 37.7, -121.95, 0, null);
//    	layer.createMarker("Marker 3 at Melbourne", -37.4, 145.2, 0, null);
        System.out.println("layer default markerList size: "+layer.getMarkerCount());
    	twitterStream.setStatusListener(this);
    	double latRangePosi = initLat + 1;
    	double latRangeNega = initLat - 1;
    	double lonRangePosi = initLon + 1;
    	double longRangeNega = initLon - 1;
//        double[][] loc={{140,-40},{148,-33}};
//    	double[][] loc={{-122.75,36.8},{-121.75,37.8}};
    	double[][] loc={{longRangeNega,latRangeNega},{lonRangePosi,latRangePosi}};
        twitterStream.filter(query.locations(loc));
    }

    public void onStatus(Status status) {
    	Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus entered");
/*		Random randlat = new Random(System.currentTimeMillis());
		Random randlon = new Random(System.currentTimeMillis());
		Random randop = new Random(System.currentTimeMillis());
		Double rlat = (double) randlat.nextInt(100);
		Double rlon = (double) randlon.nextInt(100);
		Double templat;
		Double templon;
		int op = randop.nextInt(100);

		if (op < 50) {
			templat = 36.8 + (rlat/1000);
		} else {
			templat = 36.8 - (rlat/1000);
		}
		
		if (op < 50) {
			templon = -121.95 + (rlon/1000);
		} else {
			templon = -121.95 + (rlon/1000);
		}
		layer.createMarker("layer default marker inner", templat, templon, 0, null);*/
		
//        System.out.println(status.getUser().getName() + " : " + status.getText());
//        System.out.println(status.getUser().getName() + " : " + status.getUser().getLocation());
        Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus username: " + status.getUser().getName());
    	user = status.getUser().getName();
    	text = status.getText();
    	geolocation = status.getGeoLocation();
    	imageURL = status.getUser().getProfileImageURL().toString();
    	if(geolocation == null){
    		Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus geolocation: Null!");
    		lat = null;
    		lon = null;
    	} else {
    		lat = status.getGeoLocation().getLatitude();
    		Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus lat: " + lat);
    		lon = status.getGeoLocation().getLongitude();
    		Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus lon: " + lon);
    		try {
//    			layer.load(user, text, lat, lon);
    			layer.load(user, text, lat, lon, imageURL);
    			System.out.println("layer non-default markerList size: "+layer.getMarkerCount());
    		} catch (NumberFormatException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		} catch (JSONException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
    	}

//		Log.d(MixView.TAG, "Debug: PrintFilterStream - onStatus MarkerList Size: " + layer.getMarkerCount());
    }

    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    }

    public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
    }

    public void onException(Exception ex) {
        ex.printStackTrace();
    }
}
