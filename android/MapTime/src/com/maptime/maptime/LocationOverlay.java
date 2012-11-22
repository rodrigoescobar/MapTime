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
	//LocationManager lMan;
	private Context mContext;
	Thread locationThread;
	//LocationUpdater locUp;
	private volatile boolean stop = false;
	
	public LocationOverlay(Drawable arg0, Context con) {
		super(boundCenter(arg0));
		// TODO Auto-generated constructor stub
		mContext = con;
		//lMan = locMan;
		//locUp = new LocationUpdater();
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, (float) 50.0, ((MainActivity) mContext).locUp);
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, (float) 500.0, new LocationUpdater());
		//mOverlays.add(new OverlayItem(new GeoPoint(0,0),"",""));
		populate();
		locationThread = new Thread(new LocationGetter());
		locationThread.start();
	}

	protected OverlayItem createItem(int arg0) {
		// TODO Auto-generated method stub
		return mOverlays.get(arg0);
	}

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
		((MainActivity)mContext).runOnUiThread(new Runnable() {
			public void run() {
				((MainActivity)mContext).mapView.getController().animateTo(gp);
			}
		});
	}

	public int size() {
		// TODO Auto-generated method stub
		return mOverlays.size();
	}

	public void stopGPS() {
		//((MainActivity) mContext).lMan.removeUpdates(locUp);
		stop = true;
		do {
			locationThread.interrupt();
		} while (locationThread.isAlive());
		//locUp = null;
		//((MainActivity) mContext).lMan = null;
	}
	
	private class LocationGetter implements Runnable {

		Location curLoc;
		
		public void run() {
			// TODO Auto-generated method stub
			while(!stop) {
				//String lProv = ((MainActivity) mContext).lMan.getBestProvider(new Criteria(), true);
				String lProv = LocationManager.GPS_PROVIDER;
				if (lProv != null) {
					curLoc = ((MainActivity) mContext).lMan.getLastKnownLocation(lProv);
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
					break;
				}

			}
			
		}
		
	}
	
}
