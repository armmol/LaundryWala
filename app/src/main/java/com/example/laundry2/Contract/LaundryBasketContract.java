package com.example.laundry2.Contract;

import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;

import java.util.List;

public interface LaundryBasketContract {

    MutableLiveData<Order> getOrderMutableLiveData ();

    MutableLiveData<Integer> getBasketSize ();

    MutableLiveData<Boolean> orderPlacementStatus();

    MutableLiveData<List<LaundryItem>> getLaundryItems();

    void createOrder (String laundryHouseUID, double deliveryCost);

    void addItem (int number);

    void removeItem(String type);
}
