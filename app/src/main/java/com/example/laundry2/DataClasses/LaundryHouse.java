package com.example.laundry2.DataClasses;

import com.google.android.gms.maps.model.LatLng;

public class LaundryHouse implements Comparable<LaundryHouse> {
    String name;
    LatLng address;
    String area;
    String upiId;
    String payseraId;
    String uid;
    double deliveryprice;
    boolean active;


    public LaundryHouse (String name, LatLng address, String area, String upiId, String payseraId, String uid, boolean active) {
        this.name = name;
        this.address = address;
        this.area = area;
        this.upiId = upiId;
        this.payseraId = payseraId;
        this.uid = uid;
        this.active = active;
    }

    public void setDeliveryprice (double deliveryprice) {
        this.deliveryprice = deliveryprice;
    }

    public double getDeliveryprice () {
        return deliveryprice;
    }

    public String getUid () {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public String getUpiId () {
        return upiId;
    }

    public void setUpiId (String upiId) {
        this.upiId = upiId;
    }

    public String getPayseraId () {
        return payseraId;
    }

    public void setPayseraId (String payseraId) {
        this.payseraId = payseraId;
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

    public void setArea (String deliveryPrice) {
        this.area = deliveryPrice;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    @Override
    public int compareTo (LaundryHouse laundryHouse) {
        return Double.compare (deliveryprice, laundryHouse.deliveryprice);
    }
}
