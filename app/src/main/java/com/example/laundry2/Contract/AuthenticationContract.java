package com.example.laundry2.Contract;

import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.Order;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public interface AuthenticationContract {

    MutableLiveData<FirebaseUser> getCurrentSignInUser ();

    MutableLiveData<List<LaundryHouse>> getLaundryHouses ();

    MutableLiveData<List<Courier>> getCouriers ();

    MutableLiveData<AuthState> getState ();

    MutableLiveData<ApplicationUser> getApplicationUserData ();

    MutableLiveData<GoogleSignInClient> getGoogleSignInClient ();

    MutableLiveData<List<Order>> getOrders ();

    MutableLiveData<Boolean> getLogoutMutableLiveData ();

    MutableLiveData<List<LatLng>> getLatLngMutableLiveData();

    MutableLiveData<Boolean> getCourierArrivalMutableLiveData();

    void loginEmail (String email, String password, String authtype);

    void signupEmail (String email, String password, String confirmpassword, String authtype);

    void signinGoogle (String authtype, String idToken, int requestcode);

    void forgotPassword (String email);

    void signOut ();

    void loadAllLaundryHouses ();

    void loadAllOrders (String authtype);

    void loadAllCouriers ();

    void getUserAndLaundryHouseMarkerLocation (String OrderUid);

    void loadApplicationUserData (String authtype);

    void enterIntoDB (String authtype, String name, String address, String area,
                      double latitude, double longitude);

    void updateOrderStatus (String status, String orderId);

    void changeActiveStatus(boolean isActive, String authtye, String Uid);

    void assignOrder(String courierId, String orderId);

    void usassignOrder(String courierId, String orderId);

    void notifyOfArrival(String OrderId, boolean value);

    void getNotified(String orderId);
}

