package com.example.laundry2;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.LocationContract;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Order;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class LocationViewModel extends AndroidViewModel implements LocationContract {
    private final ApplicationRepository repository;
    private final MutableLiveData<Location> currentLocationMutableLiveData;
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final MutableLiveData<List<LatLng>> userLatLngMutableLiveData;
    private final MutableLiveData<Boolean> serviceStateMutableLiveData;
    private final MutableLiveData<Order> orderMutableLiveData;
    private final MutableLiveData<String> customerEmail;
    private final MutableLiveData<AuthState> authStateMutableLiveData;

    public LocationViewModel (@NonNull Application application) {
        super (application);
        repository = new ApplicationRepository (application);
        currentLocationMutableLiveData = repository.getCurrentLocationMutableLiveData ();
        userMutableLiveData = repository.getUserMutableLiveData ();
        serviceStateMutableLiveData = repository.getServiceState ();
        orderMutableLiveData = repository.getOrderMutableLiveData ();
        userLatLngMutableLiveData = repository.getUserLatLngListMutableLiveData ();
        customerEmail = repository.getCustomerEmail ();
        authStateMutableLiveData = repository.getAuthStateMutableLiveData ();
    }

    @Override
    public MutableLiveData<Location> getCurrentLocationMutableLiveData () {
        return currentLocationMutableLiveData;
    }

    @Override
    public MutableLiveData<FirebaseUser> getCurrentSignedInUser () {
        return userMutableLiveData;
    }

    @Override
    public void getCurrentLocation () {
        repository.getLocation ();
    }

    @Override
    public void getCustomerEmail (String orderId) {
        repository.getCustomerEmail (orderId);
    }

    @Override
    public void getCourierLocation (String courierUid) {
        repository.getCourierLocation (courierUid);
    }

    @Override
    public void startLiveLocation () {
        repository.startLocationService ();
    }

    @Override
    public void stopLiveLocation () {
        repository.stopLocationService ();
    }

    @Override
    public void updateLiveLocation (String courierUid, Location location) {
        repository.updateCourierLocation (courierUid, location);
    }

    @Override
    public void getUserAndLaundryHouseMarkerLocation (String OrderUid) {
        repository.getUserAndLaundryHouseLatLng (OrderUid);
    }

    @Override
    public MutableLiveData<List<LatLng>> getLatLngMutableLiveData () {
        return userLatLngMutableLiveData;
    }

    @Override
    public MutableLiveData<Boolean> getServiceStateMutableLiveData () {
        return serviceStateMutableLiveData;
    }

    @Override
    public MutableLiveData<Order> getOrder () {
        return orderMutableLiveData;
    }

    @Override
    public MutableLiveData<String> getCustomerEmailMutableLiveData () {
        return customerEmail;
    }

    @Override
    public void getCustomerOrder (String orderId) {
        repository.getOrder (orderId);
    }

    @Override
    public void orderChange (String courierId, String orderId) {
        repository.orderChange (courierId, orderId);
    }

    //Required for Testing
    public MutableLiveData<AuthState> getAuthStateMutableLiveData () {
        return authStateMutableLiveData;
    }
}
