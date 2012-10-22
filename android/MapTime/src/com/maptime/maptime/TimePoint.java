package com.maptime.maptime;

public class TimePoint {

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
			fineDate = true;
		}
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
}
