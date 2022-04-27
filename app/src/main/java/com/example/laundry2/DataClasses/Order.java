package com.example.laundry2.DataClasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Order {

    String orderId;
    ArrayList<LaundryItem> items;
    String dateTime;
    String courierId;
    String status;
    double deliveryCost;
    boolean courierHasArrived;

    public Order () {
    }


    public Order (String orderId, String courierId, ArrayList<LaundryItem> items, String dateTime, String status, double deliveryCost, boolean courierHasArrived) {
        this.orderId = orderId;
        this.items = items;
        this.dateTime = dateTime;
        this.status = status;
        this.courierId = courierId;
        this. deliveryCost = deliveryCost;
        this.courierHasArrived = courierHasArrived;
    }

    public double getTotalCost () {
        double c = 0;
        for (LaundryItem item : this.items) {
            c += item.cost;
        }
        return new BigDecimal (c).setScale (2, BigDecimal.ROUND_DOWN).doubleValue ();
    }

    public String getStatus () {
        return status;
    }

    public void setStatus (String status) {
        this.status = status;
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

    public void setOrderId (String orderId) {
        this.orderId = orderId;
    }

    public ArrayList<LaundryItem> getItems () {
        return items;
    }

    public void setItems (ArrayList<LaundryItem> items) {
        this.items = items;
    }

    public String getDateTime () {
        return dateTime;
    }

    public void setDateTime (String dateTime) {
        this.dateTime = dateTime;
    }


    public double getDeliveryCost () {
        return deliveryCost;
    }

    public void setDeliveryCost (double deliveryCost) {
        this.deliveryCost = deliveryCost;
    }

    public boolean isCourierHasArrived () {
        return courierHasArrived;
    }

    public void setCourierHasArrived (boolean courierHasArrived) {
        this.courierHasArrived = courierHasArrived;
    }
}
