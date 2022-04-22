package com.example.laundry2.View;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.LocationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.Repositories.LocationService;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


public class activity_maps extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private LocationViewModel locationViewModel;
    private String authsType;
    int flag = 0;

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_maps);

        locationViewModel = new ViewModelProvider (this).get (LocationViewModel.class);
        authsType = getIntent ().getStringExtra ("authtype");
        if (authsType.equals (getString (R.string.courier))) {
            locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location -> {
                locationViewModel.updateLiveLocation (locationViewModel.getCurrentSignedInUser ().getValue ().getUid (), location);
                //Toast.makeText (this, location.toString (), Toast.LENGTH_SHORT).show ();
            });
        }


        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager ()
                .findFragmentById (R.id.map);
        mapFragment.getMapAsync (this);
        OnBackPressedCallback callback = new OnBackPressedCallback (true /* enabled by default */) {
            @Override
            public void handleOnBackPressed () {
                flag = 1;
                startActivity (new Intent (activity_maps.this, activity_home.class)
                        .putExtra ("authtype", getIntent ().getExtras ().get ("authtype").toString ()));
            }
        };
        this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else {
            if (authsType.equals (getString (R.string.courier)))
                startLocationUpdates ();
            else if (authsType.equals (getString (R.string.customer)))
                getCourierUpdates ();
        }
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions,
                                            @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getPermissions ();
            } else
                startActivity (new Intent ());
            Toast.makeText (this, "Permission denied", Toast.LENGTH_SHORT).show ();
        }
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        flag = 1;
        stopLocationUpdates ();
    }

    @Override
    protected void onStart () {
        super.onStart ();
        flag = 0;
        authsType = getIntent ().getStringExtra ("authtype");
        getPermissions ();
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady (@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMyLocationEnabled (true);
        if(getIntent ().hasExtra ("LaundryHouseLatLng")) {
            MarkerOptions laundry_house_location = new MarkerOptions ().title ("Laundry House Location");
            laundry_house_location.position (getIntent ().getParcelableExtra ("LaundryHouseLatLng"));
            laundry_house_location.icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_YELLOW));
            mMap.addMarker (laundry_house_location);
            MarkerOptions customer_location = new MarkerOptions ().title ("Customer Location");
            customer_location.icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN));
            customer_location.position (getIntent ().getParcelableExtra ("CustomerLatLng"));
            mMap.addMarker (customer_location);
        }
        if (authsType.equals (getString (R.string.customer))) {
            getCourierUpdates ();
            customerMap ();
        }
    }

    private void customerMap () {
        MarkerOptions markerOptions = new MarkerOptions ().title ("Your Courier is here");
        locationViewModel.getCurrentLocationMutableLiveData ().observe (this, location -> {
            Log.d ("Update Location", location.getLatitude () + "," + location.getLongitude ());
            mMap.clear ();
            LatLng latLng = new LatLng (location.getLatitude (), location.getLongitude ());
            markerOptions.position (latLng);
            mMap.addMarker (markerOptions);
            mMap.moveCamera (CameraUpdateFactory.newLatLng (latLng));
            mMap.setMinZoomPreference (14.0f);
        });
    }

    private void getCourierUpdates () {
        locationViewModel.getCourierLocation (getIntent ().getStringExtra ("courierId"));
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
}