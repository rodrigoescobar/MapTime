package com.maptime.maptime;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

/**
 * Home screen menu which can retrieve TimeLines, and launch the MainActivity
 * either with reverse geocoded data to start with a navigation route, 
 * or with no data so that the user can use the map for that.
 */

public class Home extends Activity {
	
	private final static String APIURL = "http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php"; //URL of the MapTime database API
	private ArrayList<Timeline> timelines = new ArrayList<Timeline>();//The local list of timelines to be displayed
	public LocationManager lMan; //Local location manager for managing the state of location services
	public LocationUpdater locUp; //Location listener for receiving location updates
	
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        
       
		
        refreshTimelines(null);
    }
    
    /**
     * Places the user's current location in the Start of Route box
     * @param view 
     */
    
    public void getLocationStart(View view) {
    	Location loc = getCurrentLocation();
    	if(loc != null) {
    		EditText eStartPoint = (EditText)findViewById(R.id.txtBoxStartPoint);
    		String sLoc = loc.getLatitude() + ", " + loc.getLongitude();
    		eStartPoint.setText(sLoc);
    	} else {
    		String errorTitle = getResources().getString(R.string.error_title);
			String errorMessage = getResources().getString(R.string.error_currentGPS);
			
    		AlertDialog errDialog = new AlertDialog.Builder(Home.this).create();
			errDialog.setTitle(errorTitle);
			errDialog.setMessage(errorMessage);
			errDialog.show();
    	}
    }
    
    /**
     * Places the user's current location in the End of Route box
     * @param view
     */
    
    public void getLocationEnd(View view) {
    	Location loc = getCurrentLocation();
    	if(loc != null) {
    		EditText eStartPoint = (EditText)findViewById(R.id.txtBoxEndPoint);
    		String sLoc = loc.getLatitude() + ", " + loc.getLongitude();
    		eStartPoint.setText(sLoc);
    	} else {
    		String errorTitle = getResources().getString(R.string.error_title);
			String errorMessage = getResources().getString(R.string.error_currentGPS);
			
    		AlertDialog errDialog = new AlertDialog.Builder(Home.this).create();
			errDialog.setTitle(errorTitle);
			errDialog.setMessage(errorMessage);
			errDialog.show();
    	}
    }
    
    /**
     * 
     * @return
     */
    
    private Location getCurrentLocation() {
    	lMan = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
 		locUp = new LocationUpdater();
 		lMan.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, (float) 50.0, locUp);
 		
 		Location lastLoc = lMan.getLastKnownLocation(LocationManager.GPS_PROVIDER);
 		if(lastLoc != null) {
 			return lastLoc;
 		} else {                
 			return null;
 		}
    }
    
    /**
     * 
     */
    
    @Override
    protected void onStop() {
    	// TODO Auto-generated method stub
    	super.onStop();
    	if (lMan != null) {
    		lMan.removeUpdates(locUp);
    		locUp = null;
    		lMan = null;
    	}
    }
    
    /**
     * 
     * @param view
     */
    
    public void refreshTimelines(View view) {
    	new TimelineRetrieverTask(this).execute();
    }
    
    /**
     * 
     * @param view
     */
    
    public void plotRoute(View view) {
    	EditText eStartPoint = (EditText)findViewById(R.id.txtBoxStartPoint);
    	String sStartPoint = eStartPoint.getText().toString();
    	EditText eEndPoint = (EditText)findViewById(R.id.txtBoxEndPoint);
    	String sEndPoint = eEndPoint.getText().toString();
    	Spinner spinnerTimeline = (Spinner)findViewById(R.id.timelines_dropdown);
    	Timeline selectedTimeline = null; 
    	
    	if(!sStartPoint.equals("") && !sEndPoint.equals("")) {
    		selectedTimeline = timelines.get(spinnerTimeline.getSelectedItemPosition());
    		String[] addresses = new String[2];
    		addresses[0] = sStartPoint;
    		addresses[1] = sEndPoint;
    		Intent intent = new Intent(Home.this, MainActivity.class);
    		intent.putExtra("EXTRA_ADDRESSES", addresses);
    		intent.putExtra("EXTRA_TIMELINE", selectedTimeline);
            Home.this.startActivity(intent);
    	} 
    	else if (spinnerTimeline.getAdapter().getCount() == 0) {
    		String errorTitle = getResources().getString(R.string.error_timelines);
			String errorMessage = getResources().getString(R.string.error_refresh);
			
    		AlertDialog errDialog = new AlertDialog.Builder(Home.this).create();
			errDialog.setTitle(errorTitle);
			errDialog.setMessage(errorMessage);
			errDialog.show();
    	} else {
    		String errorTitle = getResources().getString(R.string.error_title);
			String errorMessage = getResources().getString(R.string.error_points);
			
    		AlertDialog errDialog = new AlertDialog.Builder(Home.this).create();
			errDialog.setTitle(errorTitle);
			errDialog.setMessage(errorMessage);
			errDialog.show();
    	}
    }
    
    /**
     * 
     * @param view
     */
    
    public void gotoMap(View view) {
    	//Start the map screen
        Intent intent = new Intent(Home.this, MainActivity.class);
        Home.this.startActivity(intent);
    }
    
    /**
     * 
     */
    
    public void getTimelines() {
    	try {
			readXML();
		} catch (Exception e) {
			handler .sendEmptyMessage(0);
		}
    }

	/**
	 * Handles the caught error for fetching XML (can't put dialogs inside the actual catch(){} method without app breaking)
	 */
	private Handler handler = new Handler() {
	    public void handleMessage(Message message) {
	    	String errorTitle = getResources().getString(R.string.error_title);
			String errorMessage = getResources().getString(R.string.error_readXML);
			
			AlertDialog errDialog = new AlertDialog.Builder(Home.this).create();
			errDialog.setTitle(errorTitle);
			errDialog.setMessage(errorMessage);
			errDialog.show();
	    }
	};
    
    /**
	 * Read the timelines XML file in a fast way, using a javax parser.
	 */
	private void readXML() throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		timelines.clear(); //Reset timelines to stop duplication
		
		DefaultHandler xmlHandler = new DefaultHandler() {
			int noOfTimelines = 0;
			boolean btime = false;
			boolean bname = false;
			boolean bdesc = false;
			boolean bmonth = false;
			boolean bday = false;
			
			String name;
			Double time;
			String desc;
			int month;
			int day;			
			int timepointID;
		 
			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {
				if (qName.equalsIgnoreCase("name")) { bname = true; }	 
				if (qName.equalsIgnoreCase("description")) { bdesc = true; }	 
				if (qName.equalsIgnoreCase("month")) { bmonth = true; }	 
				if (qName.equalsIgnoreCase("day")) { bday = true; }
				if (qName.equalsIgnoreCase("yearInBC")) { btime = true; }
				
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equalsIgnoreCase("timelineName")) {
						timelines.add(noOfTimelines, new Timeline(attributes.getValue(i), noOfTimelines));
						noOfTimelines++;
					}
					if (attributes.getQName(i).equalsIgnoreCase("timepointID")) {
						timepointID = Integer.parseInt(attributes.getValue(i));
					}
				}
			}
			
			public void endElement(String uri, String localName, String qName) throws SAXException {
					if(qName.equalsIgnoreCase("timepoint")) {
						timelines.get(noOfTimelines - 1).addTimePoint(time, timepointID, name, desc, month, day);
					}
			}
		 
			public void characters(char ch[], int start, int length) throws SAXException {
				if (bname) {
					name = new String(ch, start, length);
					bname = false;
				}
				if (bdesc) {
					desc = new String(ch, start, length);
					bdesc = false;
				}
				if (bmonth) {
					month = Integer.parseInt(new String(ch, start, length));
					bmonth = false;
				}
				if (bday) {
					day = Integer.parseInt(new String(ch, start, length));
					bday = false;
				}
				if (btime) {
					time = Double.parseDouble(new String(ch, start, length));
					btime = false;
				}
		 
			}
			
	    };
		saxParser.parse(APIURL, xmlHandler);
	}
	
	/**
	 * 
	 *
	 */
	
	private class TimelineRetrieverTask extends AsyncTask<Void, Void, Void> {

		private Context context;
		private ProgressDialog progressDialog;
		
		public TimelineRetrieverTask(Context ct) {
			context = ct;
		}
		
		protected void onPreExecute() {
			String progressTitle = getString(R.string.progress_loading);
			String progressMessage = getString(R.string.progress_fetchingTimlines);
			progressDialog = ProgressDialog.show(context, progressTitle , progressMessage);
		}
		
		protected Void doInBackground(Void... params) {
			getTimelines();
			return null;
		}
		
		protected void onPostExecute(Void result) {
			String [] timelineArray = new String[timelines.size()];
			int i = 0;
			Iterator<Timeline> itrTimeline = timelines.iterator();
			while(itrTimeline.hasNext()) {
				Timeline currentTimeline = itrTimeline.next();
				timelineArray[i] = currentTimeline.getLineName();
				i++;
			}

			
			Spinner spinner = (Spinner)findViewById(R.id.timelines_dropdown);
	        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, timelineArray);
		    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		    spinner.setAdapter(spinnerAdapter);
		    
		    progressDialog.dismiss();
		}
	}
	
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_home, menu);
        return true;
    }
}
