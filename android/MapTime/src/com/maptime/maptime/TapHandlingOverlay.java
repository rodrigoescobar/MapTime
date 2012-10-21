package com.maptime.maptime;

import android.util.Log;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;

class TapHandlingOverlay extends com.google.android.maps.Overlay {
	
	private boolean isPinch  =  false;
	private String TAG = "TapHandler";
	@Override
	public boolean onTap(GeoPoint p, MapView map){
		
		if ( isPinch ) {
			
			return false;
		} 
		
		else {
			
			Log.i(TAG,"TAP!");
			if ( p!=null ) {
				
				//handleGeoPoint(p);
				return true;            // We handled the tap
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

}