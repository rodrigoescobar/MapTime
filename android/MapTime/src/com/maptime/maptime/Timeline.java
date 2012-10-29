package com.maptime.maptime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class Timeline implements Parcelable{

	private ArrayList<TimePoint> timePoints;
	private int lineID;
	
	public Timeline(String xml) {
		
		timePoints = new ArrayList<TimePoint>();
		BufferedReader bf = new BufferedReader(new StringReader(xml));
		String line;
		try {
			line = bf.readLine();
			lineID = Integer.parseInt(line.split("'")[1]);
			//extract each timepoint, create its object and shove it in the arraylist
			while(!line.trim().startsWith("</timel")) {
				line = bf.readLine();
				int id = Integer.parseInt(line.split("'")[1]);
				String name = bf.readLine().trim().substring(6).split("</na")[0];
				String desc = bf.readLine().trim().substring(13).split("</des")[0];
				bf.readLine();//pass over sourceName tag
				bf.readLine();//pass over sourceURL tag
				bf.readLine();//pass over year tag
				bf.readLine();//pass over yearUnitID tag
				int month = Integer.parseInt(bf.readLine().trim().substring(7).split("</mo")[0]);
				int day = Integer.parseInt(bf.readLine().trim().substring(5).split("</day")[0]);
				bf.readLine();//pass over second description tag
				double time = Double.parseDouble(bf.readLine().trim().substring(10).split("</yea")[0]);;
				timePoints.add(new TimePoint(time, id, name, desc, month, day));
				bf.readLine();
			}
	
		} catch (IOException e){
			e.printStackTrace();
		}
	}

	public Timeline (Parcel source) {
		timePoints = new ArrayList<TimePoint>();
		lineID = source.readInt();
		source.readList(timePoints, TimePoint.class.getClassLoader());
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(lineID);
		dest.writeList(timePoints);
	}
}
