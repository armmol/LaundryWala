package com.example.laundry2.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
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

import java.util.concurrent.atomic.AtomicInteger;


public class activity_maps extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

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
                OnBackPressedCallback callback = new OnBackPressedCallback (true) {
                    @Override
                    public void handleOnBackPressed () {
                        stopLocationUpdates ();
                        startActivity (new Intent (activity_maps.this, activity_home.class));
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
                    if (authType.equals (getString (R.string.courier)))
                        startLocationUpdates ();
                    else if (authType.equals (getString (R.string.customer)) || authType.equals (getString (R.string.laundryhouse)))
                        getCourierUpdates ();
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
        authenticationViewModel.getAuthType ().observe (this, authtype -> {
            String authType = authtype.authtype;
            if (authType.equals (getString (R.string.courier))) {
                courierMap ();
            } else {
                getCourierUpdates ();
                customerTrackingMap ();
            }
        });
    }

    private void courierMap () {
        authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
                authenticationViewModel.loadAllOrders (getString (R.string.courier), user.getUid (), false));
        authenticationViewModel.getOrders ().observe (this, orders -> {
            mMap.clear ();
            for (Order order : orders) {
                AtomicInteger flag = new AtomicInteger ();
                flag.set (0);
                locationViewModel.getCustomerEmail (order.getOrderId ());
                authenticationViewModel.getCurrentSignInUser ().observe (this, user -> locationViewModel.orderChange (user.getUid (), order.getOrderId ()));
                locationViewModel.getUserAndLaundryHouseMarkerLocation (order.getOrderId ());
                locationViewModel.getCustomerEmailMutableLiveData ().observe (this, emailCustomer -> {
                    if (flag.get () == 0) {
                        flag.set (1);
                        if ((order.getCustomerPickUp () && order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                                || order.getCustomerPickUp () && !order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ()) {
                            laundryHouse = mMap.addMarker (new MarkerOptions ().title ("Laundry House-" + emailCustomer)
                                    .position (new LatLng (order.getLaundryHouseDeliveryLocationLatitude (), order.getLaundryHouseDeliveryLocationLongitude ()))
                                    .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW)));
                            laundryHouse.setTag (order);
                            mMap.moveCamera (CameraUpdateFactory.newLatLng (laundryHouse.getPosition ()));
                        } else if ((!order.getCustomerPickUp () && !order.getLaundryHouseDrop () && !order.getLaundryHousePickUp () && !order.getCustomerDrop ())
                                || order.getCustomerPickUp () && order.getLaundryHouseDrop () && order.getLaundryHousePickUp () && !order.getCustomerDrop ()) {
                            customer = mMap.addMarker (new MarkerOptions ().title ("Customer-" + emailCustomer)
                                    .position (new LatLng (order.getCustomerDeliveryLocationLatitude (), order.getCustomerDeliveryLocationLongitude ()))
                                    .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN)));
                            customer.setTag (order);
                            mMap.moveCamera (CameraUpdateFactory.newLatLng (customer.getPosition ()));
                        }
                        mMap.setMinZoomPreference (12.0f);
                        mMap.setOnMarkerClickListener (this);
                    }
                });
            }
        });
    }

    private void customerTrackingMap () {
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId ->
                locationViewModel.getUserAndLaundryHouseMarkerLocation (currentOrderCourierId.orderId));
        MarkerOptions customer = new MarkerOptions ().title ("Delivery Location").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN));
        MarkerOptions laundryHouse = new MarkerOptions ().title ("Laundry House").icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW));
        locationViewModel.getLatLngMutableLiveData ().observe (this, latLngs -> {
            customer.position (latLngs.get (0));
            laundryHouse.position (latLngs.get(1));
            MarkerOptions markerOptions = new MarkerOptions ().title ("Your Courier is here");
            locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location -> {
                Log.d ("Update Location", location.getLatitude () + "," + location.getLongitude ());
                mMap.clear ();
                LatLng latLng = new LatLng (location.getLatitude (), location.getLongitude ());
                markerOptions.position (latLng);
                mMap.addMarker (markerOptions);
                mMap.addMarker (customer);
                mMap.addMarker (laundryHouse);
                mMap.setMinZoomPreference (12.0f);
            });
        });

    }

    private void getCourierUpdates () {
        authenticationViewModel.getCurrentOrderCourierId ().observe (this, currentOrderCourierId ->
                locationViewModel.getCourierLocation (currentOrderCourierId.courierId));
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

    private View createPopUpWindow (int layout) {
        View popupWindowView = LayoutInflater.from (activity_maps.this).inflate (layout, null);
        window = new PopupWindow (popupWindowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (popupWindowView, Gravity.BOTTOM, -1, 0);
        return popupWindowView;
    }

    @Override
    public boolean onMarkerClick (@NonNull Marker marker) {
        Order order = (Order) marker.getTag ();
        View orderStatusView = createPopUpWindow (R.layout.window_arrival_courier);
        ConstraintLayout layout = orderStatusView.findViewById (R.id.courierArrivalConstraintLayout);
        layout.setOnClickListener (view -> window.dismiss ());
        Button customerPickUp, customerDropOff, laundryHousePickUp, laundryHouseDropOff;
        customerPickUp = orderStatusView.findViewById (R.id.button_arrival_courier_customer_pick_up);
        customerDropOff = orderStatusView.findViewById (R.id.button_arrival_courier_customer_drop_off);
        laundryHousePickUp = orderStatusView.findViewById (R.id.button_arrival_courier_laundry_house_pick_up);
        laundryHouseDropOff = orderStatusView.findViewById (R.id.button_arrival_courier_laundry_house_drop_off);
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
            customerPickUp.setOnClickListener (view -> authenticationViewModel.notifyOfArrival (order.getOrderId (), check[0],
                    "Courier Arrival", String.format ("Arrived at Customer location\n-For Pick Up-%s-%s", order.getOrderId (), user.getUid ())));
            laundryHousePickUp.setOnClickListener (view -> authenticationViewModel.notifyOfArrival (order.getOrderId (), check[2],
                    "Courier Arrival", String.format ("Arrived at Laundry House location\n-For Pick Up-%s-%s", order.getOrderId (), user.getUid ())));
            customerDropOff.setOnClickListener (view -> authenticationViewModel.notifyOfArrival (order.getOrderId (), check[0],
                    "Courier Arrival", String.format ("Arrived at Customer location\n-For Drop off-%s-%s", order.getOrderId (), user.getUid ())));
            laundryHouseDropOff.setOnClickListener (view -> authenticationViewModel.notifyOfArrival (order.getOrderId (), check[2],
                    "Courier Arrival", String.format ("Arrived at Laundry House location\n-For Drop off-%s-%s", order.getOrderId (), user.getUid ())));
        });
        return false;
    }
}