package com.example.laundry2.Contract;

import android.location.Location;

import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.Order;
import com.google.firebase.auth.FirebaseUser;

public interface LocationContract {

    MutableLiveData<Location> getCurrentLocationMutableLiveData ();

    MutableLiveData<FirebaseUser> getCurrentSignedInUser();

    MutableLiveData<Boolean> getServiceStateMutableLiveData ();

    MutableLiveData<Order> getOrder();

    MutableLiveData<String > getCustomerEmailMutableLiveData();

    void getCustomerOrder(String orderId);

    void getCurrentLocation ();

    void getCustomerEmail(String orderId);

    void getCourierLocation(String courierUid);

    void startLiveLocation ();

    void stopLiveLocation();

    void updateLiveLocation(String courierUid, Location location);
}
