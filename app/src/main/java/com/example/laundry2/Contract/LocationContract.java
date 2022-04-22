package com.example.laundry2.Contract;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.Order;
import com.google.firebase.auth.FirebaseUser;

public interface LocationContract {

    MutableLiveData<Location> getCurrentLocationMutableLiveData ();

    MutableLiveData<FirebaseUser> getCurrentSignedInUser();

    MutableLiveData<Boolean> getServiceStateMutableLiveData ();

    MutableLiveData<ApplicationUser> getApplicationUserData();

    MutableLiveData<Order> getOrder();

    void loadApplicationUserData (String authtype);

    void getCustomerOrder(String orderId);

    void getCurrentLocation ();

    void getCourierLocation(String courierUid);

    void startLiveLocation ();

    void stopLiveLocation();

    void updateLiveLocation(String courierUid, Location location);
}
