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

public class Timeline implements Parcelable{

	private ArrayList<TimePoint> timePoints;
	private int lineID;
	private int size = 0;
	
	public Timeline(String xml) {
		
		try {
			readXML(xml);
		} catch (Exception e) {}
		
		/*
		 * REDUNDANT DUE TO JAKOB'S CODE
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
		*/
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
	
	/*
	 * Read the XML file in a nice way
	 */
	private void readXML(String xmlFile) throws ParserConfigurationException, SAXException, IOException {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		timePoints = new ArrayList<TimePoint>();
	 
		DefaultHandler handler = new DefaultHandler() {
			boolean btime = false;
			boolean bname = false;
			boolean bdesc = false;
			boolean bmonth = false;
			boolean bday = false;
			
			String name;
			Double time;
			String desc;
			int month;
			int day;			
			String timelineName;
			int timepointID;
		 
			public void startElement(String uri, String localName,String qName, Attributes attributes) throws SAXException {	 
				if (qName.equalsIgnoreCase("name")) { bname = true; }	 
				if (qName.equalsIgnoreCase("description")) { bdesc = true; }	 
				if (qName.equalsIgnoreCase("month")) { bmonth = true; }	 
				if (qName.equalsIgnoreCase("day")) { bday = true; }
				if (qName.equalsIgnoreCase("yearInBC")) { btime = true; }
				
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.getQName(i).equalsIgnoreCase("timelineName")) {
						timelineName = attributes.getValue(i);
					}
					if (attributes.getQName(i).equalsIgnoreCase("timepointID")) {
						timepointID = Integer.parseInt(attributes.getValue(i));
					}
				}
			}
			
			public void endElement(String uri, String localName, String qName) throws SAXException {
					if(qName.equalsIgnoreCase("timepoint"));
					timePoints.add(new TimePoint(time, timepointID, name, desc, month, day));
					size++;
					//TODO: Add a timelineName option to get the different timeline names
			}
		 
			public void characters(char ch[], int start, int length) throws SAXException {
				if (bname) {
					name = new String(ch, start, length);
					bname = false;
				}
				if (bdesc) {
					desc = new String(ch, start, length);
					bdesc = false;
				}
				if (bmonth) {
					month = Integer.parseInt(new String(ch, start, length));
					bmonth = false;
				}
				if (bday) {
					day = Integer.parseInt(new String(ch, start, length));
					bday = false;
				}
				if (btime) {
					time = Double.parseDouble(new String(ch, start, length));
					btime = false;
				}
		 
			}
			
	    };
		saxParser.parse(xmlFile, handler);
	}
	
}
