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
    boolean customerPickUp;
    boolean customerDrop;
    boolean laundryHousePickUp;
    boolean laundryHouseDrop;

    public Order () {
    }

    public Order (String orderId, String courierId, ArrayList<LaundryItem> items, String dateTime, String status, double deliveryCost,
                  boolean courierHasArrived, boolean customerPickUp, boolean customerDrop, boolean laundryHousePickUp, boolean laundryHouseDrop) {
        this.orderId = orderId;
        this.items = items;
        this.dateTime = dateTime;
        this.status = status;
        this.courierId = courierId;
        this.deliveryCost = deliveryCost;
        this.courierHasArrived = courierHasArrived;
        this.customerPickUp = customerPickUp;
        this.customerDrop = customerDrop;
        this.laundryHouseDrop = laundryHouseDrop;
        this.laundryHousePickUp = laundryHousePickUp;
    }

    public double getTotalCost () {
        double c = 0;
        for (LaundryItem item : this.items) {
            c += item.cost;
        }
        return new BigDecimal (c+ this.deliveryCost).setScale (2, BigDecimal.ROUND_DOWN).doubleValue () ;
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

    public boolean getCourierHasArrived () {
        return courierHasArrived;
    }

    public void setCourierHasArrived (boolean courierHasArrived) {
        this.courierHasArrived = courierHasArrived;
    }

    public boolean getCustomerPickUp () {
        return customerPickUp;
    }

    public void setCustomerPickUp (boolean customerPickUp) {
        this.customerPickUp = customerPickUp;
    }

    public boolean getCustomerDrop () {
        return customerDrop;
    }

    public void setCustomerDrop (boolean customerDrop) {
        this.customerDrop = customerDrop;
    }

    public boolean getLaundryHousePickUp () {
        return laundryHousePickUp;
    }

    public void setLaundryHousePickUp (boolean laundryHousePickUp) {
        this.laundryHousePickUp = laundryHousePickUp;
    }

    public boolean getLaundryHouseDrop () {
        return laundryHouseDrop;
    }

    public void setLaundryHouseDrop (boolean laundryHouseDrop) {
        this.laundryHouseDrop = laundryHouseDrop;
    }

}
