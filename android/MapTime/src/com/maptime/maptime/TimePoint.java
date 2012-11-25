package com.maptime.maptime;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A class represent an individual point of time that is described
 * in the MapTime database.
 */

public class TimePoint implements Parcelable, Comparable<TimePoint>{
	
	private double timeInBC; //Time in BC of the event, i.e. number of years before 0AD
	private int id; //ID of the event in the MapTime database
	private String name; //Name of the event
	private String description; //Description of the event
	private int month; //Month the event took place
	private int day; //Day the event took place
	private boolean fineDate = true;  //Whether month and day are available or not
	
	/**
	 * Standard constructor
	 * @param timeSet Time in BC of the event
	 * @param idSet ID number of the event in the MapTime database
	 * @param nameSet Name of the Event
	 * @param descSet Description of the event
	 * @param monthSet Month the event took place
	 * @param daySet Day the event took place
	 */
	
	public TimePoint(double timeSet, int idSet, String nameSet, String descSet, int monthSet, int daySet) {
		
		timeInBC = timeSet;
		id = idSet;
		name = nameSet;
		description = descSet;
		month = monthSet;
		day = daySet;
		if (month > 12 || day < 1) {
			fineDate = false;
		}
	}
	
	/**
	 * Constructor for use with a parcelled TimePoint
	 * @param source Parcel containing the TimePoint
	 */
	
	public TimePoint(Parcel source) {
		timeInBC = source.readDouble();
		id = source.readInt();
		name = source.readString();
		description = source.readString();
		month = source.readInt();
		day = source.readInt();
		boolean[] temp = new boolean[1];
		source.readBooleanArray(temp);
		fineDate = temp[0];
	}
	
	/**
	 * Getter for description
	 * @return Description of the event
	 */
	
	public String getDescription() {
		return description;
	}
	
	/**
	 * Getter for ID
	 * @return ID of the event in the MapTime database
	 */
	
	public int getId() {
		return id;
	}
	
	/**
	 * Getter for name
	 * @return Name of the event
	 */
	
	public String getName() {
		return name;
	}
	
	/**
	 * Getter for timeInBC
	 * @return Time of the event in years before 0AD
	 */
	
	public double getTimeInBC() {
		return timeInBC;
	}
	
	/**
	 * Getter for month
	 * @return Month the event took place
	 */
	
	public int getMonth() {
		return month;
	}
	
	/**
	 * Getter for day
	 * @return Day the event took place
	 */
	
	public int getDay() {
		return day;
	}
	
	/**
	 * Getter for fineDate
	 * @return true if the month and day are available, false otherwise
	 */
	
	public boolean usesFineDate() {
		return fineDate;
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeDouble(timeInBC);
		dest.writeInt(id);
		dest.writeString(name);
		dest.writeString(description);
		dest.writeInt(month);
		dest.writeInt(day);
		dest.writeBooleanArray(new boolean[] {fineDate});
	}
	
	public static final Parcelable.Creator<TimePoint> CREATOR = 
	new Parcelable.Creator<TimePoint>() {
		public TimePoint createFromParcel(Parcel in) {
			return new TimePoint(in);
		}

		public TimePoint[] newArray(int size) {
			return new TimePoint[size];
		}
	};

	/**
	 * Note: this class has a natural ordering that is inconsistent with equals.(non-Javadoc)
	 * Also, greatest timeInBC is considered the 'smallest' element
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(TimePoint arg0) { //comparable on timeInBC
		
		if (this.getTimeInBC() < arg0.getTimeInBC()) {
			return -1;
		}
		else if(this.getTimeInBC() > arg0.getTimeInBC()) {
			return 1;
		}
		else {
			return 0;
		}
	}
	
}
