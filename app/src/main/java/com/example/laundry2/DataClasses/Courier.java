package com.example.laundry2.DataClasses;

import com.google.android.gms.maps.model.LatLng;

public class Courier {
    String name;
    String uid;
    LatLng location;
    String OrderId;
    boolean active;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getUid () {
        return uid;
    }

    public void setUid (String uid) {
        this.uid = uid;
    }

    public LatLng getLocation () {
        return location;
    }

    public void setLocation (LatLng location) {
        this.location = location;
    }

    public String getOrderId () {
        return OrderId;
    }

    public void setOrderId (String orderId) {
        OrderId = orderId;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public Courier (String name, String uid, LatLng location, String orderId, boolean active) {
        this.name = name;
        this.uid = uid;
        this.location = location;
        this.OrderId = orderId;
        this.active = active;
    }
}
