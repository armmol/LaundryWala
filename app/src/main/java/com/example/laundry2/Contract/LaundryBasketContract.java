package com.example.laundry2.Contract;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;

import java.util.List;

public interface LaundryBasketContract {

    MutableLiveData<Order> getOrderMutableLiveData ();

    MutableLiveData<Integer> getBasketSize ();

    MutableLiveData<Boolean> orderPlacementStatus();

    MutableLiveData<List<LaundryItem>> getLaundryItems();

    LiveData<List<LaundryItemCache>> getCachedItems();

    void createOrder (String laundryHouseUID, double deliveryCost);

    void addItem (String type);

    void removeItem(String type);
}
