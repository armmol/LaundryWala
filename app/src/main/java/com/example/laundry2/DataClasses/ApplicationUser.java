package com.example.laundry2.DataClasses;

import java.util.ArrayList;

public class ApplicationUser {
    double latitude;
    double longitude;
    String email;
    String name;
    String authType;
    String address;
    String area;
    int orders;
    boolean active;
    ArrayList<String> orderID;


    public ApplicationUser (String address, String area, String authType, String email, double latitude,
                            double longitude, String name, int orders, boolean active, ArrayList<String> orderID) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
        this.area = area;
        this.authType = authType;
        this.orders = orders;
        this.active = active;
        this.orderID = orderID;
    }

    public int getOrders () {
        return orders;
    }

    public void setOrders (int orders) {
        this.orders = orders;
    }

    public String getEmail () {
        return email;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getAddress () {
        return address;
    }

    public void setAddress (String address) {
        this.address = address;
    }

    public String getArea () {
        return area;
    }

    public String getAuthType () {
        return authType;
    }

    public void setAuthType (String authType) {
        this.authType = authType;
    }

    public boolean isActive () {
        return active;
    }

    public ArrayList<String> getOrderId () {
        return orderID;
    }

    public void setOrderId (ArrayList<String> orderID) {
        this.orderID = orderID;
    }

    public ApplicationUser () { }
}
