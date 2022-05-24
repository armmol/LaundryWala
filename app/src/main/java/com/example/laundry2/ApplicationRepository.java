package com.example.laundry2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;
import android.util.Patterns;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundry2.DataClasses.ApplicationUser;
import com.example.laundry2.DataClasses.AuthState;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.ApplicationDao;
import com.example.laundry2.Database.ApplicationDatabase;
import com.example.laundry2.Database.AuthType;
import com.example.laundry2.Database.CurrentOrderCourierId;
import com.example.laundry2.Database.LaundryHouseCache;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.Database.OrderTracking;
import com.example.laundry2.PaymentUtil.PaymentsUtil;
import com.example.laundry2.Services.NotificationSender;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;


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
    private final MutableLiveData<Integer> basketSize;
    private final MutableLiveData<List<LaundryItem>> laundryItemListMutableLiveData;
    private final MutableLiveData<List<Courier>> courierListMutableLiveData;
    private final MutableLiveData<AuthState> orderPlacementSuccessMutableLiveData;
    private final MutableLiveData<Boolean> courierArrivalMutableLiveData;
    private final MutableLiveData<String> customerEmail;
    private final MutableLiveData<Double> newDeliveryCostMutableLiveData;
    private final ArrayList<LaundryItem> laundryItems;
    //Database datasets
    private final ApplicationDao applicationDao;
    private final LiveData<AuthType> authTypeLiveData;
    private final LiveData<List<LaundryItemCache>> laundryItemCacheLiveData;
    private final LiveData<LaundryHouseCache> laundryHouseCacheLiveData;
    private final LiveData<OrderTracking> orderTrackingLiveData;
    private final LiveData<CurrentOrderCourierId> currentOrderCourierIdLiveData;

    @SuppressLint("MissingPermission")
    public ApplicationRepository (Application application) {
        this.application = application;

        //Firebase
        mAuth = FirebaseAuth.getInstance ();
        firebaseFirestore = FirebaseFirestore.getInstance ();
        subscribeToChannel (mAuth.getUid ());
        FirebaseMessaging.getInstance ().setAutoInitEnabled (true);
        FirebaseAnalytics.getInstance (application).setAnalyticsCollectionEnabled (true);
        //DataBase
        ApplicationDatabase applicationDatabase = ApplicationDatabase.getDatabase (application);
        applicationDao = applicationDatabase.appDao ();
        laundryItemCacheLiveData = applicationDao.getAllItems ();
        authTypeLiveData = applicationDao.getAuthType ();
        laundryHouseCacheLiveData = applicationDao.getLaundryHouseCache ();
        orderTrackingLiveData = applicationDao.getIsOrderTracking ();
        currentOrderCourierIdLiveData = applicationDao.getCurrentOrderCourierId ();
        //Payments
        paymentsClient = PaymentsUtil.createPaymentsClient (application);
        //MutableLiveData
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
        laundryItemListMutableLiveData = new MutableLiveData<> ();
        basketSize = new MutableLiveData<> ();
        laundryItems = new ArrayList<> ();
        courierListMutableLiveData = new MutableLiveData<> ();
        orderPlacementSuccessMutableLiveData = new MutableLiveData<> ();
        userLatLngListMutableLiveData = new MutableLiveData<> ();
        courierArrivalMutableLiveData = new MutableLiveData<> ();
        newDeliveryCostMutableLiveData = new MutableLiveData<> ();
        customerEmail = new MutableLiveData<> ();

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

        serviceState.postValue (true);
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

    public MutableLiveData<Boolean> get_canUseGooglePay () {
        return _canUseGooglePay;
    }

    public MutableLiveData<Task<PaymentData>> getPaymentDataTaskMutableLiveData () {
        return paymentDataTaskMutableLiveData;
    }

    public MutableLiveData<Location> getCurrentLocationMutableLiveData () {
        return currentLocationMutableLiveData;
    }

    public MutableLiveData<Order> getOrderMutableLiveData () {
        return orderMutableLiveData;
    }

    public MutableLiveData<Integer> getBasketSize () {
        return basketSize;
    }

    public MutableLiveData<List<LaundryItem>> getLaundryItemList () {
        return laundryItemListMutableLiveData;
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

    public MutableLiveData<AuthState> getOrderPlacementSuccessMutableLiveData () {
        return orderPlacementSuccessMutableLiveData;
    }

    public MutableLiveData<List<LatLng>> getUserLatLngListMutableLiveData () {
        return userLatLngListMutableLiveData;
    }

    public MutableLiveData<Boolean> getCourierArrivalMutableLiveData () {
        return courierArrivalMutableLiveData;
    }

    public MutableLiveData<Double> getNewDeliveryCostMutableLiveData () {
        return newDeliveryCostMutableLiveData;
    }

    public LiveData<List<LaundryItemCache>> getLaundryItemCacheLiveData () {
        return laundryItemCacheLiveData;
    }

    public LiveData<LaundryHouseCache> getLaundryHouseCacheLiveData () {
        return laundryHouseCacheLiveData;
    }

    public LiveData<AuthType> getAuthTypeLiveData () {
        return authTypeLiveData;
    }

    public MutableLiveData<String> getCustomerEmail () {
        return customerEmail;
    }

    public LiveData<OrderTracking> getOrderTrackingLiveData () {
        return orderTrackingLiveData;
    }

    public LiveData<CurrentOrderCourierId> getCurrentOrderCourierIdLiveData () {
        return currentOrderCourierIdLiveData;
    }

    public void getOrder (String orderId) {
        firebaseFirestore.collection ("Order")
                .document (orderId).get ().addOnCompleteListener (task ->
                orderMutableLiveData.postValue (task.getResult ().toObject (Order.class)));
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
                            authStateMutableLiveData.postValue (new AuthState ("Updated successfully", false));
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

    private void SignUpCreateDocument (String authtype) {
        firebaseFirestore.collection (authtype).document (mAuth.getUid ()).get ().addOnSuccessListener (documentSnapshot -> {
            DocumentReference df = firebaseFirestore.collection (authtype).document (mAuth.getUid ());
            df.set (new ApplicationUser ("", "", authtype, mAuth.getCurrentUser ().getEmail (), 0.0, 0.0,
                    "", 0, false, new ArrayList<> ()));
            df.get ();
        });
    }

    public void isProfileCompleted (String authtype, String uid) {
        if (authtype.equals ("")) {
            String[] authTypes = new String[]{"Laundry House", "Courier", "Customer"};
            for (int i = 0; i < 3; i++) {
                firebaseFirestore.collection (authTypes[i]).document ((Objects.requireNonNull (uid)))
                        .get ().addOnCompleteListener (task -> {
                    if (task.getResult ().get ("address") == "") {
                        Log.d (TAG, "Failed to get data from firestore");
                        authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
                    }
                }).addOnFailureListener (e -> {
                    Log.d (TAG, "Failed to get data from firestore");
                    authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
                });
            }
        } else {
            firebaseFirestore.collection (authtype).document ((Objects.requireNonNull (uid)))
                    .get ().addOnCompleteListener (task -> {
                if (task.getResult ().get ("address") == "") {
                    Log.d (TAG, "Failed to get data from firestore");
                    authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
                }
            }).addOnFailureListener (e -> {
                Log.d (TAG, "Failed to get data from firestore");
                authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
            });
        }
    }

    private void loginCheckIfAuthTypeIsValid (String authtype, String uid) {
        String[] authTypes_array = new String[]{"Customer", "Laundry House", "Courier"};
        for (int i = 0; i < authTypes_array.length; i++) {
            int finalI = i;
            firebaseFirestore.collection (authTypes_array[i]).document (uid).get ().addOnSuccessListener (documentSnapshot -> {
                //Correct User Type
                if (documentSnapshot.get ("email") != null && authtype.equals (authTypes_array[finalI])) {
                    //userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    applicationDao.insertAuthtype (new AuthType (authtype));
                    authStateMutableLiveData.postValue (new AuthState
                            ("Successfully logged in as " + authtype, true));
                }
                //Wrong User Type
                else if (documentSnapshot.get ("email") != null && !authtype.equals (authTypes_array[finalI])) {
                    authStateMutableLiveData.postValue (new AuthState
                            ("Could not log in, user exists as " + authTypes_array[finalI] + " Change user type", false));
                }
            });
        }
    }

    private void isCorrectGoogleSignIn (String authtype, int requestCode) {
        AtomicInteger flag = new AtomicInteger (0);
        AtomicInteger flagC = new AtomicInteger (0);
        AtomicInteger flagLH = new AtomicInteger (0);
        firebaseFirestore.collection (application.getString (R.string.courier)).document (mAuth.getUid ())
                .get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()) {
                DocumentSnapshot document = task.getResult ();
                if (document.exists ()) {
                    //User is REGISTERED and has put CORRECT authType
                    if (document.get ("email") != null && authtype.equals (application.getString (R.string.courier))) {
                        userMutableLiveData.postValue (mAuth.getCurrentUser ());
                        applicationDao.deleteAuthType ();
                        applicationDao.insertAuthtype (new AuthType (authtype));
                        authStateMutableLiveData.postValue (new AuthState
                                ("Successfully logged in as " + authtype, true));
                        flag.set (100);
                    }//User is REGISTERED and has put INCORRECT authType
                    else if (document.get ("email") != null && !authtype.equals (application.getString (R.string.courier))) {
                        authStateMutableLiveData.postValue (new AuthState
                                ("Could not log in, user exists as " + application.getString (R.string.courier).toUpperCase ()
                                        + " Change user type", false));
                        mAuth.signOut ();
                        flag.set (100);
                    } else
                        flag.set (101);
                }
            }
        });
        firebaseFirestore.collection (application.getString (R.string.laundryhouse)).document (mAuth.getUid ())
                .get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()) {
                DocumentSnapshot document = task.getResult ();
                if (document.exists ()) {
                    //User is REGISTERED and has put CORRECT authType
                    if (document.get ("email") != null && authtype.equals (application.getString (R.string.laundryhouse))) {
                        userMutableLiveData.postValue (mAuth.getCurrentUser ());
                        applicationDao.deleteAuthType ();
                        applicationDao.insertAuthtype (new AuthType (authtype));
                        authStateMutableLiveData.postValue (new AuthState
                                ("Successfully logged in as " + authtype, true));
                        flagLH.set (100);
                    }//User is REGISTERED and has put INCORRECT authType
                    else if (document.get ("email") != null && !authtype.equals (application.getString (R.string.laundryhouse))) {
                        authStateMutableLiveData.postValue (new AuthState
                                ("Could not log in, user exists as " + application.getString (R.string.laundryhouse).toUpperCase ()
                                        + " Change user type", false));
                        mAuth.signOut ();
                        flagLH.set (100);
                    } else
                        flagLH.set (102);
                }
            }
        });
        firebaseFirestore.collection (application.getString (R.string.customer)).document (mAuth.getUid ())
                .get ().addOnCompleteListener (task -> {
            if (task.isSuccessful ()) {
                DocumentSnapshot document = task.getResult ();
                if (document.exists ()) {
                    //User is REGISTERED and has put CORRECT authType
                    if (document.get ("email") != null && authtype.equals (application.getString (R.string.customer))) {
                        userMutableLiveData.postValue (mAuth.getCurrentUser ());
                        applicationDao.deleteAuthType ();
                        applicationDao.insertAuthtype (new AuthType (authtype));
                        authStateMutableLiveData.postValue (new AuthState
                                ("Successfully logged in as " + authtype, true));
                        flagC.set (100);
                    }//User is REGISTERED and has put INCORRECT authType
                    else if (document.get ("email") != null && !authtype.equals (application.getString (R.string.customer))) {
                        authStateMutableLiveData.postValue (new AuthState
                                ("Could not log in, user exists as " + application.getString (R.string.customer).toUpperCase ()
                                        + " Change user type", false));
                        mAuth.signOut ();
                        flagC.set (100);
                    } else
                        flagC.set (103);
                }
            }
        });

        //User is NOT REGISTERED but is trying LOGIN
        if (requestCode == R.integer.Login && flagC.get () == 103 && flag.get () == 101 && flagLH.get () == 102) {
            authStateMutableLiveData.postValue (new AuthState
                    ("You are not registered, sign up", false));
            mAuth.signOut ();
        }
        //User is NOT REGISTERED but is trying SIGNUP
        else if (requestCode == R.integer.Signup && flagC.get () == 103 && flag.get () == 101 && flagLH.get () == 102) {
            userMutableLiveData.postValue (mAuth.getCurrentUser ());
            applicationDao.deleteAuthType ();
            applicationDao.insertAuthtype (new AuthType (authtype));
            authStateMutableLiveData.postValue (new AuthState
                    ("Successfully Signed Up as " + authtype, true));
        }
    }


    public void firebaseAuthWithGoogle (String authtype, String idToken, int requestCode) {
        AuthCredential credential = GoogleAuthProvider.getCredential (idToken, null);
        mAuth.signInWithCredential (credential)
                .addOnCompleteListener (ContextCompat.getMainExecutor (application), task -> {
                    if (task.isSuccessful ()) {
                        // Sign in success, update UI with the signed-in user's information
                        isCorrectGoogleSignIn (authtype, requestCode);
                        logoutMutableLiveData.postValue (false);
                    } else {
                        // If sign in fails, display a message to the user.
                        authStateMutableLiveData.postValue (new AuthState
                                ("You are not registered, sign up", false));
                        Log.w (TAG, "Failure to Sign in. Please Check Google Account and Internet Connection", task.getException ());
                        authStateMutableLiveData.postValue (new AuthState ("Failure to Sign in. Please Check Google Account and Internet Connection" + task.getException (), false));
                    }
                });
    }

    public void registerWithEmail (String email, String password, String confirmPassword, String authType) {
        if (email.trim ().isEmpty () || password.trim ().isEmpty ()
                || authType.trim ().isEmpty () || confirmPassword.trim ().isEmpty ())
            authStateMutableLiveData.postValue (new AuthState ("Email or password cannot be empty", false));
        else if (!password.equals (confirmPassword))
            authStateMutableLiveData.postValue (new AuthState ("Passwords do not match", false));
        else if (!Patterns.EMAIL_ADDRESS.matcher (email.trim ()).matches ())
            authStateMutableLiveData.postValue (new AuthState ("Email not in correct format", false));
        else if (password.trim ().length () < 6)
            authStateMutableLiveData.postValue (new AuthState ("Password cannot be less that 6 characters", false));
        else {
            mAuth.createUserWithEmailAndPassword (email, password).addOnCompleteListener (task -> {
                if (task.isSuccessful ()) {
                    Log.d (TAG, "signInWithEmail:success");
                    SignUpCreateDocument (authType);
                    logoutMutableLiveData.postValue (false);
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    applicationDao.insertAuthtype (new AuthType (authType));
                    authStateMutableLiveData.postValue (new AuthState
                            ("Successfully logged in as " + authType, true));
                } else {
                    Log.d (TAG, "Registration failed");
                    authStateMutableLiveData.postValue (new AuthState ("Registration failed. Account might exist, Check Email", false));
                }
            });
        }
    }

    public void loginEmail (String email, String password, String authtype) {
        if (email.trim ().equals ("") || password.trim ().equals (""))
            authStateMutableLiveData.postValue (new AuthState ("Email or password cannot be empty", false));
        else if (!Patterns.EMAIL_ADDRESS.matcher (email.trim ()).matches ())
            authStateMutableLiveData.postValue (new AuthState ("Email not in correct format", false));
        else if (password.trim ().length () < 6)
            authStateMutableLiveData.postValue (new AuthState ("Password cannot be less that 6 characters", false));
        else {
            mAuth.signInWithEmailAndPassword (email, password).addOnCompleteListener (task -> {
                if (task.isSuccessful ()) {
                    Log.d (TAG, "EmailLogin:success");
                    userMutableLiveData.postValue (mAuth.getCurrentUser ());
                    logoutMutableLiveData.postValue (false);
                    loginCheckIfAuthTypeIsValid (authtype, mAuth.getUid ());
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w (TAG, "EmailLogin:failure", task.getException ());
                    authStateMutableLiveData.postValue (new AuthState ("Login Failure, Email or Password is incorrect.\n" +
                            "Are you registered?", false));
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
        if (mAuth != null) {
            mAuth.signOut ();
            logoutMutableLiveData.postValue (true);
            authStateMutableLiveData.postValue (new AuthState ("Signed Out Successfully", false));
            applicationDao.deleteAll ();
            applicationDao.deleteAuthType ();
        } else
            authStateMutableLiveData.postValue (new AuthState ("Could not sign out as FireBase Auth is null", false));
    }

    public void assignOrder (String courierId, String orderId) {
        firebaseFirestore.collection ("Courier").document (courierId).get ().addOnSuccessListener (courierDocumentSnapshot ->
                firebaseFirestore.collection ("Order").document (orderId).get ().addOnSuccessListener (orderDocumentSnapshot -> {
                    if (orderDocumentSnapshot.getString ("courierId").equals ("")) {
                        DocumentReference dfCourier = firebaseFirestore.collection ("Courier").document (courierId);
                        DocumentReference dfOrder = firebaseFirestore.collection ("Order").document (orderId);

                        //Update Courier OrderId
                        ApplicationUser applicationUser = courierDocumentSnapshot.toObject (ApplicationUser.class);
                        applicationUser.getOrderId ().add (orderId);
                        Map<String, Object> CourierInfo = new HashMap<> ();
                        CourierInfo.put ("orderId", applicationUser.getOrderId ());
                        dfCourier.update (CourierInfo);
                        dfCourier.get ();

                        //Update Order CourierId
                        Map<String, Object> OrderInfo = new HashMap<> ();
                        OrderInfo.put ("courierId", courierId);
                        dfOrder.update (OrderInfo);
                        dfOrder.get ();
                        authStateMutableLiveData.setValue (new AuthState ("Order assigned to Courier", true));
                    } else {
                        authStateMutableLiveData.setValue (new AuthState ("Order already assigned to Courier", true));
                    }
                }));
    }

    public void unassignOrder (String orderId) {
        firebaseFirestore.collection ("Order").document (orderId).get ().addOnSuccessListener (orderDocumentSnapshot -> {
            if (orderDocumentSnapshot.getString ("dateTime") != null) {
                String courierId = orderDocumentSnapshot.getString ("courierId");
                if (!courierId.equals ("")) {
                    firebaseFirestore.collection ("Courier").document (courierId).get ().addOnSuccessListener (courierDocumentSnapshot -> {
                        DocumentReference df = firebaseFirestore.collection ("Courier").document (courierId);
                        DocumentReference df1 = firebaseFirestore.collection ("Order").document (orderId);

                        //Update Courier OrderId
                        ApplicationUser applicationUser = courierDocumentSnapshot.toObject (ApplicationUser.class);
                        applicationUser.getOrderId ().remove (orderId);
                        Map<String, Object> CourierInfo = new HashMap<> ();
                        CourierInfo.put ("orderId", applicationUser.getOrderId ());
                        df.update (CourierInfo);
                        df.get ();

                        //Update Order CourierId
                        Map<String, Object> OrderInfo = new HashMap<> ();
                        OrderInfo.put ("courierId", "");
                        df1.update (OrderInfo);
                        df1.get ();

                        authStateMutableLiveData.setValue (new AuthState ("Order unassigned from Courier", true));
                    });
                } else {
                    authStateMutableLiveData.setValue (new AuthState ("Order already unassigned from Courier", true));
                }
            }
        });
    }

    public void getCustomerEmail (String orderId) {
        String customer = orderId.split ("_")[0];
        firebaseFirestore.collection ("Customer").document (customer)
                .get ().addOnSuccessListener (documentSnapshot ->
                customerEmail.setValue (documentSnapshot.getString ("email")));
    }

    public void enterDataIntoDB (String uid, String email, String authtype, String name, String address, String area,
                                 double latitude, double longitude) {
        if (name.trim ().isEmpty () || address.trim ().isEmpty () || area.isEmpty ())
            authStateMutableLiveData.postValue (new AuthState ("Please complete Empty Fields", false));
        else {
            if (!uid.equals ("")) {
                firebaseFirestore.collection (authtype).document (uid).get ().addOnSuccessListener (documentSnapshot -> {
                    DocumentReference df = firebaseFirestore.collection (authtype).document (uid);
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
                        df.set (new ApplicationUser (address, area, authtype, email, latitude, longitude,
                                name, 0, false, new ArrayList<> ()));
                        df.get ();
                        authStateMutableLiveData.postValue (new AuthState ("Added to database successfully", true));
                    }
                }).addOnFailureListener (e -> authStateMutableLiveData.postValue (new AuthState ("User not found", false)));
            } else {
                authStateMutableLiveData.postValue (new AuthState ("User not found", false));
            }
        }
    }

    public void createOrder (String uid, String laundryHouseUID, double deliveryCost, boolean drying) {
        if (!uid.equals ("")) {
            DocumentReference dfCustomer = firebaseFirestore.collection ("Customer").document (uid);
            DocumentReference dfLaundryHouse = firebaseFirestore.collection ("Laundry House").document (laundryHouseUID);
            List<LaundryItemCache> laundryItemCacheList = applicationDao.getAllItemsAsList ();
            ArrayList<LaundryItem> newList = new ArrayList<> ();
            for (LaundryItemCache item : laundryItemCacheList) {
                String[] chek = item.getType ().split (",");
                newList.add (new LaundryItem (chek[0], Double.parseDouble (chek[1])));
            }
            firebaseFirestore.collection ("Laundry House").document (laundryHouseUID).get ().addOnSuccessListener (laundryHouseDocumentSnapshot ->
                    firebaseFirestore.collection ("Customer").document (Objects.requireNonNull (uid)).get ()
                            .addOnSuccessListener (customerDocumentSnapshot -> {
                                //Create Order Object
                                int orderNumberCustomer = customerDocumentSnapshot.get ("orders", int.class);
                                int orderNumberLaundryHouse = laundryHouseDocumentSnapshot.get ("orders", int.class);
                                String orderId = uid + "_" + orderNumberCustomer + "_" + laundryHouseUID;
                                Order tempOrder = new Order (orderId, "", newList,
                                        Calendar.getInstance ().getTime ().toString (), "Order Not Started",
                                        customerDocumentSnapshot.get ("latitude", Double.class), customerDocumentSnapshot.get ("longitude", Double.class),
                                        laundryHouseDocumentSnapshot.get ("latitude", Double.class), laundryHouseDocumentSnapshot.get ("longitude", Double.class),
                                        deliveryCost, drying,
                                        false, false, false, false, false);
                                orderMutableLiveData.postValue (tempOrder);

                                //Enter Order Object into Database
                                firebaseFirestore.collection ("Order").document (orderId)
                                        .get ().addOnSuccessListener (documentSnapshot1 -> {
                                    DocumentReference dfOrder = firebaseFirestore.collection ("Order")
                                            .document (orderId);
                                    if (documentSnapshot1.get ("dateTime") == null) {
                                        dfOrder.set (tempOrder);
                                        dfOrder.get ();
                                        //Update number of orders for customer
                                        ApplicationUser customerUser = customerDocumentSnapshot.toObject (ApplicationUser.class);
                                        customerUser.getOrderId ().add (orderId);
                                        Map<String, Object> customerUserInfo = new HashMap<> ();
                                        customerUserInfo.put ("orders", orderNumberCustomer + 1);
                                        customerUserInfo.put ("orderId", customerUser.getOrderId ());
                                        dfCustomer.update (customerUserInfo);
                                        dfCustomer.get ();
                                        //Update number of orders for laundryHouse
                                        ApplicationUser laundryHouseUser = laundryHouseDocumentSnapshot.toObject (ApplicationUser.class);
                                        laundryHouseUser.getOrderId ().add (orderId);
                                        Map<String, Object> laundryHouseUserInfo = new HashMap<> ();
                                        laundryHouseUserInfo.put ("orders", orderNumberLaundryHouse + 1);
                                        laundryHouseUserInfo.put ("orderId", laundryHouseUser.getOrderId ());
                                        dfLaundryHouse.update (laundryHouseUserInfo);
                                        dfLaundryHouse.get ();
                                        orderPlacementSuccessMutableLiveData.postValue (new AuthState ("Order was placed", true));
                                    } else
                                        orderPlacementSuccessMutableLiveData.postValue (new AuthState ("Order was not placed", false));
                                }).addOnFailureListener (e ->
                                        orderPlacementSuccessMutableLiveData.postValue (new AuthState ("Order was not placed", false)));
                            }));
        }
    }

    public void orderIDChange (String authType, String uid) {
        firebaseFirestore.collection (authType).document (uid).addSnapshotListener ((value, error) ->
                loadAllOrders (authType, uid, false));
    }

    public void orderChange (String courierId, String orderId) {
        firebaseFirestore.collection ("Order").document (orderId).addSnapshotListener ((value, error) ->
                loadAllOrders ("Courier", courierId, false));
    }

    public void updateOrderStatus (String Status, String orderId) {
        firebaseFirestore.collection ("Order").document (orderId)
                .get ().addOnSuccessListener (orderDocumentSnapshot -> {
            DocumentReference dfOrder = firebaseFirestore.collection ("Order")
                    .document (orderId);
            if (orderDocumentSnapshot.get ("dateTime") != null) {
                Map<String, Object> OrderInfo = new HashMap<> ();
                OrderInfo.put ("status", Status);
                if (orderDocumentSnapshot.toObject (Order.class).getDeliveryCost () < 1) {
                    if (Status.equals ("Order Was Delivered to Laundry House")) {
                        OrderInfo.put ("laundryHouseDrop", true);
                        OrderInfo.put ("customerPickUp", true);
                    } else if (Status.equals ("Order Was Picked Up From Laundry House")) {
                        OrderInfo.put ("laundryHousePickUp", true);
                        OrderInfo.put ("customerDrop", true);
                        OrderInfo.put ("status", "Completed");
                    }
                }
                dfOrder.update (OrderInfo);
                dfOrder.get ();
                authStateMutableLiveData.postValue (new AuthState ("Order Status changed successfully", true));
                SendNotification (orderId.split ("_")[0], "Order Update", "Update-" + orderId);
            }
        });
    }

    private void addOrderHistoryForCourier (String uid, String orderId, String type) {
        DocumentReference dfCourier = firebaseFirestore.collection ("Courier").document (uid);
        firebaseFirestore.collection ("Courier").document (uid)
                .get ()
                .addOnSuccessListener (documentSnapshot -> {
                    documentSnapshot.get (type, ArrayList.class);
                    ArrayList<String> ordersServed = new ArrayList<> ();
                    Map<String, Object> courierMap = new HashMap<> ();
                    if (documentSnapshot.get (type) != null)
                        ordersServed = (ArrayList<String>) documentSnapshot.get (type);
                    ordersServed.add (orderId);
                    courierMap.put (type, ordersServed);
                    dfCourier.update (courierMap);
                    dfCourier.get ();
                    Log.d (TAG, "DocumentSnapshot successfully written!");
                })
                .addOnFailureListener (e -> Log.w (TAG, "Error writing document", e));
    }

    public void changeOrderPickDropStatus (String orderId, String courierId, String authType, String type, boolean value) {
        firebaseFirestore.collection ("Order").document (orderId).get ().addOnSuccessListener (documentSnapshot -> {
            DocumentReference dfOrder = firebaseFirestore.collection ("Order")
                    .document (orderId);
            if (documentSnapshot.get ("dateTime") != null) {
                Map<String, Object> OrderInfo = new HashMap<> ();
                switch (authType) {
                    case "Customer":
                        if (value) {
                            if (type.equals ("For Pick Up")) {
                                OrderInfo.put ("customerPickUp", value);
                                authStateMutableLiveData.postValue (new AuthState ("Order Status changed successfully.", true));
                            } else {
                                OrderInfo.put ("customerDrop", value);
                                OrderInfo.put ("status", "Completed");
                                unassignOrder (orderId);
                                addOrderHistoryForCourier (courierId, orderId, "ordersFromLaundryHouseToCustomer");
                                authStateMutableLiveData.postValue (new AuthState ("Order Completed!", true));
                            }
                        } else {
                            if (type.equals ("For Pick Up")) {
                                OrderInfo.put ("customerPickUp", value);
                                authStateMutableLiveData.postValue (new AuthState ("Courier Could not pick up Order, we will send a new Courier", true));
                                unassignOrder (orderId);
                            } else {
                                OrderInfo.put ("customerDrop", value);
                                OrderInfo.put ("status", "Completed");
                                authStateMutableLiveData.postValue (new AuthState ("Please wait for Courier! Courier is close to you, we will update you", true));
                            }
                        }
                        break;
                    case "Laundry House":
                        if (value) {
                            if (type.equals ("For Pick Up"))
                                OrderInfo.put ("laundryHousePickUp", value);
                            else {
                                OrderInfo.put ("laundryHouseDrop", value);
                                addOrderHistoryForCourier (courierId, orderId, "ordersFromCustomerToLaundryHouse");
                                unassignOrder (orderId);
                            }
                        } else {
                            if (type.equals ("For Pick Up")) {
                                OrderInfo.put ("laundryHousePickUp", value);
                                authStateMutableLiveData.postValue (new AuthState ("Courier Could not pick up Order, we will send a new Courier", true));
                                unassignOrder (orderId);
                            } else {
                                OrderInfo.put ("laundryHouseDrop", value);
                                authStateMutableLiveData.postValue (new AuthState ("Please wait for Courier! Courier is close to you, we will update you", true));
                            }
                        }
                        authStateMutableLiveData.postValue (new AuthState ("Order Status changed successfully.", true));
                        break;
                    default:
                        authStateMutableLiveData.postValue (new AuthState ("Order not updated.", true));
                }
                dfOrder.update (OrderInfo);
                dfOrder.get ();
            }
        });
    }

    private void subscribeToChannel (String Uid) {
        FirebaseMessaging.getInstance ().subscribeToTopic (Uid);
        Log.d (TAG, "Subscribed to" + Uid);
    }

    public void notifyOfArrival (String orderId, String Uid, String title, String message) {
        firebaseFirestore.collection ("Order").document (orderId)
                .get ().addOnSuccessListener (documentSnapshot1 -> {
            DocumentReference dfOrder = firebaseFirestore.collection ("Order")
                    .document (orderId);
            if (documentSnapshot1.get ("dateTime") != null) {
                Map<String, Object> OrderInfo = new HashMap<> ();
                OrderInfo.put ("courierHasArrived", true);
                dfOrder.update (OrderInfo);
                dfOrder.get ();
                authStateMutableLiveData.postValue (new AuthState ("Notified successfully", true));
                SendNotification (Uid, title, message);
            } else
                authStateMutableLiveData.postValue (new AuthState ("Not Notified", true));
        });
    }

    private void SendNotification (String Uid, String title, String message) {
        NotificationSender notificationSender = new NotificationSender ("/topics/" + Uid, title, message, application);
        notificationSender.SendNotifications ();
        Log.d (TAG, "Notification Sent");
    }

    public void getNotified (String orderId) {
        firebaseFirestore.collection ("Order").document (orderId).addSnapshotListener ((documentSnapshot, error) -> {
            if (documentSnapshot.get ("courierHasArrived", Boolean.class)) {
                courierArrivalMutableLiveData.postValue (true);
            }
        });
    }

    public void changeActiveStatus (boolean isActive, String authtype, String Uid) {
        firebaseFirestore.collection (authtype).document (Uid).get ().addOnSuccessListener (documentSnapshot -> {
            DocumentReference dfActiveStatus = firebaseFirestore.collection (authtype).document (Uid);
            if (authtype.equals ("Courier") && documentSnapshot.toObject (ApplicationUser.class).getOrderId ().size () > 1 && !isActive) {
                authStateMutableLiveData.postValue (new AuthState ("You will not receive any more orders", true));
            } else {
                if (documentSnapshot.get ("active") != null) {
                    Map<String, Object> UserInfo = new HashMap<> ();
                    UserInfo.put ("active", isActive);
                    dfActiveStatus.update (UserInfo);
                    dfActiveStatus.get ();
                    authStateMutableLiveData.postValue (new AuthState ("Active Status changed successfully", true));
                }
            }
        });
    }

    public void insertOrderTracking (String isOrderTracking) {
        applicationDao.insertIsOrderTracking (new OrderTracking (isOrderTracking));
    }

    public void removeOrderTracking () {
        applicationDao.deleteIsOrderTracking ();
    }

    public void insertCurrentOrderCourierId (String courierId, String orderId) {
        applicationDao.insertCurrentOrderCourierId (new CurrentOrderCourierId (courierId, orderId));
    }

    public void removeCurrentOrderCourierId () {
        applicationDao.deleteCurrentOrderCourierId ();
    }

    public void clearLaundryItemCache () {
        applicationDao.deleteAll ();
    }

    public void clearBasket () {
        applicationDao.deleteAll ();
        laundryItems.clear ();
    }

    public void insertLaundryHouseCacheData (String laundryHouseID, String deliveryCost) {
        applicationDao.insertLaundryHouseData (new LaundryHouseCache (laundryHouseID, deliveryCost));
    }

    public void removerLaundryHouseCacheData () {
        applicationDao.deleteLaundryHouseCache ();
    }

    public void addItem (String type) {
        switch (type) {
            case "Shirt":
            case "Pant":
            case "Towel":
                laundryItems.add (new LaundryItem (type, 0.1));
                laundryItemListMutableLiveData.postValue (laundryItems);
                applicationDao.insertLaundryItem (new LaundryItemCache (type + ",0.1"));
                basketSize.postValue (laundryItems.size ());
                break;
            case "Suit/Blazer/Coat":
            case "Jackets/Woolen":
                laundryItems.add (new LaundryItem (type, 1));
                laundryItemListMutableLiveData.postValue (laundryItems);
                applicationDao.insertLaundryItem (new LaundryItemCache (type + ",1.0"));
                basketSize.postValue (laundryItems.size ());
                break;
            case "Carpet/Rug":
                laundryItems.add (new LaundryItem (type, 5));
                laundryItemListMutableLiveData.postValue (laundryItems);
                applicationDao.insertLaundryItem (new LaundryItemCache (type + ",5.0"));
                basketSize.postValue (laundryItems.size ());
                break;
            case "Bedsheet/Duvet":
            case "Kg":
                laundryItems.add (new LaundryItem (type, 0.5));
                laundryItemListMutableLiveData.postValue (laundryItems);
                applicationDao.insertLaundryItem (new LaundryItemCache (type + ",0.5"));
                basketSize.postValue (laundryItems.size ());
                break;
        }
    }

    public void removeItem (LaundryItemCache laundryItemCache) {
        String type = laundryItemCache.getType ().split (",")[0];
        boolean isRemoved = false;
        ArrayList<LaundryItem> refinedList = new ArrayList<> (laundryItems);
        for (LaundryItem item : refinedList) {
            if (item.getType ().equals (type) && !isRemoved) {
                laundryItems.remove (item);
                isRemoved = true;
            }
        }
        laundryItemListMutableLiveData.postValue (laundryItems);
        applicationDao.deleteLaundryItem (laundryItemCache);
        basketSize.postValue (laundryItems.size ());
    }

    public void getApplicationUserData (String authtype, String uid) {
        firebaseFirestore.collection (authtype).document ((Objects.requireNonNull (uid)))
                .get ().addOnSuccessListener (documentSnapshot -> {
            if (documentSnapshot.toObject (ApplicationUser.class) != null) {
                applicationUserMutableLiveData.postValue (documentSnapshot.toObject (ApplicationUser.class));
            } else {
                Log.d (TAG, "Failed to get data from firestore");
                authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
            }
        }).addOnFailureListener (e -> {
            Log.d (TAG, "Failed to get data from firestore");
            authStateMutableLiveData.postValue (new AuthState ("User Data Failed to load", false));
        });
    }

    public void getNewDeliveryCost (Place place, String laundryHouseUid) {
        Location a = new Location ("a");
        a.setLongitude (place.getLatLng ().longitude);
        a.setLatitude (place.getLatLng ().latitude);
        Location b = new Location ("b");
        firebaseFirestore.collection (application.getString (R.string.laundryhouse))
                .document (laundryHouseUid).get ().addOnSuccessListener (documentSnapshot -> {
            if (documentSnapshot.get ("latitude") != null) {
                b.setLongitude (documentSnapshot.get ("longitude", Double.class));
                b.setLatitude (documentSnapshot.get ("latitude", Double.class));
            }
            double cost = (a.distanceTo (b)) > 3000 ? BigDecimal.valueOf (2.5 + ((int) a.distanceTo (b) - 3000) * 0.002).setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue () : 2.5;
            newDeliveryCostMutableLiveData.postValue (cost);
        });
    }

    public void loadAllLaundryHouses (String uid) {
        ArrayList<LaundryHouse> mArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Laundry House")
                .get ().addOnSuccessListener (queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments ()) {
                if (snapshot.get ("latitude", double.class) != null && snapshot.get ("longitude", double.class) != null) {
                    //noinspection ConstantConditions
                    LatLng address = new LatLng (snapshot.get ("latitude", double.class), snapshot.get ("longitude", double.class));
                    LaundryHouse temp = new LaundryHouse (snapshot.getString ("name"),
                            address,
                            snapshot.getString ("address"),
                            snapshot.getReference ().getId (),
                            snapshot.getBoolean ("active"));
                    firebaseFirestore.collection ("Customer").document (uid).get ().addOnSuccessListener (documentSnapshot -> {
                        float[] results = new float[3];
                        Location.distanceBetween (
                                (Objects.requireNonNull (documentSnapshot.get ("latitude", double.class))),
                                (Objects.requireNonNull (documentSnapshot.get ("longitude", double.class))),
                                address.latitude, address.longitude, results);
                        if ((int) results[0] > 3000) {
                            temp.setDeliveryPrice (BigDecimal.valueOf (2.5 + ((int) results[0] - 3000) * 0.002)
                                    .setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ());
                        } else
                            temp.setDeliveryPrice (2.5);
                        mArrayList.add (temp);
                        Collections.sort (mArrayList);
                        if (mArrayList.size () == queryDocumentSnapshots.size ())
                            laundryHouseMutableLiveData.setValue (mArrayList);
                    });
                }
            }
        }).addOnFailureListener (e -> Log.d (TAG, "Failed to get data from firestore"));
    }

    public void loadAllOrders (String authtype, String uid, boolean isOrderHistory) {
        ExpressoIdlingResource.increment ();
        ArrayList<Order> mArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Order").get ().addOnSuccessListener (queryDocumentSnapshots -> {
            for (DocumentSnapshot snapshot : queryDocumentSnapshots.getDocuments ()) {
                Order order = (snapshot.toObject (Order.class));
                mArrayList.add (order);
            }
            Collections.sort (mArrayList);
            List<Order> refinedList = new ArrayList<> (mArrayList);
            for (Order order : mArrayList) {
                String[] check = order.getOrderId ().split ("_");
                if (authtype.equals (application.getString (R.string.courier))) {
                    if (order.getStatus ().equals ("Completed") ||
                            !order.getCourierId ().equals (uid))
                        refinedList.remove (order);
                } else if (authtype.equals (application.getString (R.string.customer))) {
                    if (order.getStatus ().equals ("Completed") ||
                            !check[0].equals (uid))
                        refinedList.remove (order);
                } else if (authtype.equals (application.getString (R.string.laundryhouse))) {
                    if (order.getStatus ().equals ("Completed") ||
                            !check[2].equals (uid))
                        refinedList.remove (order);
                }
            }

            List<Order> refinedListForOrderHistory = new ArrayList<> (mArrayList);
            if (authtype.equals (application.getString (R.string.courier))) {
                List<Order> refinedListForCourier = new ArrayList<> ();
                for (Order order : refinedListForOrderHistory) {
                    firebaseFirestore.collection (authtype).document (uid).get ().addOnSuccessListener (documentSnapshot -> {
                        ArrayList<String> ordersServed = new ArrayList<> ();
                        ArrayList<String> ordersServed1 = new ArrayList<> ();
                        if (documentSnapshot.get ("ordersFromCustomerToLaundryHouse") != null)
                            ordersServed = (ArrayList<String>) documentSnapshot.get ("ordersFromCustomerToLaundryHouse");
                        if (documentSnapshot.get ("ordersFromLaundryHouseToCustomer") != null)
                            ordersServed1 = (ArrayList<String>) documentSnapshot.get ("ordersFromLaundryHouseToCustomer");
                        if (ordersServed.contains (order.getOrderId ()) && ordersServed1.contains (order.getOrderId ())) {
                            order.setStatus ("Served both deliveries");
                            refinedListForCourier.add (order);
                        } else if (ordersServed.contains (order.getOrderId ()) && !ordersServed1.contains (order.getOrderId ())) {
                            order.setStatus ("Delivered From Customer to Laundry House");
                            refinedListForCourier.add (order);
                        } else if (!ordersServed.contains (order.getOrderId ()) && ordersServed1.contains (order.getOrderId ())) {
                            order.setStatus ("Delivered from Laundry House to Customer");
                            refinedListForCourier.add (order);
                        }
                        if (isOrderHistory)
                            orderListMutableLiveData.postValue (refinedListForCourier);
                        ExpressoIdlingResource.decrement ();
                    });
                }
            } else {
                for (Order order : mArrayList) {
                    String[] check = order.getOrderId ().split ("_");
                    if (authtype.equals (application.getString (R.string.customer))) {
                        if (!check[0].equals (uid))
                            refinedListForOrderHistory.remove (order);
                    } else if (authtype.equals (application.getString (R.string.laundryhouse))) {
                        if (!check[2].equals (uid))
                            refinedListForOrderHistory.remove (order);
                    }
                }
            }
            if (mArrayList.size () == queryDocumentSnapshots.size ()) {
                if (isOrderHistory && !authtype.equals (application.getString (R.string.courier)))
                    orderListMutableLiveData.postValue (refinedListForOrderHistory);
                else orderListMutableLiveData.postValue (refinedList);
                ExpressoIdlingResource.decrement ();
            }
        }).addOnFailureListener (e -> Log.d (TAG, "Failed to get Orders"));
    }

    public void loadAllCouriers (String orderId) {
        ArrayList<Courier> courierArrayList = new ArrayList<> ();
        firebaseFirestore.collection ("Courier").get ().addOnSuccessListener (queryDocumentSnapshots ->
                firebaseFirestore.collection ("Order").document (orderId).get ()
                        .addOnSuccessListener (documentSnapshotOrder -> {
                            if (documentSnapshotOrder.toObject (Order.class) != null) {
                                Order order = documentSnapshotOrder.toObject (Order.class);
                                for (DocumentSnapshot courierDocumentSnapshot : queryDocumentSnapshots.getDocuments ()) {
                                    if (courierDocumentSnapshot.get ("latitude", double.class) != null
                                            && courierDocumentSnapshot.get ("longitude", double.class) != null) {
                                        //noinspection ConstantConditions
                                        LatLng address = new LatLng (courierDocumentSnapshot.get ("latitude", double.class),
                                                courierDocumentSnapshot.get ("longitude", double.class));
                                        Location customerLoc = new Location ("customerLoc");
                                        customerLoc.setLatitude (order.getCustomerDeliveryLocationLatitude ());
                                        customerLoc.setLongitude (order.getCustomerDeliveryLocationLongitude ());
                                        Location laundryHouseLoc = new Location ("laundryHouseLoc");
                                        laundryHouseLoc.setLatitude (order.getLaundryHouseDeliveryLocationLatitude ());
                                        laundryHouseLoc.setLongitude (order.getLaundryHouseDeliveryLocationLongitude ());
                                        Location courierLoc = new Location ("courierLoc");
                                        courierLoc.setLatitude (address.latitude);
                                        courierLoc.setLongitude (address.longitude);
                                        double distCustomer, distLaundryHouse;
                                        distCustomer = BigDecimal.valueOf (Math.round (courierLoc.distanceTo (customerLoc))).movePointLeft (3).doubleValue ();
                                        distLaundryHouse = BigDecimal.valueOf (Math.round (courierLoc.distanceTo (laundryHouseLoc))).movePointLeft (3).doubleValue ();
                                        ArrayList<String> toLaundryHouse = new ArrayList<> ();
                                        ArrayList<String> toCustomer = new ArrayList<> ();
                                        if (courierDocumentSnapshot.get ("ordersFromLaundryHouseToCustomer") != null)
                                            toCustomer = (ArrayList<String>) courierDocumentSnapshot.get ("ordersFromLaundryHouseToCustomer");
                                        if (courierDocumentSnapshot.get ("ordersFromCustomerToLaundryHouse") != null)
                                            toLaundryHouse = (ArrayList<String>) courierDocumentSnapshot.get ("ordersFromCustomerToLaundryHouse");
                                        courierArrayList.add (new Courier (
                                                courierDocumentSnapshot.getString ("name"),
                                                courierDocumentSnapshot.getId (),
                                                address,
                                                courierDocumentSnapshot.toObject (ApplicationUser.class).getOrderId (),
                                                toCustomer,
                                                toLaundryHouse,
                                                distCustomer,
                                                distLaundryHouse,
                                                courierDocumentSnapshot.getBoolean ("active")));
                                    }
                                }
                                Collections.sort (courierArrayList);
                                courierListMutableLiveData.postValue (courierArrayList);
                            }
                        })).addOnFailureListener (e -> {
            authStateMutableLiveData.postValue (
                    new AuthState ("Could not load Couriers", false));
            Log.d (TAG, "Failed to get courier data from firestore");
        });
    }

    public void getUserAndLaundryHouseLatLng (String orderId) {
        String[] Ids = orderId.split ("_");
        List<LatLng> latLongs = new ArrayList<> ();
        firebaseFirestore.collection (application.getString (R.string.customer)).document (Ids[0]).get ().addOnSuccessListener (courierDocumentSnapshot -> {
            if (courierDocumentSnapshot.get ("latitude") != null)
                latLongs.add (new LatLng (courierDocumentSnapshot.get ("latitude", Double.class), courierDocumentSnapshot.get ("longitude", Double.class)));
            firebaseFirestore.collection (application.getString (R.string.laundryhouse)).document (Ids[2]).get ().addOnSuccessListener (laundryHouseDocumentSnapshot -> {
                if (laundryHouseDocumentSnapshot.get ("latitude") != null)
                    latLongs.add (new LatLng (laundryHouseDocumentSnapshot.get ("latitude", Double.class), laundryHouseDocumentSnapshot.get ("longitude", Double.class)));
                userLatLngListMutableLiveData.postValue (latLongs);
            });
        });
    }

    public void fetchCanUseGooglePay () {
        final JSONObject isReadyToPayJson = PaymentsUtil.getIsReadyToPayRequest ();
        if (isReadyToPayJson == null) {
            _canUseGooglePay.setValue (false);
            return;
        }

        // The call to isReadyToPay is asynchronous and returns a Task. We need to provide an
        // OnCompleteListener to be triggered when the result of the call is known.
        IsReadyToPayRequest request = IsReadyToPayRequest.fromJson (isReadyToPayJson.toString ());
        Task<Boolean> task = paymentsClient.isReadyToPay (request);
        task.addOnCompleteListener (
                completedTask -> {
                    if (completedTask.isSuccessful ()) {
                        _canUseGooglePay.setValue (completedTask.getResult ());
                    } else {
                        Log.w ("isReadyToPay failed", completedTask.getException ());
                        _canUseGooglePay.setValue (false);
                    }
                });
    }

    public void getLoadPaymentDataTask (final long priceCents) {
        JSONObject paymentDataRequestJson = PaymentsUtil.getPaymentDataRequest (priceCents);
        PaymentDataRequest request =
                PaymentDataRequest.fromJson (paymentDataRequestJson.toString ());
        paymentDataTaskMutableLiveData.postValue (paymentsClient.loadPaymentData (request));
    }

}
