package com.maptime.maptime;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

public class ParcelableGeoPoint extends GeoPoint implements Parcelable{

	public ParcelableGeoPoint(int lat, int lon) {
		super(lat, lon);
		// TODO Auto-generated constructor stub
	}

	public ParcelableGeoPoint(GeoPoint gp) {
		super(gp.getLatitudeE6(), gp.getLongitudeE6());
	}
	
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
		dest.writeInt(this.getLatitudeE6());
		dest.writeInt(this.getLongitudeE6());
	}

	public static final Parcelable.Creator<ParcelableGeoPoint> CREATOR = 
		new Parcelable.Creator<ParcelableGeoPoint>() {
			public ParcelableGeoPoint createFromParcel(Parcel in) {
				return new ParcelableGeoPoint(in.readInt(), in.readInt());
			}

			public ParcelableGeoPoint[] newArray(int size) {
				return new ParcelableGeoPoint[size];
			}
		};
	
}
