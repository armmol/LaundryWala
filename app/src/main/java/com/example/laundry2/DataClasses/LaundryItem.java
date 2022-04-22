package com.example.laundry2.DataClasses;

import android.os.Parcel;
import android.os.Parcelable;

public class LaundryItem implements Parcelable {

    String type;
    double cost;
    public LaundryItem () { }


    public LaundryItem (String type, double cost) {
        this.type = type;
        this.cost = cost;
    }

    protected LaundryItem (Parcel in) {
        type = in.readString ();
        cost = in.readDouble ();
    }

    public static final Creator<LaundryItem> CREATOR = new Creator<LaundryItem> () {
        @Override
        public LaundryItem createFromParcel (Parcel in) {
            return new LaundryItem (in);
        }

        @Override
        public LaundryItem[] newArray (int size) {
            return new LaundryItem[size];
        }
    };

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public double getCost () {
        return cost;
    }

    public void setCost (double cost) {
        this.cost = cost;
    }

    @Override
    public int describeContents () {
        return 0;
    }

    @Override
    public void writeToParcel (Parcel parcel, int i) {
        parcel.writeString (type);
        parcel.writeDouble (cost);
    }
}
