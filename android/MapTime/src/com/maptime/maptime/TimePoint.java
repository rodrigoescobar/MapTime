package com.maptime.maptime;

import android.os.Parcel;
import android.os.Parcelable;

public class TimePoint implements Parcelable, Comparable<TimePoint>{

	private double timeInBC;
	private int id;
	private String name;
	private String description;
	private int month;
	private int day;
	private boolean fineDate = true;
	
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
	
	public String getDescription() {
		return description;
	}
	public int getId() {
		return id;
	}
	public String getName() {
		return name;
	}
	public double getTimeInBC() {
		return timeInBC;
	}
	public int getMonth() {
		return month;
	}
	public int getDay() {
		return day;
	}
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

	/*
	 * Note: this class has a natural ordering that is inconsistent with equals.(non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 * Also, greatest timeInBC is considered the 'smallest' element
	 */
	public int compareTo(TimePoint arg0) { //comparable on timeInBC
		// TODO Auto-generated method stub
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
