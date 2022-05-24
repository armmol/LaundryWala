package com.example.laundry2.DataClasses;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.google.android.gms.maps.model.LatLng;

@Entity
public class LaundryHouse implements Comparable<LaundryHouse> {
    @PrimaryKey
    @NonNull
    String uid;
    String name;
    LatLng address;
    String area;
    double deliveryPrice;
    boolean active;


    public LaundryHouse (String name, LatLng address, String area, @NonNull String uid, boolean active) {
        this.name = name;
        this.address = address;
        this.area = area;
        this.uid = uid;
        this.active = active;
    }

    public void setDeliveryPrice (double deliveryPrice) {
        this.deliveryPrice = deliveryPrice;
    }

    public double getDeliveryPrice () {
        return deliveryPrice;
    }

    public String getUid () {
        return uid;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public LatLng getAddress () {
        return address;
    }

    public void setAddress (LatLng address) {
        this.address = address;
    }

    public String getArea () {
        return area;
    }

    public boolean isActive () {
        return active;
    }

    @Override
    public int compareTo (LaundryHouse laundryHouse) {
        return Double.compare (deliveryPrice, laundryHouse.deliveryPrice);
    }
}
