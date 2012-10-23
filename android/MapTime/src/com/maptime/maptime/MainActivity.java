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
import android.os.StrictMode;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

public class MainActivity extends MapActivity {
	
	MapController mapController;
	private PointsOverlay itemizedOverlay;
	GeoPoint point, point2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
		
        setContentView(R.layout.activity_main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(android.R.drawable.arrow_down_float);
        itemizedOverlay = new PointsOverlay(drawable, this);
        
        //TODO: Make these set by input
        point = new GeoPoint(50935017, -1396294);
		point2 = new GeoPoint(51501135, -0115356);
		OverlayItem overlayItem = new OverlayItem(point, "Highfield", "Highfield Campus");
		OverlayItem overlayItem2 = new OverlayItem(point2, "London", "London City!!!1");
        //TODO: These will be done after user input
        itemizedOverlay.addOverlay(overlayItem);
		itemizedOverlay.addOverlay(overlayItem2);
		mapOverlays.add(itemizedOverlay);
		//TODO: An asynctask which does the following since we can't network on main thread
		//mapOverlays.add(new NavOverlay(point, point2));
        
    }    
    
	@Override
	protected void onActivityResult (int requestCode, int resultCode, Intent data) {
		
		super.onActivityResult(requestCode, resultCode, data);
		if (data != null) { //if data was returned
			
			System.out.println(data.getIntArrayExtra(Timelinechoice.GEOPOINTS)[3]);
		}
		//else{System.out.println("No data");}
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
	    	itemizedOverlay.clearPoints();
	    	itemizedOverlay.setNavMode(true);
	    	AlertDialog.Builder  dialog = new AlertDialog.Builder(this);
			dialog.setTitle("Navigation Mode");
			dialog.setMessage("Tap where you want to start your timeline");
			dialog.show();
			//get alerted when user has entered both points
			//pop up alert with are these two points correct? Yes/No. If no, set navMode to false, remove both points.
			//if yes, then set navmode to false and set the points to be used in the NavOverlay
			//then add the two points to the navOverlay and display the route.
			
			//TODO: Again, really needs to be some sort  of wait here, with the following code run only after we have our two points
			
			itemizedOverlay.setNavMode(false);
			point = itemizedOverlay.getStartpoint();
			point2 = itemizedOverlay.getEndpoint();
	    	return true;
	    }
	    return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}