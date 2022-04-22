package com.example.laundry2;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.AuthenticationContract;
import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Repositories.ApplicationRepository;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class AuthenticationViewModel extends AndroidViewModel implements AuthenticationContract {

    private final ApplicationRepository repository;
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final MutableLiveData<List<LatLng>> userLatLngMutableLiveData;
    private final MutableLiveData<List<LaundryHouse>> laundryHouseMutableLiveData;
    private final MutableLiveData<ApplicationUser> applicationUserMutableLiveData;
    private final MutableLiveData<GoogleSignInClient> googleSignInClientMutableLiveData;
    private final MutableLiveData<AuthState> authStateMutableLiveData;
    private final MutableLiveData<List<Order>> orderMutableLiveData;
    private final MutableLiveData<Boolean> logoutMutableLiveData;
    private final MutableLiveData<List<Courier>> courierListMutableLiveData;

    public AuthenticationViewModel (Application application) {
        super (application);
        repository = new ApplicationRepository (application);
        userMutableLiveData = repository.getUserMutableLiveData ();
        laundryHouseMutableLiveData = repository.getLaundryHouseMutableLiveData ();
        applicationUserMutableLiveData = repository.getApplicationUserMutableLiveData ();
        googleSignInClientMutableLiveData = repository.getGoogleSignInClientMutableLiveData ();
        authStateMutableLiveData = repository.getAuthStateMutableLiveData ();
        orderMutableLiveData = repository.getOrderListMutableLiveData ();
        logoutMutableLiveData = repository.getLogoutMutableLiveData ();
        courierListMutableLiveData = repository.getCourierListMutableLiveData ();
        userLatLngMutableLiveData = repository.getUserLatLngListMutableLiveData ();
    }

    @Override
    public MutableLiveData<FirebaseUser> getCurrentSignInUser () {
        return userMutableLiveData;
    }

    @Override
    public MutableLiveData<List<LaundryHouse>> getLaundryHouses () {
        return laundryHouseMutableLiveData;
    }

    @Override
    public MutableLiveData<List<Courier>> getCouriers () {
        return courierListMutableLiveData;
    }

    @Override
    public MutableLiveData<AuthState> getState () {
        return authStateMutableLiveData;
    }

    @Override
    public MutableLiveData<ApplicationUser> getApplicationUserData () {
        return applicationUserMutableLiveData;
    }

    @Override
    public MutableLiveData<GoogleSignInClient> getGoogleSignInClient () {
        return googleSignInClientMutableLiveData;
    }

    @Override
    public MutableLiveData<List<Order>> getOrders () {
        return orderMutableLiveData;
    }

    @Override
    public MutableLiveData<Boolean> getLogoutMutableLiveData () {
        return logoutMutableLiveData;
    }

    @Override
    public MutableLiveData<List<LatLng>> getLatLngMutableLiveData () {
        return userLatLngMutableLiveData;
    }

    @Override
    public void loginEmail (String email, String password, String authtype) {
        repository.loginEmail (email, password, authtype);
    }

    @Override
    public void signupEmail (String email, String password, String confirmpassword, String authtype) {
        repository.registerWithEmail (email, password, confirmpassword, authtype);
    }

    @Override
    public void signinGoogle (String authtype, String idToken, int requestcode) {
        repository.firebaseAuthWithGoogle (authtype, idToken, requestcode);
    }

    @Override
    public void forgotPassword (String email) {
        repository.forgotPassword (email);
    }

    @Override
    public void loadAllLaundryHouses () {
        repository.loadAllLaundryHouses ();
    }

    @Override
    public void loadAllOrders () {
        repository.loadAllOrders ();
    }

    @Override
    public void loadAllCouriers () {
        repository.loadAllCouriers ();
    }

    @Override
    public void getUserAndLaundryHouseMarkerLocation (String OrderUid) {
        repository.getUserLatLng (OrderUid);
    }

    @Override
    public void loadApplicationUserData (String authtype) {
        repository.getApplicationUserData (authtype);
    }

    @Override
    public void signOut () {
        repository.logout ();
    }

    @Override
    public void enterIntoDB (String authtype, String name, String address, String area,
                             double latitude, double longitude, String upiid, String payseraid) {
        repository.enterDataIntoDB (authtype, name, address, area, latitude, longitude, upiid, payseraid);
    }

    @Override
    public void updateOrderStatus (String status, String orderId) {
        repository.updateOrderStatus (status, orderId);
    }

    @Override
    public void changeActiveStatus (boolean isActive, String authtye, String Uid) {
        repository.changeActiveStatus (isActive, authtye, Uid);
    }

    @Override
    public void assignOrder (String courierId, String orderId) {
        repository.assignOrder (courierId, orderId);
    }

    @Override
    public void usassignOrder (String courierId, String orderId) {
        repository.unassignOrder (courierId, orderId);
    }


}
