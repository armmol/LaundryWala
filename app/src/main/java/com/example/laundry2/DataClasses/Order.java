package com.example.laundry2.DataClasses;

import java.util.ArrayList;

public class Order {

    String orderId;
    ArrayList<LaundryItem> items;
    String dateTime;
    String courierId;
    String status;

    public Order(){}


    public Order (String orderId, String courierId, ArrayList<LaundryItem> items, String dateTime, String status) {
        this.orderId = orderId;
        this.items = items;
        this.dateTime = dateTime;
        this.status = status;
        this.courierId = courierId;
    }

    public double getTotalCost(){
        double c =0;
        for (LaundryItem item: this.items) {
            c+=item.cost;
        }
        return c;
    }

    public String getStatus () {
        return status;
    }

    public void setStatus (String status) {
        this.status = status;
    }

    public void setOrderId (String orderId) {
        this.orderId = orderId;
    }

    public String getCourierId () {
        return courierId;
    }

    public void setCourierId (String courierId) {
        this.courierId = courierId;
    }

    public String getOrderId () {
        return orderId;
    }

    public ArrayList<LaundryItem> getItems () {
        return items;
    }

    public void setItems (ArrayList<LaundryItem>  items) {
        this.items = items;
    }

    public String getDateTime () {
        return dateTime;
    }

    public void setDateTime (String dateTime) {
        this.dateTime = dateTime;
    }
}
