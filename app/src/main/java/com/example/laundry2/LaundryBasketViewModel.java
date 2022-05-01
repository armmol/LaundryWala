package com.example.laundry2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.LaundryBasketContract;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.Repositories.ApplicationRepository;

import java.util.List;


public class LaundryBasketViewModel extends AndroidViewModel implements LaundryBasketContract {
    private final MutableLiveData<Order> orderMutableLiveData;
    private final MutableLiveData<Integer> basketSize;
    private final MutableLiveData<List<LaundryItem>> laundryItemList;
    private final MutableLiveData<AuthState> orderPlacementSuccessMutableLiveData;
    private final ApplicationRepository laundryBasketRepository;
    private final LiveData<List<LaundryItemCache>> laundryItemCacheList;


    public LaundryBasketViewModel (@NonNull Application application) {
        super (application);
        laundryBasketRepository = new ApplicationRepository (application);
        orderMutableLiveData = laundryBasketRepository.getOrderMutableLiveData ();
        basketSize = laundryBasketRepository.getBasketSize ();
        laundryItemList = laundryBasketRepository.getLaundryItemList ();
        orderPlacementSuccessMutableLiveData = laundryBasketRepository.getOrderPlacementSuccessMutableLiveData ();
        laundryItemCacheList = laundryBasketRepository.getLaundryItemCaches ();
    }

    @Override
    public MutableLiveData<Order> getOrderMutableLiveData(){
        return orderMutableLiveData;
    }

    @Override
    public MutableLiveData<Integer> getBasketSize () {
        return basketSize;
    }

    @Override
    public MutableLiveData<AuthState> orderPlacementStatus () {
        return orderPlacementSuccessMutableLiveData;
    }

    @Override
    public MutableLiveData<List<LaundryItem>> getLaundryItems () {
        return laundryItemList;
    }

    @Override
    public LiveData<List<LaundryItemCache>> getCachedItems () {
        return laundryItemCacheList;
    }

    @Override
    public void createOrder(String laundryHouseUID, double deliveryCost){
        laundryBasketRepository.createOrder (laundryHouseUID, deliveryCost);
    }

    @Override
    public void addItem(String type){
        laundryBasketRepository.addItem (type);
    }

    @Override
    public void removeItem (String type) {
        laundryBasketRepository.removeItem (type);
    }
}
