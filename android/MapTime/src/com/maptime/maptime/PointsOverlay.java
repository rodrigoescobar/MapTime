package com.maptime.maptime;

import java.util.ArrayList;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class PointsOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>(); //Local list of OverlayItems
	//Dedicated indexes: 0 is the start point of the route, 1 is the end point, rest go from 1 after start to 1 before end
	private volatile Context mContext; //Activity that this object is attached to, always a MainActivity
	private boolean isPinch  =  false; //Used for determining taps
	private String TAG = "TapHandler"; //
	private boolean navMode = false; //Is the user currently setting points in the route
	private volatile boolean end = false; //Do we want to terminate the geofencing thread
	private GeoPoint startPoint, endPoint; //GeoPoints used to set the start and end OverlayItems
	Thread geoFence; //Thread used for geofencing
	Handler alertHandler = new Handler() { //Handler used to get geofencing alerts processed on the main thread
        public void handleMessage(final Message msgs) {
        	alertUser(mOverlays.get(msgs.arg1));
        }
        };
	
        /**
         * Standard constructor
         * @param defaultMarker Drawable containing map pin images
         * @param context Activity that this object is attached to, always a MainActivity
         */
        
	public PointsOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		//((MainActivity) mContext).lMan = locMan;
		//locUp = new LocationUpdater();
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, (float) 100.0, ((MainActivity)mContext).locUp);
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, (float) 100.0, new LocationUpdater());
		geoFence = new Thread(new GeoFenceTask());
		geoFence.start();
		populate();
	}
	
	/**
	 * Constructer used from pre-existing information
	 * @param ois List of OverlayItems
	 * @param start GeoPoint for start of route
	 * @param end GeoPoint for end of route
	 * @param defaultMarker Drawable containing map pin images
	 * @param context Activity that this object is attached to, always a MainActivity
	 */
	
	public PointsOverlay(ArrayList<ParcelableOverlayItem> ois, ParcelableGeoPoint start, ParcelableGeoPoint end, Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, (float) 100.0, new LocationUpdater());
		//((MainActivity) mContext).lMan.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, (float) 100.0, new LocationUpdater());
		ArrayList<OverlayItem> newOIs = new ArrayList<OverlayItem>();
		for (ParcelableOverlayItem poi:ois) {
			newOIs.add(new OverlayItem(poi.getPoint(), poi.getTitle(), poi.getSnippet()));
		}
		mOverlays = newOIs;
		if (start != null) {
			startPoint = new GeoPoint(start.getLatitudeE6(), start.getLongitudeE6());
		}
		if (end != null) {
			endPoint = new GeoPoint(end.getLatitudeE6(), end.getLongitudeE6());
		}
		if (start != null && end == null) {
			navMode = true;
		}
		populate();
	}
	
	/**
	 * 
	 */
	
	public void clearMOverLays() {
		mOverlays = new ArrayList<OverlayItem>();
	}
	
	/**
	 * Adds an OverlayItem to the Overlay
	 * @param overlay The OverlayItem to add
	 */
	
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}
	
	/**
	 * Setter for the start point of the route
	 * @param oi The start point of the route the user has entered
	 */
	
	public void setStartPointOverlay(OverlayItem oi) {
		if (mOverlays.size() == 0) { //If it doesn't exist, create it, else set it to it's dedicated index
			mOverlays.add(oi);
		}
		else {
			mOverlays.set(0, oi);
		}
		populate();
	}
	
	/**
	 * Setter for the end point of the route
	 * @param oi The end point of the route the user has entered
	 */
	
	public void setEndPointOverlay(OverlayItem oi) {
		if (mOverlays.size() == 1) { //If it doesn't exist, create it, else set it to it's dedicated index
			mOverlays.add(oi);
		}
		else {
			mOverlays.set(1, oi);
		}
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return this.mOverlays.get(i);
	}
	
	/**
	 * Getter for the local List mOverlays
	 * @return The local list of OverlayItems
	 */
	
	public ArrayList<OverlayItem> getMOverLays() {
		return mOverlays;
	}
	
	/**
	 * @return The size of the local OverlayItem list
	 */
	
	@Override
	public int size() {
		return mOverlays.size();
	}
	
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item;
		if (mOverlays.size() == 2) { //If the timeline hasn't been set yet, return the message of the points we have
			item = mOverlays.get(index);
		}
		else { //Else, if we're touching the start or end points, show the start and end timeline descriptions instead
			if (index == 0) {
				item = mOverlays.get(2);
			}
			else if (index == 1) {
				item = mOverlays.get(mOverlays.size()-1);
			}
			else {
				item = mOverlays.get(index);
			}
		}
		AlertDialog.Builder  dialog = new AlertDialog.Builder(mContext); //Show the TimePoint descriptions
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
		return true;
	}
	
	@Override
	public boolean onTap(GeoPoint p, MapView map){
		if (isPinch) {
			return false;
		} else {
			//Log.i(TAG,"TAP!"); 
			if ( p!=null ) {
				
				//handleGeoPoint(p);
				if (navMode && startPoint == null) {
					startPoint = p;
					AlertDialog.Builder  dialog = new AlertDialog.Builder(mContext);
					dialog.setTitle("Navigation Mode");
					dialog.setMessage("Now tap the endPoint of your route");
					//dialog.setNeutralButton("OK", null);
					dialog.show();
				} else if (navMode && endPoint == null) {
					endPoint = p;
					((MainActivity)mContext).mapView.postInvalidate();
				}
				return super.onTap(p, map); // We handled the tap
			} else {			
				return false; // Null GeoPoint
			}
			
		}
		
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent e, MapView mapView) { //for determining if a touch event is a tap, slide or pinch
		
		int fingers = e.getPointerCount();
		if( e.getAction()==MotionEvent.ACTION_DOWN ) {
			
			isPinch=false;  // Touch DOWN, don't know if it's a pinch yet
		}
		
		if ( e.getAction()==MotionEvent.ACTION_MOVE && fingers==2 ) {
			
    	isPinch=true;   // Two fingers, definitely a pinch
    	}
		
    return super.onTouchEvent(e,mapView);
	}
	
	/**
	 * Setter for navMode
	 * @param b Enable navMode
	 */
	
	public void setNavMode(boolean b) {
		navMode = b;
	}
	
	/**
	 * Getter for startPoint
	 * @return This PointsOverlay's startPoint
	 */
	
	public GeoPoint getStartPoint() {
		return startPoint;
	}
	
	/**
	 * Getter for endPoint
	 * @return This PointsOverlay's endPoint
	 */
	
	public GeoPoint getEndPoint() {
		return endPoint;
	}
	
	/**
	 * Clears startPoint and endPoint from this PointsOverlay
	 */
	
	public void clearPoints() {
		startPoint = null;
		endPoint = null;
	}
	
	/**
	 * Clears the OverlayItem list except for the first two in the list, 
	 * which correspond to the start and end of the route.
	 */
	
	public void clearTimePoints(){
		for (int i = mOverlays.size()-1; i > 1; i--) {
			mOverlays.remove(i);
		}
	}
	
	/**
	 * Function which is called once the user gets within a certain radius of an OverlayItem
	 * @param oi The OverlayItem the user is now close to
	 */
	
	private void alertUser(OverlayItem oi) {
		AlertDialog.Builder  dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(oi.getTitle());
		dialog.setMessage(oi.getSnippet());
		dialog.show();
	}
	
	/**
	 * Ends the thread that works out if the user has entered some distance from a point
	 */
	
	public void stopGPS() {
		//((MainActivity) mContext).lMan.removeUpdates(locUp);
		end = true;
		do {
			geoFence.interrupt();
		} while (geoFence.isAlive());
		//locUp = null;
		//((MainActivity) mContext).lMan = null;
	}
	
	/**
	 * Task that implements geofencing, that is, working out if a user has crossed a 
	 * distance threshold of a point
	 */
	
	private class GeoFenceTask implements Runnable {

		ArrayList<Double> distances = new ArrayList<Double>(); //List of distances from each point the user is
		boolean isInit = false; //is the location service initialised
		Location curLocGPS; //user's current location from GPS
		Location curLocNetwork; //user's current location from Network
		Location curLocFinal; //The loaction we want to use
		final static double threshold = 0.1; //distance in KM from timeline point that we want to alert the user
		
		public void run() {
						
			while (!end) {
				String lProvNetwork = LocationManager.NETWORK_PROVIDER;
				String lProvGPS = LocationManager.GPS_PROVIDER;
				curLocGPS = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvGPS);
				curLocNetwork = ((MainActivity) mContext).lMan.getLastKnownLocation(lProvNetwork);
				//Log.i("test",lProv);
				if(curLocGPS != null && curLocGPS.getTime() > (System.currentTimeMillis() - 5000)) {
					curLocFinal = curLocGPS;
				}
				else {
					curLocFinal = curLocNetwork;
				}
				if (curLocFinal != null) {
					//Log.i("Cur Loc", curLoc.toString());
					if (!isInit || distances.size() != mOverlays.size()) { //if we have a different amount of points to last time through
						distances.clear(); //reset the distances list and start again
						for (OverlayItem oi: mOverlays) {
							distances.add(MainActivity.distanceKm(curLocFinal.getLatitude(), curLocFinal.getLongitude(),
								(double)(oi.getPoint().getLatitudeE6())/1000000.0, 
								(double)(oi.getPoint().getLongitudeE6())/1000000.0));
						}
						isInit = true;
					}
					else { //else if the user has passed within THRESHOLD km of a point, alert the user
						for (int i = 0; i < distances.size(); i++) {
							double curDist = MainActivity.distanceKm(curLocFinal.getLatitude(), curLocFinal.getLongitude(),
								(double)(mOverlays.get(i).getPoint().getLatitudeE6())/1000000.0, 
								(double)(mOverlays.get(i).getPoint().getLongitudeE6())/1000000.0);
							if (curDist < threshold && distances.get(i) > threshold) {
								Message closePoint = new Message();
								closePoint.arg1 = i;
								alertHandler.sendMessage(closePoint);
							}
							distances.set(i, (MainActivity.distanceKm(curLocFinal.getLatitude(), curLocFinal.getLongitude(),
								(double)(mOverlays.get(i).getPoint().getLatitudeE6())/1000000.0, 
								(double)(mOverlays.get(i).getPoint().getLongitudeE6())/1000000.0)));
						}
					}
				}
				try {
					//Log.i("Sleep","yes");
					Thread.sleep(6000); //Wait for a few seconds, GPS doesn't update that often.
					//Log.i("Sleep","no");
				} catch (InterruptedException e) {
					break;
				}
				
			}
			
		}
		
	}
	
}
