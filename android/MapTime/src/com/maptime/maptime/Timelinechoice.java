package com.maptime.maptime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import android.os.AsyncTask;
import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.support.v4.app.NavUtils;


public class Timelinechoice extends Activity {

	public final static String GEOPOINTS = "com.maptime.maptime.GEOPOINTS";
	private final static String APIURL = "http://kanga-na8g09c.ecs.soton.ac.uk/api/fetchAll.php";
	private ArrayList<Timeline> timelines = new ArrayList<Timeline>();
	private int timelineChoice = -1;
	
    @SuppressLint({ "NewApi", "NewApi" }) //so it doesn't error on getActionBar()
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timelinechoice);
        if (android.os.Build.VERSION.SDK_INT >= 11) { //11 = API 11 i.e. honeycomb
        	getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        //TODO: get timeline list, populate list with timelines
        //somehow return a list of geopoints to the main activity when something is selected here
		//TODO: the intent returning needs to be done somehow before onPause() so 
        //prob at after user selects a timeline from the list. Every time user selects timeline from list
        
        //new Thread(new TimelineRetriever(this)).start();
        new TimelineRetrieverTask(this).execute();
    }

    
    private void retrieveTimelines() {
    	String total = "";
		try {
			// Create a URL for the desired page
			URL url = new URL(APIURL);

			// Read all the text returned by the server
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			in.readLine();//remove<?xml etc. line
			String curLine = in.readLine();
			while (curLine != null) {
				while (!curLine.trim().startsWith("</timeli")) {
					total = total + curLine + '\n';
					curLine = in.readLine();
				}
				total = total + curLine + '\n';
				timelines.add(new Timeline(total));
				total = "";
				curLine = in.readLine();
			}
			in.close();
		} catch (MalformedURLException e) {
		} catch (IOException e) {
		}
    	
    }
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_timelinechoice, menu);
        return true;
    }

    
	@Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        	case R.id.menu_refresh:
        		new TimelineRetrieverTask(this).execute();
        		return true;
        }
        return super.onOptionsItemSelected(item);
    }

	private class TimelineRetrieverTask extends AsyncTask<Void, Void, Void> {

		private Context context;
		
		public TimelineRetrieverTask(Context ct) {
			context = ct;
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			retrieveTimelines();
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			String[] tlIDs = new String[timelines.size()];
	        for (int i = 0; i < timelines.size(); i++) {
	        	tlIDs[i] = "Timeline "+Integer.toString(timelines.get(i).getLineID());
	        }
	        ArrayAdapter ad=new ArrayAdapter(context,android.R.layout.simple_list_item_1,tlIDs);
	        final ListView list=(ListView)findViewById(R.id.tlcList);
	        list.setAdapter(ad);
	        list.setOnItemClickListener(new OnItemClickListener() {

	        	public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
	        		// TODO Auto-generated method stub
	        		TextView txt=(TextView)findViewById(R.id.tlcTXT);
	        		txt.setText(list.getItemAtPosition(arg2).toString());
	        		timelineChoice = arg2;
	        		//the following is quick/dirty purely for making-work purposes
	        		Intent result = new Intent();
	        		result.putExtra("selectedTimeline", timelines.get(arg2));
	        		setResult(RESULT_OK, result);
	        		finish();
	        	}
	        });
		}
		
	}
	
	
	
}
