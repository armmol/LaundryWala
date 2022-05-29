package com.example.laundry2;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.Contract.AuthenticationContract;
import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.Database.CurrentOrderCourierId;
import com.example.laundry2.Database.LaundryHouseCache;
import com.example.laundry2.Database.OrderTracking;
import com.example.laundry2.Database.Permission;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;


public class AuthenticationViewModel extends AndroidViewModel implements AuthenticationContract {

    private final ApplicationRepository repository;
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final MutableLiveData<List<LaundryHouse>> laundryHouseMutableLiveData;
    private final MutableLiveData<ApplicationUser> applicationUserMutableLiveData;
    private final MutableLiveData<GoogleSignInClient> googleSignInClientMutableLiveData;
    private final MutableLiveData<AuthState> authStateMutableLiveData;
    private final MutableLiveData<List<Order>> orderMutableLiveData;
    private final MutableLiveData<Boolean> logoutMutableLiveData;
    private final MutableLiveData<List<Courier>> courierListMutableLiveData;
    private final MutableLiveData<Boolean> courierArrivalMutableLiveData;
    private final MutableLiveData<Double> newDeliveryCostMutableLiveData;
    private final LiveData<AuthType> authTypeLiveData;
    private final LiveData<LaundryHouseCache> laundryHouseCacheLiveData;
    private final LiveData<OrderTracking> orderTrackingLiveData;
    private final LiveData<CurrentOrderCourierId> currentOrderCourierIdLiveData;
    private final LiveData<Permission> permissionLiveData;

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
        courierArrivalMutableLiveData = repository.getCourierArrivalMutableLiveData ();
        newDeliveryCostMutableLiveData = repository.getNewDeliveryCostMutableLiveData ();
        authTypeLiveData = repository.getAuthTypeLiveData ();
        laundryHouseCacheLiveData = repository.getLaundryHouseCacheLiveData ();
        orderTrackingLiveData = repository.getOrderTrackingLiveData ();
        currentOrderCourierIdLiveData = repository.getCurrentOrderCourierIdLiveData ();
        permissionLiveData = repository.getPermissionLiveData ();
    }

    @Override
    public LiveData<AuthType> getAuthType () {
        return authTypeLiveData;
    }

    @Override
    public LiveData<LaundryHouseCache> getLaundryHouseCacheData () {
        return laundryHouseCacheLiveData;
    }

    @Override
    public LiveData<OrderTracking> getOrderTracking () {
        return orderTrackingLiveData;
    }

    @Override
    public LiveData<CurrentOrderCourierId> getCurrentOrderCourierId () {
        return currentOrderCourierIdLiveData;
    }

    @Override
    public LiveData<Permission> getPermission () {
        return permissionLiveData;
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
    public MutableLiveData<Boolean> getCourierArrivalMutableLiveData () {
        return courierArrivalMutableLiveData;
    }

    @Override
    public MutableLiveData<Double> getNewDeliveryCost () {
        return newDeliveryCostMutableLiveData;
    }

    @Override
    public void laundryHousesUpdate (String uid) {
        repository.laundryHousesUpdate (uid);
    }

    @Override
    public void getNewDeliveryCost (Place place, String laundryHouseUid) {
        repository.getNewDeliveryCost (place, laundryHouseUid);
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
    public void loadAllLaundryHouses (String uid) {
        repository.loadAllLaundryHouses (uid);
    }

    @Override
    public void loadAllOrders (String authType, String uid, boolean isOrderHistory) {
        repository.loadAllOrders (authType, uid, isOrderHistory);
    }

    @Override
    public void loadAllCouriers (String orderId) {
        repository.loadAllCouriers (orderId);
    }

    @Override
    public void loadApplicationUserData (String authtype, String uid) {
        repository.getApplicationUserData (authtype, uid);
    }

    @Override
    public void signOut () {
        repository.logout ();
    }

    @Override
    public void enterIntoDB (String uid, String email, String authtype, String name, String address, String area,
                             double latitude, double longitude) {
        repository.enterDataIntoDB (uid, email, authtype, name, address, area, latitude, longitude);
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
    public void checkIsForProfileCompleted (String authtype, String uid) {
        repository.isProfileCompleted (authtype, uid);
    }

    @Override
    public void insertLaundryHouseCacheData (String laundryHouseId, String deliveryCost) {
        repository.insertLaundryHouseCacheData (laundryHouseId, deliveryCost);
    }

    @Override
    public void insertPermission (String permission) {
        repository.insertPermission (permission);
    }

    @Override
    public void deletePermission () {
        repository.deletePermission ();
    }

    @Override
    public void removeLaundryHouseCacheData () {
        repository.removerLaundryHouseCacheData ();
    }

    @Override
    public void insertIsOrderTrackingData (String isOrderTracking) {
        repository.insertOrderTracking (isOrderTracking);
    }

    @Override
    public void removeIsOrderTrackingData () {
        repository.removeOrderTracking ();
    }

    @Override
    public void insertCurrentOrderCourierId (String courierId, String orderId, String deliveryCost) {
        repository.insertCurrentOrderCourierId (courierId, orderId, deliveryCost);
    }

    @Override
    public void removeCurrentOrderCourierId () {
        repository.removeCurrentOrderCourierId ();
    }

    @Override
    public void assignOrder (String courierId, String orderId) {
        repository.assignOrder (courierId, orderId);
    }

    @Override
    public void unassignOrder (String orderId) {
        repository.unassignOrder (orderId);
    }

    @Override
    public void notifyOfArrival (String OrderId, String Uid, String title, String message) {
        repository.notifyOfArrival (OrderId, Uid, title, message);
    }

    @Override
    public void getNotified (String orderId) {
        repository.getNotified (orderId);
    }

    @Override
    public void changeOrderPickDropStatus (String orderId, String courierId, String authType, String type, boolean value) {
        repository.changeOrderPickDropStatus (orderId, courierId, authType, type, value);
    }

    @Override
    public void orderIDChange (String authType, String uid) {
        repository.orderIDChange (authType, uid);
    }
}
