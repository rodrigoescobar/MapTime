package com.maptime.maptime;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class LocationOverlay extends ItemizedOverlay<OverlayItem>{

	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	LocationManager lMan;
	private Context mContext;
	
	public LocationOverlay(Drawable arg0, LocationManager locMan, Context con) {
		super(boundCenter(arg0));
		// TODO Auto-generated constructor stub
		lMan = locMan;
		lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, (float) 50.0, new LocationUpdater());
		//lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, (float) 500.0, new LocationUpdater());
		mContext = con;
		mOverlays.add(new OverlayItem(new GeoPoint(0,0),"",""));
		populate();
		new Thread(new LocationGetter()).start();
	}

	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

	private void setLocation(GeoPoint gp) {
		System.out.println(gp);
		mOverlays.set(0,new OverlayItem(gp,"",""));
		populate();
		((MainActivity)mContext).mapView.postInvalidate();
	}

	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	private class LocationGetter implements Runnable {

		Location curLoc;
		
		public void run() {
			// TODO Auto-generated method stub
			while(true) {
				//String lProv = lMan.getBestProvider(new Criteria(), true);
				String lProv = LocationManager.GPS_PROVIDER;
				if (lProv != null) {
					curLoc = lMan.getLastKnownLocation(lProv);
				}
				Log.i("test",lProv);
				if (curLoc != null) {
					setLocation(new GeoPoint((int)(curLoc.getLatitude()*1000000.0),(int)(curLoc.getLongitude()*1000000.0)));
				}
				try {
					Thread.sleep(5679);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
			
		}
		
	}
	
}
