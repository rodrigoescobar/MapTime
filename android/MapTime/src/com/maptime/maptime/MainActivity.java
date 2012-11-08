package com.maptime.maptime;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends MapActivity {
	
	private MapController mapController;
	private PointsOverlay itemizedOverlay;
	private List<Overlay> mapOverlays;
	private GeoPoint point, point2;
	private ArrayList<OverlayItem> timePoints;
	private Timeline curTimeline;
	private ProgressDialog progressDialog;
	
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(android.R.drawable.arrow_down_float);
		itemizedOverlay = new PointsOverlay(drawable, this);
		itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(0, 0), "whoops", "you shouldn't see this"));
		itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(0, 0), "whoops", "you shouldn't see this")); //debug code to avoid null pointer exceptions. fix later
		mapOverlays.add(itemizedOverlay);
    }    
    
	private void timeToPlace() {
		itemizedOverlay.clearTimePoints();
		ArrayList<GeoPoint> gp = ((NavOverlay) mapOverlays.get(1)).getNavGPs();
		double dist = ((NavOverlay) mapOverlays.get(1)).getLength();
		int timelineSize = curTimeline.size();
		double endTimeValue = curTimeline.getPoint(0).getTimeInBC();
		double timeRange = curTimeline.getPoint(timelineSize-1).getTimeInBC() - endTimeValue;
		double[] relativePos = new double[timelineSize];
		for (int i = 0; i < timelineSize; i++) {
			relativePos[i] = dist * ((timeRange - (curTimeline.getPoint((timelineSize-1)-i).getTimeInBC() - endTimeValue)) / timeRange);
		}
		int curCheck = 0;
		double lengthSoFar = 0.0;
		for(int i = 0; i < gp.size()-1; i++){
			double length = distanceKm((double)(gp.get(i).getLatitudeE6())/(double)1000000.0,
					(double)(gp.get(i).getLongitudeE6())/(double)1000000.0,
					(double)(gp.get(i+1).getLatitudeE6())/(double)1000000.0,
					(double)(gp.get(i+1).getLongitudeE6())/(double)1000000.0);
			while(curCheck < relativePos.length && relativePos[curCheck] < length + lengthSoFar) {
				double fraction = (relativePos[curCheck]-lengthSoFar) / length;
				int lat1 = gp.get(i).getLatitudeE6();
				int lon1 = gp.get(i).getLongitudeE6();
				int lat2 = gp.get(i+1).getLatitudeE6();
				int lon2 = gp.get(i+1).getLongitudeE6();
				int diffLat = lat2 - lat1;
				int diffLon = lon2 - lon1;
				double addLat = fraction * (double) diffLat;
				double addLon = fraction * (double) diffLon;
				itemizedOverlay.addOverlay(new OverlayItem(new GeoPoint(lat1+(int)addLat,lon1+(int)addLon), curTimeline.getPoint((curTimeline.size()-1)-curCheck).getName(), curTimeline.getPoint((curTimeline.size()-1)-curCheck).getDescription()));
				//get microdegrees between geopoints, multiply by fraction, then add that onto the first geopoint, and add that point to our arraylist
				curCheck++;
			}
			lengthSoFar += length;
		}
		/*work out how far down route each TimePoint should be, normalised to dist, then
		 *work out how far each geopoint is using distanceKm(), and if TimePoints should go 
		 *between the two geopoints, and if so, for each timepoint that should go, work out how far between, and...
		 *Need to confirm that YOURS does in fact use KM for length() though
		*/
	}
	
	public static double distanceKm(double lat1, double lon1, double lat2, double lon2) {
	    int EARTH_RADIUS_KM = 6371;
	    double lat1Rad = Math.toRadians(lat1);
	    double lat2Rad = Math.toRadians(lat2);
	    double deltaLonRad = Math.toRadians(lon2 - lon1);

	    return Math.acos(Math.sin(lat1Rad) * Math.sin(lat2Rad) + Math.cos(lat1Rad) * Math.cos(lat2Rad) * Math.cos(deltaLonRad)) * EARTH_RADIUS_KM;
	}
	
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 0 && resultCode == RESULT_OK) {
			curTimeline = data.getParcelableExtra("selectedTimeline");
			if (mapOverlays.size() == 2) {
				timeToPlace();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){        
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.activity_main, menu);
	    return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
	    switch(item.getItemId()){
	    case R.id.menu_timelines:
	        Intent intent = new Intent(this,Timelinechoice.class);
	        startActivityForResult(intent, 0);
	        return true;
	    case R.id.menu_startnav:
	    	AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Navigation Mode");
			dialog.setMessage("Tap where you want to start your timeline");
			dialog.show();
	    	Thread nst = new Thread(new NavStartThread());
			nst.start();
	    	//TODO: Add progress bar here (?)
			//progressDialog = ProgressDialog.show(MainActivity.this, "Loading" , "Calculating Route...");
	    	return true;
	    }
	    return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
	
	private class NavStartThread implements Runnable {
		
		public void run() {
			itemizedOverlay.clearPoints();
	    	itemizedOverlay.setNavMode(true);
			while (itemizedOverlay.getEndPoint() == null) {
			//get alerted when user has entered both points
			//pop up alert with are these two points correct? Yes/No. If no, set navMode to false, remove both points.
			//if yes, then set navmode to false and set the points to be used in the NavOverlay
			//then add the two points to the navOverlay and display the route.
			
			//TODO: Again, really needs to be some sort  of wait here, with the following code run only after we have our two points
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			itemizedOverlay.setNavMode(false);
			point = itemizedOverlay.getStartPoint();
			point2 = itemizedOverlay.getEndPoint();
			itemizedOverlay.setStartPointOverlay(new OverlayItem(point, "Start", "Start of TimeLine"));
			itemizedOverlay.setEndPointOverlay(new OverlayItem(point2, "End", "End of TimeLine"));
			//TODO: An asynctask which does the following since we can't network on main thread
			if (mapOverlays.size() == 1) {
				mapOverlays.add(new NavOverlay(point, point2));
			}
			else if (mapOverlays.get(1) instanceof NavOverlay) {
				mapOverlays.set(1, new NavOverlay(point, point2));
			}
			if(curTimeline != null) {
				timeToPlace();
			}
			//Log.i("MAP_OVERLAYS", Integer.toString(mapOverlays.size()));
		}
		
	}
}