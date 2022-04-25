package com.example.laundry2.DataClasses;

public class ApplicationUser {
    double latitude;
    double longitude;
    String email;
    String name;
    String authype;
    String address;
    String area;
    int orders;
    boolean active;
    String orderID;


    public ApplicationUser (String address, String area, String authype, String email, double latitude,
                            double longitude, String name, int orders, boolean active, String orderID) {
        this.email = email;
        this.latitude = latitude;
        this.longitude = longitude;
        this.name = name;
        this.address = address;
        this.area = area;
        this.authype = authype;
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

    public double getLatitude () {
        return latitude;
    }

    public void setLatitude (double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude () {
        return longitude;
    }

    public void setLongitude (double logitude) {
        this.longitude = logitude;
    }

    public String getEmail () {
        return email;
    }

    public void setEmail (String email) {
        this.email = email;
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

    public void setArea (String area) {
        this.area = area;
    }

    public String getAuthype () {
        return authype;
    }

    public void setAuthype (String authype) {
        this.authype = authype;
    }

    public boolean isActive () {
        return active;
    }

    public void setActive (boolean active) {
        this.active = active;
    }

    public String getOrderId () {
        return orderID;
    }

    public void setOrderId (String orderID) {
        this.orderID = orderID;
    }

    public ApplicationUser () { }
}
