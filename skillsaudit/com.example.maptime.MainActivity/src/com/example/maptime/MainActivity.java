package com.example.maptime;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

import android.os.Bundle;
import android.os.StrictMode;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends MapActivity {
	
	MapController mapController;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy); 
        
        
        setContentView(R.layout.activity_main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.androidmarker);
        HelloItemizedOverlay itemizedOverlay = new HelloItemizedOverlay(drawable, this);
        
        GeoPoint point = new GeoPoint(50935017, -1396294);
        GeoPoint point2 = new GeoPoint(51501135, -0115356);
        OverlayItem overlayItem = new OverlayItem(point, "Highfield", "Highfield Campus");
        OverlayItem overlayItem2 = new OverlayItem(point2, "London", "London City!!!1");
        itemizedOverlay.addOverlay(overlayItem);
        itemizedOverlay.addOverlay(overlayItem2);
        mapOverlays.add(itemizedOverlay);
        
        mapOverlays.add(new DirectionalPathOverlay(point, point2));
        
    }    
    

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
}
