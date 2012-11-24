package com.maptime.maptime;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.android.maps.GeoPoint;

/**
 * A Parcelable implementation of Android's GeoPoint class.
 *
 */

public class ParcelableGeoPoint extends GeoPoint implements Parcelable{

	/**
	 * Standard constructor, identical to GeoPoint's
	 * @param lat Latitude in microdegrees
	 * @param lon Longitude in microdegrees
	 */
	
	public ParcelableGeoPoint(int lat, int lon) {
		super(lat, lon);
		
	}

	/**
	 * A constructor creating a ParcelableGeoPoint equivalent of a GeoPoint more directly.
	 * @param gp GeoPoint to create an equivalent of
	 */
	
	public ParcelableGeoPoint(GeoPoint gp) {
		super(gp.getLatitudeE6(), gp.getLongitudeE6());
	}
	
	public int describeContents() {
		
		return 0;
	}
	
	public void writeToParcel(Parcel dest, int flags) {
		
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
