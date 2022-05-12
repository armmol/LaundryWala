package com.example.laundry2.DataClasses;

import java.math.BigDecimal;
import java.util.ArrayList;

public class Order{

    String orderId;
    ArrayList<LaundryItem> items;
    String dateTime;
    String courierId;
    String status;
    double customerDeliveryLocationLatitude;
    double customerDeliveryLocationLongitude;
    double laundryHouseDeliveryLocationLatitude;
    double laundryHouseDeliveryLocationLongitude;
    double deliveryCost;
    boolean drying;
    boolean courierHasArrived;
    boolean customerPickUp;
    boolean customerDrop;
    boolean laundryHousePickUp;
    boolean laundryHouseDrop;

    public Order () {
    }

    public Order (String orderId, String courierId, ArrayList<LaundryItem> items, String dateTime,
                  String status, double customerDeliveryLocationLatitude, double customerDeliveryLocationLongitude,
                  double laundryHouseDeliveryLocationLatitude, double laundryHouseDeliveryLocationLongitude, double deliveryCost, boolean drying,
                  boolean courierHasArrived, boolean customerPickUp, boolean customerDrop, boolean laundryHousePickUp, boolean laundryHouseDrop) {
        this.orderId = orderId;
        this.items = items;
        this.dateTime = dateTime;
        this.status = status;
        this.courierId = courierId;
        this.deliveryCost = deliveryCost;
        this.drying = drying;
        this.courierHasArrived = courierHasArrived;
        this.customerPickUp = customerPickUp;
        this.customerDrop = customerDrop;
        this.laundryHouseDrop = laundryHouseDrop;
        this.laundryHousePickUp = laundryHousePickUp;
        this.customerDeliveryLocationLatitude = customerDeliveryLocationLatitude;
        this.customerDeliveryLocationLongitude = customerDeliveryLocationLongitude;
        this.laundryHouseDeliveryLocationLatitude = laundryHouseDeliveryLocationLatitude;
        this.laundryHouseDeliveryLocationLongitude = laundryHouseDeliveryLocationLongitude;
    }

    public double getTotalCost () {
        double c = 0;
        for (LaundryItem item : this.items) {
            c += item.cost + 0.16;
        }
        return new BigDecimal (c+ this.deliveryCost).setScale (2, BigDecimal.ROUND_DOWN).doubleValue () ;
    }

    public boolean isDrying () {
        return drying;
    }

    public double getCustomerDeliveryLocationLatitude () {
        return customerDeliveryLocationLatitude;
    }

    public double getCustomerDeliveryLocationLongitude () {
        return customerDeliveryLocationLongitude;
    }

    public double getLaundryHouseDeliveryLocationLatitude () {
        return laundryHouseDeliveryLocationLatitude;
    }

    public double getLaundryHouseDeliveryLocationLongitude () {
        return laundryHouseDeliveryLocationLongitude;
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

    public String getOrderId () {
        return orderId;
    }

    public void setOrderId (String orderId) {
        this.orderId = orderId;
    }

    public ArrayList<LaundryItem> getItems () {
        return items;
    }

    public String getDateTime () {
        return dateTime;
    }

    public double getDeliveryCost () {
        return deliveryCost;
    }

    public boolean getCustomerPickUp () {
        return customerPickUp;
    }

    public boolean getCustomerDrop () {
        return customerDrop;
    }

    public boolean getLaundryHousePickUp () {
        return laundryHousePickUp;
    }

    public boolean getLaundryHouseDrop () {
        return laundryHouseDrop;
    }

}
