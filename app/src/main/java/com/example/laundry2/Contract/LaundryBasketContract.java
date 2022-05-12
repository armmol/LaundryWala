package com.example.laundry2.Contract;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;

import java.util.List;

public interface LaundryBasketContract {

    MutableLiveData<Order> getOrderMutableLiveData ();

    MutableLiveData<Integer> getBasketSize ();

    MutableLiveData<AuthState> orderPlacementStatus();

    MutableLiveData<List<LaundryItem>> getLaundryItems();

    LiveData<List<LaundryItemCache>> getCachedItems();

    void createOrder (String uid, String laundryHouseUID, double deliveryCost,boolean drying);

    void clearLaundryItemCache ();

    void clearBasket();

    void addItem (String type);

    void removeItem(LaundryItemCache laundryItemCache);
}
