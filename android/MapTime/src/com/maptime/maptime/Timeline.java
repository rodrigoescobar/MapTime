package com.maptime.maptime;

import java.io.File;
import java.util.ArrayList;

public class Timeline {

	private ArrayList<TimePoint> timePoints;
	
	public Timeline(File xml) {
		
		while(xml.canRead()) {
			//extract each timepoint, create its object and shove it in the arraylist
		}
	}
}
