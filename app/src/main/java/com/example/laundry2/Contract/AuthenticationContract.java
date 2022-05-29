package com.example.laundry2.Contract;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

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

public interface AuthenticationContract {

    LiveData<AuthType> getAuthType();

    LiveData<LaundryHouseCache> getLaundryHouseCacheData();

    LiveData<OrderTracking> getOrderTracking();

    LiveData<CurrentOrderCourierId> getCurrentOrderCourierId();

    LiveData<Permission> getPermission();

    MutableLiveData<FirebaseUser> getCurrentSignInUser ();

    MutableLiveData<List<LaundryHouse>> getLaundryHouses ();

    MutableLiveData<List<Courier>> getCouriers ();

    MutableLiveData<AuthState> getState ();

    MutableLiveData<ApplicationUser> getApplicationUserData ();

    MutableLiveData<GoogleSignInClient> getGoogleSignInClient ();

    MutableLiveData<List<Order>> getOrders ();

    MutableLiveData<Boolean> getLogoutMutableLiveData ();

    MutableLiveData<Boolean> getCourierArrivalMutableLiveData();

    MutableLiveData<Double> getNewDeliveryCost();

    void laundryHousesUpdate (String uid);

    void getNewDeliveryCost (Place place, String laundryHouseUid);

    void loginEmail (String email, String password, String authtype);

    void signupEmail (String email, String password, String confirmpassword, String authtype);

    void signinGoogle (String authtype, String idToken, int requestcode);

    void forgotPassword (String email);

    void signOut ();

    void loadAllLaundryHouses (String uid);

    void loadAllOrders (String authtype, String uid, boolean isOrderHistory);

    void loadAllCouriers (String orderId);

    void loadApplicationUserData (String authtype, String uid);

    void enterIntoDB (String uid, String email, String authtype, String name, String address, String area,
                      double latitude, double longitude);

    void updateOrderStatus ( String status, String orderId);

    void changeActiveStatus(boolean isActive, String authtype, String Uid);

    void checkIsForProfileCompleted(String authtype, String uid);

    void insertLaundryHouseCacheData(String laundryHouseId, String deliveryCost);

    void insertPermission(String permission);

    void deletePermission();

    void removeLaundryHouseCacheData();

    void insertIsOrderTrackingData(String isOrderTracking);

    void removeIsOrderTrackingData();

    void insertCurrentOrderCourierId (String courierId, String orderId, String deliveryCost);

    void removeCurrentOrderCourierId ();

    void assignOrder(String courierId, String orderId);

    void unassignOrder (String orderId);

    void notifyOfArrival(String OrderId, String Uid, String title, String Message);

    void getNotified(String orderId);

    void changeOrderPickDropStatus (String orderId, String courierId, String authType, String type, boolean value);

    void orderIDChange (String authType, String uid);
}
//

