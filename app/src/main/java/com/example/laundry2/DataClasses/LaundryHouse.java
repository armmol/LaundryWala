package com.example.laundry2.DataClasses;

import com.google.android.gms.maps.model.LatLng;

public class LaundryHouse implements Comparable<LaundryHouse> {
    String name;
    LatLng address;
    String area;
    String uid;
    double deliveryPrice;
    boolean active;


    public LaundryHouse (String name, LatLng address, String area, String uid, boolean active) {
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
