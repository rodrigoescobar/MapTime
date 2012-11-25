package com.maptime.maptime;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * Class used for drawing the user's current location 
 * onto the map in MainActivity
 */

public class LocationOverlay extends ItemizedOverlay<OverlayItem>{

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>(); //Local list of OverlayItems. Contains one item.
	private Context mContext; //The activity that the object is attached to. Always a MainActivity
	Thread locationThread; //Local thread for getting the current location
	private volatile boolean stop = false; //If true, stop the above thread
	
	/**
	 * Standard constructor
	 * @param arg0 Drawable containing the image used for current location
	 * @param con The activity that the object is attached to. Always a MainActivity
	 */
	
	public LocationOverlay(Drawable arg0, Context con) {
		super(boundCenter(arg0));
		
		mContext = con;
		populate();
		locationThread = new Thread(new LocationGetter());
		locationThread.start();
	}
	
	protected OverlayItem createItem(int arg0) {
		
		return mOverlays.get(arg0);
	}

	/**
	 * Sets the OverlayItem in mOverlays to be located at the user's current lcoation
	 * @param gp The user's current location
	 */
	
	private void setLocation(final GeoPoint gp) {
		System.out.println(gp);
		if (mOverlays.size() == 1) {
			mOverlays.set(0,new OverlayItem(gp,"You are here",""));
		}
		else {
			mOverlays.add(new OverlayItem(gp,"You are here",""));
		}
		populate();
		((MainActivity)mContext).mapView.postInvalidate();
	}
	
	/**
	 * @return The size of the local List of OverlayItems
	 */
	
	public int size() {
		
		return mOverlays.size();
	}

	/**
	 * When called, stops the thread that gets location updates
	 */
	
	public void stopGPS() {

		stop = true;
		do {
			locationThread.interrupt();
		} while (locationThread.isAlive());

	}
	
	/**
	 * Thread which continuously checks the user's current location, and sets the icon to that location
	 */
	
	private class LocationGetter implements Runnable {

		Location curLocGPS; //user's current location from GPS
		Location curLocNetwork; //user's current location from Network
		Location curLocFinal; //The loaction we want to use
		
		@SuppressLint("NewApi")
		public void run() {

			((MainActivity)mContext).runOnUiThread(new Runnable() {
				public void run() {
					String lProvNetwork = LocationManager.NETWORK_PROVIDER;
					String lProvGPS = LocationManager.GPS_PROVIDER;
					curLocNetwork = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvNetwork);
					if (lProvGPS != null) {
						curLocGPS = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvGPS);
					}
					if(curLocGPS != null && curLocGPS.getTime() > (System.currentTimeMillis() - 5000)) {
						curLocFinal = curLocGPS;
					}
					else {
						curLocFinal = curLocNetwork;
					}
					//Log.i("test",lProv);
					if (curLocFinal != null) { //first time this is run, animate the map over to where the icon is
						((MainActivity)mContext).mapView.getController().animateTo(
								new GeoPoint((int)(curLocFinal.getLatitude()*1000000.0),
										(int)(curLocFinal.getLongitude()*1000000.0)));
					}
				}
			});
			
			while(!stop) {
				String lProvNetwork = LocationManager.NETWORK_PROVIDER;
				String lProvGPS = LocationManager.GPS_PROVIDER;
				curLocNetwork = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvNetwork);
				if (lProvGPS != null) {
					curLocGPS = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvGPS);
				}
				if(curLocGPS != null && curLocGPS.getTime() > (System.currentTimeMillis() - 5000)) {
					curLocFinal = curLocGPS;
				}
				else {
					curLocFinal = curLocNetwork;
					((MainActivity) mContext).lMan.requestSingleUpdate(lProvNetwork, ((MainActivity) mContext).locUpNetwork, null);
					curLocFinal = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvNetwork);
				}
				Log.i("test",curLocFinal.toString());
				if (curLocFinal != null) {
					setLocation(new GeoPoint((int)(curLocFinal.getLatitude()*1000000.0),
							(int)(curLocFinal.getLongitude()*1000000.0)));
				}
				try {
					Thread.sleep(4000); //check every 4 seconds
				} catch (InterruptedException e) {
					break; //If interrupted, break out of the loop.
				}

			}
			
		}
		
	}
	
}
