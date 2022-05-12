package com.example.laundry2.DataClasses;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class Courier implements Comparable<Courier>{
    String name;
    String uid;
    LatLng location;
    ArrayList<String> OrderId;
    double distanceFromCustomer;
    double distanceFromLaundryHouse;
    boolean active;

    public Courier (String name, String uid, LatLng location, ArrayList<String> orderId, double distanceFromCustomer, double distanceFromLaundryHouse, boolean active) {
        this.name = name;
        this.uid = uid;
        this.location = location;
        this.OrderId = orderId;
        this.distanceFromCustomer = distanceFromCustomer;
        this.distanceFromLaundryHouse = distanceFromLaundryHouse;
        this.active = active;
    }

    public double getDistanceFromCustomer () {
        return distanceFromCustomer;
    }

    public double getDistanceFromLaundryHouse () {
        return distanceFromLaundryHouse;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getUid () {
        return uid;
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

    @Override
    public int compareTo (Courier courier) {
        return Integer.compare (OrderId.size (), this.OrderId.size ());
    }
}
