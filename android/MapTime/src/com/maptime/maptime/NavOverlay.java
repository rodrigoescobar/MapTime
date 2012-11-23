package com.maptime.maptime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public class NavOverlay extends Overlay {

    private ArrayList<GeoPoint> navGPs;
    private ArrayList<Point> navPoints = new ArrayList<Point>();;
    private double length;
    private boolean isFullyCreated = false;

    /**
     * 
     * @param gp1 Start of Route
     * @param gp2 End of Route
     */
    
    public NavOverlay(GeoPoint gp1, GeoPoint gp2) {
        navGPs = new ArrayList<GeoPoint>();
        navGPs.add(gp1);
        try {
        	URL url = makeURL(gp1,gp2);
        	URLConnection urlC = url.openConnection();
        	urlC.addRequestProperty("X-Yours-client", "MapTime");
        	BufferedReader in = new BufferedReader(new InputStreamReader(urlC.getInputStream()));
			String str;
			String[] gpCouple;
			while ((str = in.readLine()) != null) {
				// str is one line of text; readLine() strips the newline character(s)
				if (str.trim().startsWith("<dista")) {
					str = str.trim().substring(10);
					String[] strTemp = str.split("<");
					length = Double.parseDouble(strTemp[0]);
				}
				else if (str.trim().startsWith("<coordi")) { //marks start of navigation coordinates
					gpCouple = str.trim().substring(14).split(","); //trims the <coordinate> tag, splits on the , between lat and long
					navGPs.add(new GeoPoint((int)(Double.valueOf(gpCouple[1])*(double)1000000.0), //list is in long,lat and degrees
							(int)(Double.valueOf(gpCouple[0])*(double)1000000.0)));				  //GeoPoint wants lat,long and microdegrees
					str = in.readLine();
					while (!str.trim().startsWith("</")) { //second while loop takes us to end of <coordinates>
						gpCouple = str.trim().split(",");
						navGPs.add(new GeoPoint((int)(Double.valueOf(gpCouple[1])*(double)1000000.0),
								(int)(Double.valueOf(gpCouple[0])*(double)1000000.0)));
						str = in.readLine();
					}
				}
			}
			in.close();
        } catch (MalformedURLException e) {
        } catch (IOException e) {
		}
        navGPs.add(gp2);
        isFullyCreated = true;
    }

    public NavOverlay(ArrayList<ParcelableGeoPoint> gps, double routeLength) {
    	ArrayList<GeoPoint> geopoints = new ArrayList<GeoPoint>();
    	for (ParcelableGeoPoint pgp : gps) {
    		geopoints.add(new GeoPoint(pgp.getLatitudeE6(), pgp.getLongitudeE6()));
    	}
    	navGPs = geopoints;
    	length = routeLength;
    	isFullyCreated = true;
    }
    
    private URL makeURL (GeoPoint g1, GeoPoint g2) throws MalformedURLException { //using YOURS example server for navigation for now
    	
    	return new URL("http://www.yournavigation.org/api/1.0/gosmore.php?format=kml&flat="+
    			(double)((double)g1.getLatitudeE6()/1000000.0)+ //flat/flon are start latitude and longitude
    			"&flon="+(double)((double)g1.getLongitudeE6()/1000000.0)+
    			"&tlat="+(double)((double)g2.getLatitudeE6()/1000000.0)+ //tlat/tlon are dest longi/lati
    			"&tlon="+(double)((double)g2.getLongitudeE6()/1000000.0)+
    			"&v=motorcar&fast=1&layer=mapnik"); //rest of URL is static, at least until this service goes down
    } //http://wiki.openstreetmap.org/wiki/YOURS
    
    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
            long when) {
        // TODO Auto-generated method stub
        Projection projection = mapView.getProjection();
        if (shadow == false) {

        	navPoints.clear();
            Paint paint = new Paint();
            paint.setAntiAlias(true);
            //Point point = new Point();
            //projection.toPixels(gp1, point);
            paint.setColor(Color.BLUE);
            //Point point2 = new Point();
            //projection.toPixels(gp2, point2);
            paint.setStrokeWidth(2);
            for (int i = 0; i < navGPs.size(); i++) {
            	navPoints.add(new Point());
            	projection.toPixels(navGPs.get(i), navPoints.get(i));
            }
            /*canvas.drawLine((float) point.x, (float) point.y, (float) point2.x,
                    (float) point2.y, paint);*/
            for (int i = 0; i < navPoints.size()-1; i++) {
            	canvas.drawLine((float) navPoints.get(i).x, (float) navPoints.get(i).y, (float) navPoints.get(i+1).x,
                        (float) navPoints.get(i+1).y, paint);
            }
        }
        return super.draw(canvas, mapView, shadow, when);
    }

    public double getLength() {
		return length;
	}
    
    public boolean isCreated() {
    	return isFullyCreated;
    }
    
    public ArrayList<GeoPoint> getNavGPs() {
		return navGPs;
	}
    
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        // TODO Auto-generated method stub

        super.draw(canvas, mapView, shadow);
    }

}