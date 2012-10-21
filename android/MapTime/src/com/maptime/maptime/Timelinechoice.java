package com.maptime.maptime;

import java.util.ArrayList;

import com.google.android.maps.GeoPoint;

import android.os.Bundle;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

public class Timelinechoice extends Activity {

	public final static String GEOPOINTS = "com.maptime.maptime.GEOPOINTS";
	
    @SuppressLint({ "NewApi", "NewApi" })
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
        }
        return super.onOptionsItemSelected(item);
    }

}
