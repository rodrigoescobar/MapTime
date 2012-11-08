package com.maptime.maptime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class Timeline implements Parcelable {

	private ArrayList<TimePoint> timePoints = new ArrayList<TimePoint>();
	private int lineID;
	private String timelineName;
	private int timelineNo;
	
	public Timeline(String name, int number) {
		timelineName = name;
		timelineNo = number;
	}
	
	public void addTimePoint(double time, int timepointID, String name, String desc, int month, int day) {
		timePoints.add(new TimePoint(time, timepointID, name, desc, month, day));
	}
	
	public String getLineName() {
		return timelineName;
	}
	
	public int getLineID() {
		return lineID;
	}

	public int size() {
		return timePoints.size();
	}
	
	public TimePoint getPoint(int point) {
		return timePoints.get(point);
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
