package com.maptime.maptime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Timeline implements Parcelable{

	private ArrayList<TimePoint> timePoints;
	private int lineID;
	private int size = 0;
	
	public Timeline(String xml) {
		
		timePoints = new ArrayList<TimePoint>();
		BufferedReader bf = new BufferedReader(new StringReader(xml));
		String line;
		try {
			line = bf.readLine();
			lineID = Integer.parseInt(line.split("\'")[1]);
			line = bf.readLine();
			//extract each timepoint, create its object and shove it in the arraylist
			while(!line.trim().startsWith("</timel")) {
				int id = Integer.parseInt(line.split("\'")[1]);
				String name = bf.readLine().trim().substring(6).split("</na")[0];
				String desc = bf.readLine().trim().substring(13).split("</des")[0];
				bf.readLine();//pass over sourceName tag
				bf.readLine();//pass over sourceURL tag
				bf.readLine();//pass over year tag
				bf.readLine();//pass over yearUnitID tag
				int month = Integer.parseInt(bf.readLine().trim().substring(7).split("</mo")[0]);
				int day = Integer.parseInt(bf.readLine().trim().substring(5).split("</day")[0]);
				bf.readLine();//pass over second description tag (it's a duplicate)
				double time = Double.parseDouble(bf.readLine().trim().substring(10).split("</yea")[0]);;
				timePoints.add(new TimePoint(time, id, name, desc, month, day));
				size++;
				bf.readLine(); //skip over </timepoint>
				line = bf.readLine();
			}
	
		} catch (IOException e){
			e.printStackTrace();
		}
	}
	
	public int getLineID() {
		return lineID;
	}

	public int size() {
		return size;
	}
	
	public TimePoint getPoint(int point) {
		return timePoints.get(point);
	}
	
	public Timeline (Parcel source) {
		timePoints = new ArrayList<TimePoint>();
		lineID = source.readInt();
		size = source.readInt();
		source.readList(timePoints, TimePoint.class.getClassLoader());
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(lineID);
		dest.writeInt(size);
		dest.writeList(timePoints);
	}
	
	public static final Parcelable.Creator<Timeline> CREATOR = 
	new Parcelable.Creator<Timeline>() {
		public Timeline createFromParcel(Parcel in) {
			return new Timeline(in);
		}

		public Timeline[] newArray(int size) {
			return new Timeline[size];
		}
	};
	
}
