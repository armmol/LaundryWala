package com.example.laundry2;

import android.app.Application;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.LocationContract;
import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Repositories.ApplicationRepository;
import com.google.firebase.auth.FirebaseUser;

public class LocationViewModel extends AndroidViewModel implements LocationContract {
    private final ApplicationRepository locationRepository;
    private final MutableLiveData<Location> currentLocationMutableLiveData;
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final MutableLiveData<Boolean> serviceStateMutableLiveData;
    private final MutableLiveData<ApplicationUser> applicationUserMutableLiveData;
    private final MutableLiveData<Order> orderMutableLiveData;

    public LocationViewModel (@NonNull Application application) {
        super (application);
        locationRepository = new ApplicationRepository (application);
        currentLocationMutableLiveData = locationRepository.getCurrentLocationMutableLiveData ();
        userMutableLiveData = locationRepository.getUserMutableLiveData ();
        serviceStateMutableLiveData = locationRepository.getServiceState ();
        applicationUserMutableLiveData = locationRepository.getApplicationUserMutableLiveData ();
        orderMutableLiveData = locationRepository.getOrderMutableLiveData ();
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
        locationRepository.getLocation ();
    }

    @Override
    public void getCourierLocation (String courierUid) {
        locationRepository.getCourierLocation (courierUid);
    }

    @Override
    public void startLiveLocation () {
        locationRepository.startLocationService ();
    }

    @Override
    public void stopLiveLocation () {
        locationRepository.stopLocationService ();
    }

    @Override
    public void updateLiveLocation (String courierUid, Location location) {
        locationRepository.updateCourierLocation (courierUid, location);
    }

    @Override
    public MutableLiveData<Boolean> getServiceStateMutableLiveData () {
        return serviceStateMutableLiveData;
    }

    @Override
    public MutableLiveData<ApplicationUser> getApplicationUserData () {
        return applicationUserMutableLiveData;
    }

    @Override
    public MutableLiveData<Order> getOrder () {
        return orderMutableLiveData;
    }

    @Override
    public void loadApplicationUserData (String authtype) {
        locationRepository.getApplicationUserData (authtype);
    }

    @Override
    public void getCustomerOrder (String orderId) {
        locationRepository.getOrder (orderId);
    }
}
