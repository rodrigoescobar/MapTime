package com.maptime.maptime;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;


public class PointsOverlay extends ItemizedOverlay {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;
	private boolean isPinch  =  false;
	private String TAG = "TapHandler";
	private boolean navMode = false;
	
	public PointsOverlay(Drawable defaultMarker, Context context) {
		super(boundCenterBottom(defaultMarker));
		mContext = context;
	}
	
	public void addOverlay(OverlayItem overlay) {
		mOverlays.add(overlay);
		populate();
	}
	
	@Override
	protected OverlayItem createItem(int i) {
		return this.mOverlays.get(i);
	}
	
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
		
		if ( isPinch ) {
			
			return false;
		} 
		
		else {
			
			Log.i(TAG,"TAP!");
			if ( p!=null ) {
				
				//handleGeoPoint(p);
				return super.onTap(p, map);            // We handled the tap
			}
			
			else {
				
				return false;           // Null GeoPoint
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
	
}
