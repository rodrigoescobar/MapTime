package com.maptime.maptime;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

import android.os.Parcel;
import android.os.Parcelable;

public class ParcelableOverlayItem extends OverlayItem implements Parcelable{

	public ParcelableOverlayItem(GeoPoint pos, String name, String desc) {
		super(pos, name, desc);
		// TODO Auto-generated constructor stub
	}

	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		// TODO Auto-generated method stub
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
