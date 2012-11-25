package com.maptime.maptime;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * A Parcelable implementation of Android's OverlayItem class.
 *
 */

public class ParcelableOverlayItem extends OverlayItem implements Parcelable{

	/**
	 * Standard constructor, identical to OverlayItem's
	 * @param pos GeoPoint describing the location to place the OverlaayItem
	 * @param name Title of the OverlayItem
	 * @param desc Snippet of the OverlayItem
	 */
	
	public ParcelableOverlayItem(GeoPoint pos, String name, String desc) {
		super(pos, name, desc);
		
	}

	/**
	 * A constructor creating a ParcelableOverlayItem equivalent of an OverlayItem more directly.
	 * @param oi OverlayItem to create an equivalent of.
	 */
	
	public ParcelableOverlayItem(OverlayItem oi) {
		super(new ParcelableGeoPoint(oi.getPoint()), oi.getTitle(), oi.getSnippet());
	}
	
	public int describeContents() {

		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {

		dest.writeString(this.getTitle());
		dest.writeString(this.getSnippet());
		dest.writeParcelable(new ParcelableGeoPoint(this.getPoint()), 0);
	}

	public static final Parcelable.Creator<ParcelableOverlayItem> CREATOR = 
		new Parcelable.Creator<ParcelableOverlayItem>() {
			public ParcelableOverlayItem createFromParcel(Parcel in) {
				String name = in.readString();
				String desc = in.readString();
				ParcelableGeoPoint pos = in.readParcelable(ParcelableGeoPoint.class.getClassLoader());
				return new ParcelableOverlayItem(pos, name, desc);
			}

			public ParcelableOverlayItem[] newArray(int size) {
				return new ParcelableOverlayItem[size];
			}
		};
	
}
