package com.example.laundry2.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.LocationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.Services.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class activity_maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnMapClickListener {

    private GoogleMap mMap;
    private LocationViewModel locationViewModel;
    private AuthenticationViewModel authenticationViewModel;
    private Marker laundryHouse, customer;
    private PopupWindow window;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_maps);

        locationViewModel = new ViewModelProvider (this).get (LocationViewModel.class);
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        authenticationViewModel.getAuthType ().observe (this, authtype -> {
            if (authtype != null) {
                String authType = authtype.authtype;
                if (authType.equals (getString (R.string.courier))) {
                    locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location ->
                            locationViewModel.updateLiveLocation (locationViewModel.getCurrentSignedInUser ().getValue ().getUid (), location));
                    courierMap ();
                    Button refreshMap = findViewById (R.id.button_refreshMap);
                    refreshMap.setVisibility (View.VISIBLE);
                    refreshMap.setOnClickListener (view -> {
                        mMap.clear ();
                        courierMap ();
                    });
                }

                // Obtain the SupportMapFragment and get notified when the map is ready to be used.
                SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                        .findFragmentById (R.id.map);
                mapFragment.getMapAsync (this);
                Button back = findViewById (R.id.backButton_map);
                back.setOnClickListener (this::goBack);
                OnBackPressedCallback callback = new OnBackPressedCallback (true) {
                    @Override
                    public void handleOnBackPressed () {
                        goBack (null);
                    }
                };
                this.getOnBackPressedDispatcher ().addCallback (callback);
            }
        });
    }


    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else {
            authenticationViewModel.getAuthType ().observe (this, authtype -> {
                if (authtype != null) {
                    String authType = authtype.authtype;
                    if (authType.equals (getString (R.string.courier))) {
                        startLocationUpdates ();
                    } else if (authType.equals (getString (R.string.customer)) || authType.equals (getString (R.string.laundryhouse))) {
                        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId -> {
                            if (currentOrderCourierId != null && Double.parseDouble (currentOrderCourierId.deliveryCost) > 1)
                                getCourierUpdates ();
                        });
                    }
                }
            });
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPermissions ();
            } else {
                Toast.makeText (this, "Permission denied", Toast.LENGTH_SHORT).show ();
            }
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        authenticationViewModel.removeCurrentOrderCourierId ();
        stopLocationUpdates ();
    }

    @Override
    protected void onStart () {
        super.onStart ();
        getPermissions ();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady (@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled (true);
        mMap.setOnMapClickListener (this);
        mMap.moveCamera (CameraUpdateFactory.newLatLng (new LatLng (54.904572, 23.938514)));
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId ->
                authenticationViewModel.getAuthType ().observe (this, authtype -> {
                    String authType = authtype.authtype;
                    if (authType.equals (getString (R.string.courier))) {
                        courierMap ();
                    } else {
                        if (currentOrderCourierId != null && Double.parseDouble (currentOrderCourierId.deliveryCost) > 1) {
                            getCourierUpdates ();
                            courierTrackingMap ();
                        } else
                            laundryHouseTrackingMap ();
                    }
                }));
    }

    private void courierMap () {
        authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
            authenticationViewModel.loadAllOrders (getString (R.string.courier), user.getUid (), false);
            authenticationViewModel.orderIDChange (getString (R.string.courier), user.getUid ());
        });
        authenticationViewModel.getOrders ().observe (this, orders -> {
            mMap.clear ();
            for (Order order : orders) {
                AtomicBoolean done = new AtomicBoolean (false);
                authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
                    if (!done.get ()) {
                        locationViewModel.orderChange (user.getUid (), order.getOrderId ());
                        if ((order.getCustomerPickUp () && order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                                || order.getCustomerPickUp () && !order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ()) {
                            laundryHouse = mMap.addMarker (new MarkerOptions ().title ("Laundry House-" + order.getCustomerEmail ())
                                    .position (new LatLng (order.getLaundryHouseDeliveryLocationLatitude (), order.getLaundryHouseDeliveryLocationLongitude ()))
                                    .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW)));
                            laundryHouse.setTag (order);
                        } else if ((!order.getCustomerPickUp () && !order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                                || order.getCustomerPickUp () && order.getLaundryHouseDrop () && order.getLaundryHousePickUp () && !order.getCustomerDrop ()) {
                            customer = mMap.addMarker (new MarkerOptions ().title ("Customer-" + order.getCustomerEmail ())
                                    .position (new LatLng (order.getCustomerDeliveryLocationLatitude (), order.getCustomerDeliveryLocationLongitude ()))
                                    .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN)));
                            customer.setTag (order);
                        }
                        done.set (true);
                    }
                });
                mMap.setMinZoomPreference (12.0f);
                mMap.setOnMarkerClickListener (this);
            }
        });
    }

    private void courierTrackingMap () {
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId -> {
            if (currentOrderCourierId != null)
                locationViewModel.getUserAndLaundryHouseMarkerLocation (currentOrderCourierId.orderId);
        });
        MarkerOptions customer = new MarkerOptions ().title ("Delivery Location").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN));
        MarkerOptions laundryHouse = new MarkerOptions ().title ("Laundry House").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW));
        MarkerOptions courier = new MarkerOptions ().title ("Your Courier is here");
        AtomicBoolean focused = new AtomicBoolean (true);
        authenticationViewModel.getAuthType ().observe (this, authType ->
                locationViewModel.getLatLngMutableLiveData ().observe (this, latLngList -> {
                    customer.position (latLngList.get (0));
                    laundryHouse.position (latLngList.get (1));
                    mMap.addMarker (customer);
                    mMap.addMarker (laundryHouse);
                    if (authType.authtype.equals (getString (R.string.customer)) && focused.get ())
                        mMap.moveCamera (CameraUpdateFactory.newLatLng (customer.getPosition ()));
                    if (authType.authtype.equals (getString (R.string.laundryhouse)) && focused.get ()) {
                        mMap.moveCamera (CameraUpdateFactory.newLatLng (laundryHouse.getPosition ()));
                    }
                    focused.set (false);
                }));
        locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location -> {
            Log.d ("Update Location", location.getLatitude () + "," + location.getLongitude ());
            mMap.clear ();
            LatLng latLng = new LatLng (location.getLatitude (), location.getLongitude ());
            courier.position (latLng);
            mMap.addMarker (courier);
            mMap.setMinZoomPreference (12.0f);
        });
    }

    private void laundryHouseTrackingMap () {
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId -> {
            if (currentOrderCourierId != null)
                locationViewModel.getUserAndLaundryHouseMarkerLocation (currentOrderCourierId.orderId);
        });
        MarkerOptions customer = new MarkerOptions ().title ("Your delivery Location").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN));
        MarkerOptions laundryHouse = new MarkerOptions ().title ("Laundry House").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW));
        AtomicBoolean isSet = new AtomicBoolean (false);
        locationViewModel.getLatLngMutableLiveData ().observe (this, latLngs -> {
            if (!isSet.get ()) {
                customer.position (latLngs.get (0));
                laundryHouse.position (latLngs.get (1));
                mMap.addMarker (customer);
                mMap.addMarker (laundryHouse);
                isSet.set (true);
                mMap.setMinZoomPreference (12.0f);
            }
        });
    }

    private void getCourierUpdates () {
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId -> {
            if (currentOrderCourierId != null)
                locationViewModel.getCourierLocation (currentOrderCourierId.courierId);
        });
    }

    private void startLocationUpdates () {
        locationViewModel.startLiveLocation ();
        startForegroundService ();
    }

    private void stopLocationUpdates () {
        locationViewModel.stopLiveLocation ();
        stopForegroundService ();
    }

    private void stopForegroundService () {
        stopService (new Intent (this, LocationService.class));
    }

    private void startForegroundService () {
        ContextCompat.startForegroundService (this, new Intent (this, LocationService.class));
    }

    @Override
    public boolean onMarkerClick (@NonNull Marker marker) {
        AtomicBoolean checkDist = new AtomicBoolean (false);
        locationViewModel.getCurrentLocation ();
        locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location -> {
            if (!checkDist.get ()) {
                checkDist.set (true);
                Location markerLoc = new Location ("marker");
                markerLoc.setLatitude (marker.getPosition ().latitude);
                markerLoc.setLongitude (marker.getPosition ().longitude);
                if (location.distanceTo (markerLoc) < 100) {
                    Order order = (Order) marker.getTag ();
                    Button customerPickUp, customerDropOff, laundryHousePickUp, laundryHouseDropOff;
                    customerPickUp = findViewById (R.id.button_arrival_courier_customer_pick_up);
                    customerDropOff = findViewById (R.id.button_arrival_courier_customer_drop_off);
                    laundryHousePickUp = findViewById (R.id.button_arrival_courier_laundry_house_pick_up);
                    laundryHouseDropOff = findViewById (R.id.button_arrival_courier_laundry_house_drop_off);
                    //Courier has to drop the order to laundry house
                    if (order.getCustomerPickUp () && !order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                        laundryHouseDropOff.setVisibility (View.VISIBLE);
                        //Courier has to pick up the order from laundry house
                    else if (order.getCustomerPickUp () && order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                        laundryHousePickUp.setVisibility (View.VISIBLE);
                        //Courier has to drop the order to customer
                    else if (order.getCustomerPickUp () && order.getLaundryHouseDrop () && order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                        customerDropOff.setVisibility (View.VISIBLE);
                        //Courier has to pick up order from customer
                    else customerPickUp.setVisibility (View.VISIBLE);
                    authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
                        String[] check = order.getOrderId ().split ("_");
                        customerPickUp.setOnClickListener (view -> {
                            authenticationViewModel.notifyOfArrival (order.getOrderId (), check[0],
                                    "Courier Arrival", String.format ("Arrived at Customer location\n-For Pick Up-%s-%s", order.getOrderId (), user.getUid ()));
                            customerPickUp.setVisibility (View.INVISIBLE);
                        });
                        laundryHousePickUp.setOnClickListener (view -> {
                            authenticationViewModel.notifyOfArrival (order.getOrderId (), check[2],
                                    "Courier Arrival", String.format
                                            ("Arrived at Laundry House location\n-For Pick Up-%s-%s", order.getOrderId (), user.getUid ()));
                            laundryHousePickUp.setVisibility (View.INVISIBLE);
                        });
                        customerDropOff.setOnClickListener (view -> {
                            authenticationViewModel.notifyOfArrival (order.getOrderId (), check[0],
                                    "Courier Arrival", String.format
                                            ("Arrived at Customer location\n-For Drop off-%s-%s", order.getOrderId (), user.getUid ()));
                            customerDropOff.setVisibility (View.INVISIBLE);
                        });
                        laundryHouseDropOff.setOnClickListener (view -> {
                            authenticationViewModel.notifyOfArrival (order.getOrderId (), check[2],
                                    "Courier Arrival", String.format
                                            ("Arrived at Laundry House location\n-For Drop off-%s-%s", order.getOrderId (), user.getUid ()));
                            laundryHouseDropOff.setVisibility (View.INVISIBLE);
                        });
                        authenticationViewModel.getState ().observe (this, authState ->
                                Toast.makeText (this, authState.getType (), Toast.LENGTH_SHORT).show ());
                    });
                }
            }
        });
        return false;
    }

    private void goBack (View view) {
        stopLocationUpdates ();
        authenticationViewModel.removeCurrentOrderCourierId ();
        startActivity (new Intent (activity_maps.this, activity_home.class));
    }

    @Override
    public void onMapClick (@NonNull LatLng latLng) {
        Button customerPickUp, customerDropOff, laundryHousePickUp, laundryHouseDropOff;
        customerPickUp = findViewById (R.id.button_arrival_courier_customer_pick_up);
        customerDropOff = findViewById (R.id.button_arrival_courier_customer_drop_off);
        laundryHousePickUp = findViewById (R.id.button_arrival_courier_laundry_house_pick_up);
        laundryHouseDropOff = findViewById (R.id.button_arrival_courier_laundry_house_drop_off);
        customerDropOff.setVisibility (View.INVISIBLE);
        laundryHouseDropOff.setVisibility (View.INVISIBLE);
        customerPickUp.setVisibility (View.INVISIBLE);
        laundryHousePickUp.setVisibility (View.INVISIBLE);
    }
}