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

/**
 * Class responsible for bundling TimePoints in a List, 
 * as well as implementing it's own data from the MapTime 
 * database, and implementing List-like methods.
 */

public class Timeline implements Parcelable {

	private ArrayList<TimePoint> timePoints = new ArrayList<TimePoint>();
	private String timelineName;
	private int timelineNo;
	
	/**
	 * Normal constructor
	 * @param name Name of the Timeline in the MapTime database
	 * @param number numbr of the Timeline in the TimelineChoice List
	 */
	
	public Timeline(String name, int number) {
		timelineName = name;
		timelineNo = number;
	}
	
	/**
	 * Adds a new TimePoint to the internal List
	 * @param time timeInBC of the new TimePoint
	 * @param timepointID ID of the new TimePoint
	 * @param name Name of the new TimePoint
	 * @param desc Description of the new TimePoint
	 * @param month Month of the new TimePoint
	 * @param day Day of the new TimePoint
	 */
	
	public void addTimePoint(double time, int timepointID, String name, String desc, int month, int day) {
		timePoints.add(new TimePoint(time, timepointID, name, desc, month, day));
	}
	
	/**
	 * Getter for timelineName
	 * @return The name of the TimeLine
	 */
	
	public String getLineName() {
		return timelineName;
	}
	
	/**
	 * Getter for timelineNo
	 * @return the List position number of the TimeLine
	 */
	
	public int getLineID() {
		return timelineNo;
	}

	/**
	 * Getter for the size of the internal List.
	 * @return the size of the internal List
	 */
	
	public int size() {
		return timePoints.size();
	}
	
	/**
	 * Gets a TimePoint from the internal List
	 * @param point Index of the TimePoint to retrieve
	 * @return The TimePoint corresponding to the given index
	 */
	
	public TimePoint getPoint(int point) {
		return timePoints.get(point);
	}
	
	/**
	 * Constructor for use with a parcelled TimeLine
	 * @param source Parcel containing the TimeLine
	 */
	
	public Timeline (Parcel source) {
		timePoints = new ArrayList<TimePoint>();
		timelineNo = source.readInt();
		source.readList(timePoints, TimePoint.class.getClassLoader());
	}
	
	public int describeContents() {
		
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		
		dest.writeInt(timelineNo);
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
