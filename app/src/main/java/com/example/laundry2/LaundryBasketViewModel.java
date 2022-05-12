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

import java.util.List;


public class LaundryBasketViewModel extends AndroidViewModel implements LaundryBasketContract {
    private final MutableLiveData<Order> orderMutableLiveData;
    private final MutableLiveData<Integer> basketSize;
    private final MutableLiveData<List<LaundryItem>> laundryItemList;
    private final MutableLiveData<AuthState> orderPlacementSuccessMutableLiveData;
    private final ApplicationRepository repository;
    private final LiveData<List<LaundryItemCache>> laundryItemCacheList;


    public LaundryBasketViewModel (@NonNull Application application) {
        super (application);
        repository = new ApplicationRepository (application);
        orderMutableLiveData = repository.getOrderMutableLiveData ();
        basketSize = repository.getBasketSize ();
        laundryItemList = repository.getLaundryItemList ();
        orderPlacementSuccessMutableLiveData = repository.getOrderPlacementSuccessMutableLiveData ();
        laundryItemCacheList = repository.getLaundryItemCacheLiveData ();
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
    public void createOrder(String uid, String laundryHouseUID, double deliveryCost, boolean drying){
        repository.createOrder (uid, laundryHouseUID, deliveryCost, drying);
    }

    @Override
    public void clearLaundryItemCache () {
        repository.clearLaundryItemCache ();
    }

    @Override
    public void clearBasket () {
        repository.clearBasket ();
    }

    @Override
    public void addItem(String type){
        repository.addItem (type);
    }

    @Override
    public void removeItem (LaundryItemCache laundryItemCache) {
        repository.removeItem (laundryItemCache);
    }
}
