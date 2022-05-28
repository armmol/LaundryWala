package com.example.laundry2.DataClasses;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class Order implements Comparable<Order> {

    String orderId;
    ArrayList<LaundryItem> items;
    String customerEmail;
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

    public Order (String orderId, String courierId, String customerEmail, ArrayList<LaundryItem> items, String dateTime,
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
        this.customerEmail = customerEmail;
    }

    public double getTotalCost () {
        double c = 0;
        for (LaundryItem item : this.items) {
            if (this.drying)
                c += item.cost + 0.16;
            else
                c += item.cost;
        }
        return new BigDecimal (c + this.deliveryCost).setScale (2, BigDecimal.ROUND_DOWN).doubleValue ();
    }

    public String getCustomerEmail () {
        return customerEmail;
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

    @Override
    public int compareTo (Order order) {
        String[] a = order.dateTime.split (" ");
        String[] b = dateTime.split (" ");
        SimpleDateFormat formatter = new SimpleDateFormat ("dd-MMM-yyyy hh:mm:ss", Locale.ENGLISH);
        String aDate = a[2] + "-" + a[1] + "-" + a[5]+ " " + a[3];
        String bDate = b[2] + "-" + b[1] + "-" + b[5]+ " " + b[3];
        try {
            return formatter.parse (aDate).compareTo (formatter.parse (bDate));
        } catch (ParseException e) {
            e.printStackTrace ();
        }
        return Integer.compare (Integer.parseInt (order.dateTime.split (" ")[2]), Integer.parseInt (dateTime.split (" ")[2]));
    }
}
