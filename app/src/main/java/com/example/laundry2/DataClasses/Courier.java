package com.example.laundry2.DataClasses;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Courier {
    String name;
    String uid;
    LatLng location;
    ArrayList<String> OrderId;
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

    public LatLng getLocation () {
        return location;
    }

    public void setLocation (LatLng location) {
        this.location = location;
    }

    public ArrayList<String> getOrderId () {
        return OrderId;
    }

    public void setOrderId (ArrayList<String> orderId) {
        OrderId = orderId;
    }

    public boolean isActive () {
        return active;
    }

    public Courier (String name, String uid, LatLng location, ArrayList<String> orderId, boolean active) {
        this.name = name;
        this.uid = uid;
        this.location = location;
        this.OrderId = orderId;
        this.active = active;
    }
}
