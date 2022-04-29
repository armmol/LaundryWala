package com.example.laundry2;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.LaundryBasketContract;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.Repositories.ApplicationRepository;

import java.util.List;


public class LaundryBasketViewModel extends AndroidViewModel implements LaundryBasketContract {
    private final MutableLiveData<Order> orderMutableLiveData;
    private final MutableLiveData<Integer> basketsize;
    private final MutableLiveData<List<LaundryItem>> laundryitemlist;
    private final MutableLiveData<Boolean> orderPlacementSuccessMutableLiveData;
    private final ApplicationRepository laundryBasketRepository;
    private final LiveData<List<LaundryItemCache>> laundryitemcachelist;


    public LaundryBasketViewModel (@NonNull Application application) {
        super (application);
        laundryBasketRepository = new ApplicationRepository (application);
        orderMutableLiveData = laundryBasketRepository.getOrderMutableLiveData ();
        basketsize = laundryBasketRepository.getBasketSize ();
        laundryitemlist = laundryBasketRepository.getLaundryItemList ();
        orderPlacementSuccessMutableLiveData = laundryBasketRepository.getOrderPlacementSuccessMutableLiveData ();
        laundryitemcachelist = laundryBasketRepository.getLaundryItemCaches ();
    }

    @Override
    public MutableLiveData<Order> getOrderMutableLiveData(){
        return orderMutableLiveData;
    }

    @Override
    public MutableLiveData<Integer> getBasketSize () {
        return basketsize;
    }

    @Override
    public MutableLiveData<Boolean> orderPlacementStatus () {
        return orderPlacementSuccessMutableLiveData;
    }

    @Override
    public MutableLiveData<List<LaundryItem>> getLaundryItems () {
        return laundryitemlist;
    }

    @Override
    public LiveData<List<LaundryItemCache>> getCachedItems () {
        return laundryitemcachelist;
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
