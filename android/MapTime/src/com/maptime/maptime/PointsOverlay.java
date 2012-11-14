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
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class PointsOverlay extends ItemizedOverlay {
	
	LocationManager lMan;
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private boolean isPinch  =  false;
	private String TAG = "TapHandler";
	private boolean navMode = false;
	private GeoPoint startPoint, endPoint;
	
	public PointsOverlay(Drawable defaultMarker, Context context, LocationManager locMan) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		lMan = locMan;
		new Thread(new GeoFenceTask()).start();
	}
	
	public PointsOverlay(ArrayList<ParcelableOverlayItem> ois, ParcelableGeoPoint start, ParcelableGeoPoint end, Drawable defaultMarker, Context context, LocationManager locMan) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
		lMan = locMan;
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
		populate();
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
	 * Sets the 
	 * @param oi
	 */
	
	public void setStartPointOverlay(OverlayItem oi) {
		mOverlays.set(0, oi);
		populate();
	}
	
	/**
	 * 
	 * @param oi
	 */
	
	public void setEndPointOverlay(OverlayItem oi) {
		if (mOverlays.size() == 1) {
			mOverlays.add(oi);
		}
		else {
			mOverlays.set(1, oi);
		}
		populate();
	}
	
	/**
	 * 
	 */
	
	@Override
	protected OverlayItem createItem(int i) {
		return this.mOverlays.get(i);
	}
	
	/**
	 * 
	 * @return
	 */
	
	public ArrayList<OverlayItem> getMOverLays() {
		return mOverlays;
	}
	
	/**
	 * 
	 */
	
	@Override
	public int size() {
		return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		OverlayItem item = mOverlays.get(index);
		AlertDialog.Builder  dialog = new AlertDialog.Builder(mContext);
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
			Log.i(TAG,"TAP!"); //TODO: Debug code, not needed
			if ( p!=null ) {
				
				//handleGeoPoint(p);
				if (navMode && startPoint == null) {
					startPoint = p;
					AlertDialog.Builder  dialog = new AlertDialog.Builder(mContext);
					dialog.setTitle("Navigation Mode");
					dialog.setMessage("Now tap the endPoint of your route");
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
	public boolean onTouchEvent(MotionEvent e, MapView mapView) {
		
		int fingers = e.getPointerCount();
		if( e.getAction()==MotionEvent.ACTION_DOWN ) {
			
			isPinch=false;  // Touch DOWN, don't know if it's a pinch yet
		}
		
		if ( e.getAction()==MotionEvent.ACTION_MOVE && fingers==2 ) {
			
    	isPinch=true;   // Two fingers, def a pinch
    	}
		
    return super.onTouchEvent(e,mapView);
	}
	
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
	
	private class GeoFenceTask implements Runnable {

		ArrayList<Double> distances = new ArrayList<Double>();
		boolean isInit = false;
		boolean end = false;
		Location curLoc;
		double threshold = 0.1; //distance in KM from timeline point that we want to alert the user
		
		public void run() {
			// TODO Auto-generated method stub
						
			while (!end) {
				String lProv = lMan.getBestProvider(new Criteria(), true);
				curLoc = lMan.getLastKnownLocation(lProv);
				Log.i("test",lProv);
				curLoc = lMan.getLastKnownLocation(lProv);
				if (curLoc != null) {
				Log.i("Cur Loc", curLoc.toString());
				}
				if (curLoc != null) {
					if (!isInit || distances.size() != mOverlays.size()) {
						distances.clear();
						for (OverlayItem oi: mOverlays) {
							distances.add(MainActivity.distanceKm(curLoc.getLatitude(), curLoc.getLongitude(),
								(double)(oi.getPoint().getLatitudeE6())/1000000.0, 
								(double)(oi.getPoint().getLongitudeE6())/1000000.0));
						}
					}
					else {
						for (int i = 0; i < distances.size(); i++) {
							double curDist = MainActivity.distanceKm(curLoc.getLatitude(), curLoc.getLongitude(),
								(double)(mOverlays.get(i).getPoint().getLatitudeE6())/1000000.0, 
								(double)(mOverlays.get(i).getPoint().getLongitudeE6())/1000000.0);
							if (curDist < threshold && distances.get(i) > threshold) {
								alertUser(mOverlays.get(i));
							}
							distances.set(i, (MainActivity.distanceKm(curLoc.getLatitude(), curLoc.getLongitude(),
								(double)(mOverlays.get(i).getPoint().getLatitudeE6())/1000000.0, 
								(double)(mOverlays.get(i).getPoint().getLongitudeE6())/1000000.0)));
						}
					}
				}
				try {
					Log.i("Sleep","yes");
					Thread.sleep(6000); //Wait for a minute before rechecking in order to conserve battery life
					Log.i("Sleep","no");
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
	}
	
}
