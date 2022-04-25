package com.example.laundry2.Repositories;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.PaymentUtil.PaymentsUtil;
import com.example.laundry2.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wallet.IsReadyToPayRequest;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.gms.wallet.PaymentDataRequest;
import com.google.android.gms.wallet.PaymentsClient;
import com.google.android.libraries.places.api.Places;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class ApplicationRepository {

    private static final String TAG = "Application Repository";
    private final FirebaseAuth mAuth;
    private final FirebaseFirestore firebaseFirestore;
    private final Application application;
    private final LocationRequest mLocationRequest;
    private final LocationCallback mLocationCallback;
    private final PaymentsClient paymentsClient;
    // LiveData with the result of whether the user can pay using Google Pay
    private final MutableLiveData<Boolean> _canUseGooglePay;
    private final MutableLiveData<Task<PaymentData>> paymentDataTaskMutableLiveData;
    private final MutableLiveData<Location> currentLocationMutableLiveData;
    private final MutableLiveData<List<LatLng>> userLatLngListMutableLiveData;
    private final MutableLiveData<Boolean> serviceState;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final MutableLiveData<FirebaseUser> userMutableLiveData;
    private final MutableLiveData<AuthState> authStateMutableLiveData;
    private final MutableLiveData<List<LaundryHouse>> laundryHouseMutableLiveData;
    private final MutableLiveData<ApplicationUser> applicationUserMutableLiveData;
    private final MutableLiveData<GoogleSignInClient> googleSignInClientMutableLiveData;
    private final MutableLiveData<List<Order>> orderListMutableLiveData;
    private final MutableLiveData<Boolean> logoutMutableLiveData;
    private final MutableLiveData<Order> orderMutableLiveData;
    private final MutableLiveData<Integer> basketsize;
    private final MutableLiveData<List<LaundryItem>> laundryitemlistMutableLiveData;
    private final MutableLiveData<List<Courier>> courierListMutableLiveData;
    private final MutableLiveData<Boolean> orderPlacementSuccessMutableLiveData;
    private final ArrayList<LaundryItem> laundryItems;


    public MutableLiveData<Boolean> get_canUseGooglePay(){
        return _canUseGooglePay;
    }

    public MutableLiveData<Task<PaymentData>> getpaymentDataTaskMutableLiveData(){
        return paymentDataTaskMutableLiveData;
    }
    public MutableLiveData<Location> getCurrentLocationMutableLiveData () {
        return currentLocationMutableLiveData;
    }

    public MutableLiveData<Order> getOrderMutableLiveData () {
        return orderMutableLiveData;
    }

    public MutableLiveData<Integer> getBasketSize () {
        return basketsize;
    }

    public MutableLiveData<List<LaundryItem>> getLaundryItemList () {
        return laundryitemlistMutableLiveData;
    }

    public MutableLiveData<Boolean> getLogoutMutableLiveData () {
        return logoutMutableLiveData;
    }

    public MutableLiveData<List<Order>> getOrderListMutableLiveData () {
        return orderListMutableLiveData;
    }

    public MutableLiveData<AuthState> getAuthStateMutableLiveData () {
        return authStateMutableLiveData;
    }

    public MutableLiveData<List<LaundryHouse>> getLaundryHouseMutableLiveData () {
        return laundryHouseMutableLiveData;
    }

    public MutableLiveData<GoogleSignInClient> getGoogleSignInClientMutableLiveData () {
        return googleSignInClientMutableLiveData;
    }

    public MutableLiveData<FirebaseUser> getUserMutableLiveData () {
        return userMutableLiveData;
    }

    public MutableLiveData<ApplicationUser> getApplicationUserMutableLiveData () {
        return applicationUserMutableLiveData;
    }

    public MutableLiveData<List<Courier>> getCourierListMutableLiveData () {
        return courierListMutableLiveData;
    }

    public MutableLiveData<Boolean> getServiceState () {
        return serviceState;
    }

    public MutableLiveData<Boolean> getOrderPlacementSuccessMutableLiveData () {
        return orderPlacementSuccessMutableLiveData;
    }

    public MutableLiveData<List<LatLng>> getUserLatLngListMutableLiveData () {
        return userLatLngListMutableLiveData;
    }

    @SuppressLint("MissingPermission")
    public ApplicationRepository (Application application) {
        this.application = application;
        mAuth = FirebaseAuth.getInstance ();
        firebaseFirestore = FirebaseFirestore.getInstance ();
        paymentsClient = PaymentsUtil.createPaymentsClient (application);
        _canUseGooglePay = new MutableLiveData<> ();
        paymentDataTaskMutableLiveData = new MutableLiveData<> ();
        currentLocationMutableLiveData = new MutableLiveData<> ();
        serviceState = new MutableLiveData<> ();
        userMutableLiveData = new MutableLiveData<> ();
        laundryHouseMutableLiveData = new MutableLiveData<> ();
        applicationUserMutableLiveData = new MutableLiveData<> ();
        googleSignInClientMutableLiveData = new MutableLiveData<> ();
        authStateMutableLiveData = new MutableLiveData<> ();
        orderListMutableLiveData = new MutableLiveData<> ();
        logoutMutableLiveData = new MutableLiveData<> ();
        orderMutableLiveData = new MutableLiveData<> ();
        laundryitemlistMutableLiveData = new MutableLiveData<> ();
        basketsize = new MutableLiveData<> ();
        laundryItems = new ArrayList<> ();
        courierListMutableLiveData = new MutableLiveData<> ();
        orderPlacementSuccessMutableLiveData = new MutableLiveData<> ();
        userLatLngListMutableLiveData = new MutableLiveData<> ();

        if (mAuth.getCurrentUser () != null) {
            userMutableLiveData.postValue (mAuth.getCurrentUser ());
            logoutMutableLiveData.postValue (false);
        }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (application.getString (R.string.WebClientIDForGoogleSignIn))
                .requestEmail ()
                .build ();
        googleSignInClientMutableLiveData.postValue (GoogleSignIn.getClient (application.getBaseContext (), gso));
        Places.initialize (application, application.getString (R.string.Api_Key));

        serviceState.setValue (false);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient (application.getBaseContext ());
        fusedLocationProviderClient.getLastLocation ().addOnSuccessListener (currentLocationMutableLiveData::postValue);
        mLocationRequest = LocationRequest.create ()
                .setPriority (LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval (4000)
                .setFastestInterval (2000);
        mLocationCallback = new LocationCallback () {
            @Override
            public void onLocationResult (@NonNull LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations ())
                        currentLocationMutableLiveData.postValue (location);
                }
            }

            @Override
            public void onLocationAvailability (@NonNull LocationAvailability locationAvailability) {
                super.onLocationAvailability (locationAvailability);
            }
        };
    }

    public void getOrder (String orderId) {
        firebaseFirestore.collection ("Order")
                .document (orderId).get ().addOnSuccessListener (documentSnapshot ->
                orderMutableLiveData.postValue (documentSnapshot.toObject (Order.class)));
    }

    public void getCourierLocation (String courierUid) {
        firebaseFirestore.collection (application.getString (R.string.courier)).document (courierUid).addSnapshotListener ((documentSnapshot, error) -> {
            Location temp = new Location (LocationManager.GPS_PROVIDER);
            temp.setLatitude (documentSnapshot.get ("latitude", Double.class));
            temp.setLongitude (documentSnapshot.get ("longitude", Double.class));
            currentLocationMutableLiveData.postValue (temp);
        });
    }

    @SuppressLint("MissingPermission")
    public void startLocationService () {
        stopLocationService ();
        serviceState.setValue (true);
        fusedLocationProviderClient.requestLocationUpdates (mLocationRequest, mLocationCallback, Looper.getMainLooper ());
    }

    public void stopLocationService () {
        serviceState.setValue (false);
        fusedLocationProviderClient.removeLocationUpdates (mLocationCallback);
    }

    @SuppressLint("MissingPermission")
    public void updateCourierLocation (String courierUid, Location location) {
        firebaseFirestore.collection ("Courier").document (courierUid)
                .get ().addOnSuccessListener (documentSnapshot -> {
                    if (location != null) {
                        DocumentReference dfCourier = firebaseFirestore.collection ("Courier").document (courierUid);
                        Map<String, Object> courierInfo = new HashMap<> ();
                        if (documentSnapshot.get ("latitude") != null) {
                            courierInfo.put ("latitude", location.getLatitude ());
                            courierInfo.put ("longitude", location.getLongitude ());
                            dfCourier.update (courierInfo);
                            dfCourier.get ();
                        }
                    }
                }
        );
    }

    public void getLocation () {
        if (ActivityCompat.checkSelfPermission (application, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission (application, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            authStateMutableLiveData.postValue (new AuthState ("Location Permission not granted", false));
        } else {
            fusedLocationProviderClient.getLastLocation ().addOnSuccessListener (location -> {
                if (location != null) {
                    currentLocationMutableLiveData.postValue (location);
                }
            });
        }
    }


    public void documentExists (String authtype, int requestcode) {
        String[] authTypes_array = new String[]{"Customer", "Laundry House", "Courier"};
        for (int i = 0; i < authTypes_array.length; i++) {
            int finalI = i;

            firebaseFirestore.collection (authTypes_array[i]).document (Objects.requireNonNull (mAuth.getUid ())).get ().addOnSuccessListener (documentSnapshot -> {
                //Document Exists in the correct authtype repository
                if (documentSnapshot.get ("name") != null && authtype.equals (authTypes_array[finalI])) {
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    authStateMutableLiveData.postValue (new AuthState
                            ("Successfully logged in as " + authtype, true));
                } else if (documentSnapshot.get ("name") != null && !authtype.equals (authTypes_array[finalI])) {
                    authStateMutableLiveData.postValue (new AuthState
                            ("Could not log in, user exists as " + authTypes_array[finalI] + "\n Change user type", false));
                } else if (documentSnapshot.get ("name") == null && authtype.equals (authTypes_array[finalI]) && finalI == 2 && requestcode == R.integer.Login)
                    authStateMutableLiveData.postValue (new AuthState
                            ("You are not registered, sign up", false));
                else if (documentSnapshot.get ("name") == null && authtype.equals (authTypes_array[finalI]) && finalI == 2 && requestcode == R.integer.Signup) {
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    authStateMutableLiveData.postValue (new AuthState
                            ("Successfully logged in as " + authtype, true));
                }
            });
        }
    }

    public void firebaseAuthWithGoogle (String authtype, String idToken, int requestcode) {
        AuthCredential credential = GoogleAuthProvider.getCredential (idToken, null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener (ContextCompat.getMainExecutor (application), task -> {
                    if (task.isSuccessful ()) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d (TAG, "signInWithCredential:success");
                        documentExists (authtype, requestcode);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w (TAG, "signInWithCredential:failure", task.getException ());
                        authStateMutableLiveData.postValue (new AuthState ("signInWithCredential:failure" + task.getException (), false));
                    }
                });
    }

    public void registerWithEmail (String email, String password, String confirmpassword, String authtype) {
        if (email.trim ().isEmpty () || password.trim ().isEmpty () || authtype.trim ().isEmpty () || confirmpassword.trim ().isEmpty ()) {
            Log.d (TAG, "Fields cannot be empty");
            authStateMutableLiveData.postValue (new AuthState ("Email or password cannot be empty", false));
        } else if (!password.equals (confirmpassword)) {
            Log.d (TAG, "Passwords do not match");
            authStateMutableLiveData.postValue (new AuthState ("Passwords do not match", false));
        } else {
            mAuth.createUserWithEmailAndPassword (email, password).addOnCompleteListener (task -> {
                if (task.isSuccessful ()) {
                    Log.d (TAG, "signInWithEmail:success");
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    authStateMutableLiveData.postValue (new AuthState ("signInWithEmail:success", true));
                } else {
                    Log.d (TAG, "Registration failed");
                    authStateMutableLiveData.postValue (new AuthState ("Registration failed", false));
                }
            });
        }
    }

    public void loginEmail (String email, String password, String authtype) {
        if (email.trim ().equals ("") || password.trim ().equals (""))
            authStateMutableLiveData.postValue (new AuthState ("Email or password cannot be empty", false));
        else {
            mAuth.signInWithEmailAndPassword (email, password).addOnCompleteListener (task -> {
                if (task.isSuccessful ()) {
                    Log.d (TAG, "EmailLogin:success");
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    authStateMutableLiveData.postValue (new AuthState ("Successfully logged in as " + authtype, true));
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w (TAG, "EmailLogin:failure", task.getException ());
                    authStateMutableLiveData.postValue (new AuthState ("EmailLogin:failure" + task.getException (), false));
                }
            });
        }
    }

    public void forgotPassword (String email) {
        if (email.isEmpty ()) {
            Log.d (TAG, "Password reset unsuccessful, Enter E-mail");
            authStateMutableLiveData.postValue (new AuthState ("Password reset unsuccessful, Enter E-mail", false));
        } else {
            mAuth.sendPasswordResetEmail (email).addOnCompleteListener (task -> {
                if (task.isSuccessful ()) {
                    Log.d (TAG, "Password reset link sent to email");
                    authStateMutableLiveData.postValue (new AuthState ("Password reset link sent to email", true));
                } else {
                    Log.d (TAG, "Password reset unsuccessful, check email or register");
                    authStateMutableLiveData.postValue (new AuthState ("Password reset unsuccessful, check email or register. Error -" +
                            task.getException (), false));
                }
            });
        }

    }

    public void logout () {
        mAuth.signOut ();
        logoutMutableLiveData.postValue (true);
        authStateMutableLiveData.postValue (new AuthState ("Signed Out Successfully", false));
    }

    public void assignOrder (String courierId, String orderId) {
        firebaseFirestore.collection ("Courier").document (courierId).get ().addOnSuccessListener (documentSnapshot ->
                firebaseFirestore.collection ("Order").document (orderId).get ().addOnSuccessListener (documentSnapshot1 -> {
                    if (documentSnapshot.getString ("orderId").equals ("") && documentSnapshot1.getString ("courierId").equals ("")) {
                        DocumentReference df = firebaseFirestore.collection ("Courier").document (courierId);
                        DocumentReference df1 = firebaseFirestore.collection ("Order").document (orderId);

                        //Update Courier OrderId
                        Map<String, Object> CourierInfo = new HashMap<> ();
                        CourierInfo.put ("orderId", orderId);
                        df.update (CourierInfo);
                        df.get ();
                        //Update Order CourierId
                        Map<String, Object> OrderInfo = new HashMap<> ();
                        OrderInfo.put ("courierId", courierId);
                        df1.update (OrderInfo);
                        df1.get ();

                        authStateMutableLiveData.setValue (new AuthState ("Order assigned to Courier", true));
                    } else {
                        authStateMutableLiveData.setValue (new AuthState ("Order already assigned to Courier", true));
                    }
                }));
    }

    public void unassignOrder (String courierId, String orderId) {
        firebaseFirestore.collection ("Courier").document (courierId).get ().addOnSuccessListener (courierDocumentSnapshot ->
                firebaseFirestore.collection ("Order").document (orderId).get ().addOnSuccessListener (orderDocumentSnapshot -> {
                    if (!courierDocumentSnapshot.getString ("orderId").equals ("") && !orderDocumentSnapshot.getString ("courierId").equals ("")) {
                        DocumentReference df = firebaseFirestore.collection ("Courier").document (courierId);
                        DocumentReference df1 = firebaseFirestore.collection ("Order").document (orderId);

                        //Update Courier OrderId
                        Map<String, Object> CourierInfo = new HashMap<> ();
                        CourierInfo.put ("orderId", "");
                        df.update (CourierInfo);
                        df.get ();
                        //Update Order CourierId
                        Map<String, Object> OrderInfo = new HashMap<> ();
                        OrderInfo.put ("courierId", "");
                        df1.update (OrderInfo);
                        df1.get ();

                        authStateMutableLiveData.setValue (new AuthState ("Order unassigned to Courier", true));
                    } else {
                        authStateMutableLiveData.setValue (new AuthState ("Order already unassigned to Courier", true));
                    }
                }));
    }

    public void enterDataIntoDB (String authtype, String name, String address, String area,
                                 double latitude, double longitude) {
        if (name.trim ().isEmpty () || address.trim ().isEmpty () || area.isEmpty ())
            authStateMutableLiveData.postValue (new AuthState ("Please complete Empty Fields", false));
        else {
            FirebaseUser user = mAuth.getCurrentUser ();
            if (user != null) {
                firebaseFirestore.collection (authtype).document (user.getUid ()).get ().addOnSuccessListener (documentSnapshot -> {
                    DocumentReference df = firebaseFirestore.collection (authtype).document (user.getUid ());
                    if (documentSnapshot.get ("name") != null) {
                        Map<String, Object> UserInfo = new HashMap<> ();
                        UserInfo.put ("name", name);
                        UserInfo.put ("address", address);
                        UserInfo.put ("area", area);
                        if (latitude != 0.0 && longitude != 0) {
                            UserInfo.put ("latitude", latitude);
                            UserInfo.put ("longitude", longitude);
                        }
                        df.update (UserInfo);
                        df.get ();
                        authStateMutableLiveData.postValue (new AuthState ("Updated Successfully", true));
                    } else {
                        df.set (new ApplicationUser (address, area, authtype, user.getEmail (), latitude, longitude,
                                name, 0, false, ""));
                        df.get ();
                        authStateMutableLiveData.postValue (new AuthState ("Added to database successfully", true));
                    }
                }).addOnFailureListener (e -> authStateMutableLiveData.postValue (new AuthState ("User not found", false)));
            } else {
                authStateMutableLiveData.postValue (new AuthState ("User not found", false));
            }
        }
    }

    public void createOrder (String laundryHouseUID) {
        FirebaseUser user = mAuth.getCurrentUser ();
        if (user != null) {
            DocumentReference df = firebaseFirestore.collection ("Customer").document (user.getUid ());
            firebaseFirestore.collection ("Customer").document (Objects.requireNonNull (mAuth.getUid ())).get ()
                    .addOnSuccessListener (documentSnapshot -> {

                        //Create Order Object
                        int ordernumber = documentSnapshot.get ("orders", int.class);
                        String orderId = mAuth.getUid () + "_" + ordernumber + "_" + laundryHouseUID;
                        Order order = new Order (orderId, "", laundryItems,
                                Calendar.getInstance ().getTime ().toString (), "Order Not Started");
                        orderMutableLiveData.postValue (order);

                        //Enter Order Object into Database
                        firebaseFirestore.collection ("Order").document (orderId)
                                .get ().addOnSuccessListener (documentSnapshot1 -> {
                            DocumentReference dforder = firebaseFirestore.collection ("Order")
                                    .document (orderId);
                            if (documentSnapshot1.get ("dateTime") == null) {
                                dforder.set (order);
                                dforder.get ();
                                orderPlacementSuccessMutableLiveData.postValue (true);
                            } else
                                orderPlacementSuccessMutableLiveData.postValue (false);
                        }).addOnFailureListener (e ->
                                orderPlacementSuccessMutableLiveData.postValue (false));

                        //Update number of orders for customer
                        Map<String, Object> UserInfo = new HashMap<> ();
                        UserInfo.put ("orders", ordernumber + 1);
                        df.update (UserInfo);
                        df.get ();
                    });
        }
    }

    public void updateOrderStatus (String Status, String orderId) {
        firebaseFirestore.collection ("Order").document (orderId)
                .get ().addOnSuccessListener (documentSnapshot1 -> {
            DocumentReference dforder = firebaseFirestore.collection ("Order")
                    .document (orderId);
            if (documentSnapshot1.get ("dateTime") != null) {
                Map<String, Object> UserInfo = new HashMap<> ();
                UserInfo.put ("status", Status);
                dforder.update (UserInfo);
                dforder.get ();
                authStateMutableLiveData.postValue (new AuthState ("Order Status changed successfully", true));
            }
        });
    }

    public void changeActiveStatus (boolean isActive, String authtye, String Uid) {
        firebaseFirestore.collection (authtye).document (Uid).get ().addOnSuccessListener (documentSnapshot -> {
            DocumentReference dfActiveStatus = firebaseFirestore.collection (authtye).document (Uid);
            if (documentSnapshot.get ("active") != null) {
                Map<String, Object> UserInfo = new HashMap<> ();
                UserInfo.put ("active", isActive);
                dfActiveStatus.update (UserInfo);
                dfActiveStatus.get ();
                authStateMutableLiveData.postValue (new AuthState ("Active Status changed successfully", true));
            }
        });

    }

    public void addItem (int number) {
        switch (number) {
            case 1:
                laundryItems.add (new LaundryItem ("Shirt", 0.1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 2:
                laundryItems.add (new LaundryItem ("Pant", 0.1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 3:
                laundryItems.add (new LaundryItem ("Suit/Blazer/Coat", 1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 4:
                laundryItems.add (new LaundryItem ("Jackets/Woolen", 1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 5:
                laundryItems.add (new LaundryItem ("Carpet/Rug", 5));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 6:
                laundryItems.add (new LaundryItem ("Bedsheet/Duvet", 0.5));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case 7:
                laundryItems.add (new LaundryItem ("Towel", 0.5));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
        }
    }

    public void removeItem (String type) {
        switch (type) {
            case "Shirt":
                laundryItems.remove (new LaundryItem ("Shirt", 0.1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Pant":
                laundryItems.remove (new LaundryItem ("Pant", 0.1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Suit/Blazer/Coat":
                laundryItems.remove (new LaundryItem ("Suit/Blazer/Coat", 1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Jackets/Woolen":
                laundryItems.remove (new LaundryItem ("Jackets/Woolen", 1));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Carpet/Rug":
                laundryItems.remove (new LaundryItem ("Carpet/Rug", 5));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Bedsheet/Duvet":
                laundryItems.remove (new LaundryItem ("Bedsheet/Duvet", 0.5));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
            case "Towel":
                laundryItems.remove (new LaundryItem ("Towel", 0.05));
                laundryitemlistMutableLiveData.postValue (laundryItems);
                basketsize.postValue (laundryItems.size ());
                break;
        }
    }

    public void getApplicationUserData (String authtype) {
        firebaseFirestore.collection (authtype).document ((Objects.requireNonNull (mAuth.getUid ())))
                .get ().addOnSuccessListener (documentSnapshot -> {
            if (documentSnapshot.toObject (ApplicationUser.class) != null) {
                applicationUserMutableLiveData.postValue (documentSnapshot.toObject (ApplicationUser.class));
                //authStateMutableLiveData.postValue (new AuthState ("User Data loaded", false));
            } else {
                Log.d (TAG, "Failed to get data from firestore");
                authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
            }
        }).addOnFailureListener (e -> {
            Log.d (TAG, "Failed to get data from firestore");
            authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
        });
    }

    public void loadAllLaundryHouses () {
        ArrayList<LaundryHouse> mArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Laundry House")
                .get ().addOnSuccessListener (queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments ()) {

                if (snapshot.get ("latitude", double.class) != null
                        && snapshot.get ("longitude", double.class) != null) {
                    //noinspection ConstantConditions
                    LatLng address = new LatLng (snapshot.get ("latitude", double.class),
                            snapshot.get ("longitude", double.class));
                    LaundryHouse temp = new LaundryHouse (snapshot.getString ("name"),
                            address,
                            snapshot.getString ("area"),
                            snapshot.getString ("upiID"),
                            snapshot.getString ("payseraID"),
                            snapshot.getReference ().getId (),
                            snapshot.getBoolean ("active"));

                    firebaseFirestore.collection ("Customer")
                            .document (Objects.requireNonNull (FirebaseAuth.getInstance ().getUid ())).get ()
                            .addOnSuccessListener (documentSnapshot -> {
                                float[] results = new float[3];
                                Location.distanceBetween (
                                        (Objects.requireNonNull (documentSnapshot.get ("latitude", double.class))),
                                        (Objects.requireNonNull (documentSnapshot.get ("longitude", double.class))),
                                        address.latitude, address.longitude, results);
                                DecimalFormat df = new DecimalFormat ("0.0");
                                if ((int) results[0] > 3000) {
                                    temp.setDeliveryprice (Math.round((2.5 + ((int) results[0] - 3000) * 0.002)*10.0)/10.0);
                                } else
                                    temp.setDeliveryprice (2.5);
                                mArrayList.add (temp);
                                Collections.sort (mArrayList);
                                laundryHouseMutableLiveData.setValue (mArrayList);
                            });

                }
            }

        }).addOnFailureListener (e -> Log.d (TAG, "Failed to get data from firestore"));
    }

    public void loadAllOrders () {
        ArrayList<Order> mArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Order")
                .get ().addOnSuccessListener (queryDocumentSnapshots -> {
            for (DocumentSnapshot dsnap : queryDocumentSnapshots.getDocuments ()) {
                Order order = (dsnap.toObject (Order.class));
                mArrayList.add (order);
            }
            orderListMutableLiveData.postValue (mArrayList);
        }).addOnFailureListener (e -> Log.d (TAG, "Failed to get Orders"));
    }

    public void loadAllCouriers () {
        ArrayList<Courier> courierArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Courier").get ().addOnSuccessListener (queryDocumentSnapshots -> {
            for (DocumentSnapshot dsnap : queryDocumentSnapshots.getDocuments ()) {
                if (dsnap.get ("latitude", double.class) != null
                        && dsnap.get ("longitude", double.class) != null) {
                    //noinspection ConstantConditions
                    LatLng address = new LatLng (dsnap.get ("latitude", double.class),
                            dsnap.get ("longitude", double.class));
                    courierArrayList.add (new Courier (
                            dsnap.getString ("name"),
                            dsnap.getId (),
                            address,
                            dsnap.getString ("orderId"),
                            dsnap.getBoolean ("active")));
                }
            }
            courierListMutableLiveData.postValue (courierArrayList);
        }).addOnFailureListener (e -> {
            authStateMutableLiveData.postValue (
                    new AuthState ("Could not load Couriers", false));
            Log.d (TAG, "Failed to get courier data from firestore");
        });
    }

    public void getUserLatLng (String OrderUid) {
        String[] Ids = OrderUid.split ("_");
        List<LatLng> latLngs = new ArrayList<> ();
        firebaseFirestore.collection (application.getString (R.string.customer)).document (Ids[0]).get ().addOnSuccessListener (documentSnapshot -> {
            if (documentSnapshot.get ("latitude") != null)
                latLngs.add (new LatLng (documentSnapshot.get ("latitude", Double.class), documentSnapshot.get ("longitude", Double.class)));
        });
        firebaseFirestore.collection (application.getString (R.string.laundryhouse)).document (Ids[2]).get ().addOnSuccessListener (documentSnapshot -> {
            if (documentSnapshot.get ("latitude") != null)
                latLngs.add (new LatLng (documentSnapshot.get ("latitude", Double.class), documentSnapshot.get ("longitude", Double.class)));
            userLatLngListMutableLiveData.postValue (latLngs);
        });
    }

    public void fetchCanUseGooglePay() {
        final JSONObject isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest();
        if (isReadyToPayJson == null) {
            _canUseGooglePay.setValue(false);
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson(isReadyToPayJson.toString());
        Task<Boolean> task = paymentsClient.isReadyToPay(request);
        task.addOnCompleteListener(
                completedTask -> {
                    if (completedTask.isSuccessful()) {
                        _canUseGooglePay.setValue(completedTask.getResult());
                    } else {
                        Log.w("isReadyToPay failed", completedTask.getException());
                        _canUseGooglePay.setValue(false);
                    }
                });
    }
    public void getLoadPaymentDataTask(final long priceCents) {
        JSONObject paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest(priceCents);
        PaymentDataRequest request =
                PaymentDataRequest.fromJson(paymentDataRequestJson.toString());
        paymentDataTaskMutableLiveData.postValue ( paymentsClient.loadPaymentData(request));
    }
}
