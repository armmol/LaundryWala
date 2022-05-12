package com.example.laundry2.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityProfileBinding;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;

import java.util.Arrays;
import java.util.List;


public class activity_profile extends AppCompatActivity {

    private AuthenticationViewModel viewModel;
    private ActivityProfileBinding binding;
    private LatLng latLng;
    private ActivityResultLauncher<Intent> findAddress;
    private double latitude = 0, longitude = 0;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_profile);
        findAddress = registerForActivityResult (new ActivityResultContracts.StartActivityForResult (), result -> {
                    if (result.getResultCode () == RESULT_OK) {
                        if (result.getData () != null) {
                            Place place = Autocomplete.getPlaceFromIntent (result.getData ());
                            binding.edtxtAddressProfile.setText (place.getAddress ());
                            binding.txtAreaProfile.setText (place.getAddress ().split (",")[1]);
                            latLng = place.getLatLng ();
                        }
                    } else
                        binding.edtxtAddressProfile.setText (getString (R.string.failed_try_again));
                });
        viewModel.getState ().observe (this, authState -> {
            Toast.makeText (activity_profile.this, authState.getType (), Toast.LENGTH_SHORT).show ();
            if (authState.isValid ())
                startActivity (new Intent (activity_profile.this, activity_home.class));
        });

        binding.imgbtnFindaddress.setOnClickListener (view -> {
            List<Place.Field> fieldList = Arrays.asList (Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
            findAddress.launch (new Autocomplete.IntentBuilder (AutocompleteActivityMode.OVERLAY
                    , fieldList).build (activity_profile.this));
        });


        binding.btnLogoutProfile.setOnClickListener (view -> viewModel.signOut ());

        viewModel.getLogoutMutableLiveData ().observe (this, isLoggedOut -> {
            if (isLoggedOut)
                startActivity (new Intent (activity_profile.this, activity_login.class));
        });
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                Toast.makeText (activity_profile.this, "please complete your profile", Toast.LENGTH_SHORT).show ();
            }
        };
        getOnBackPressedDispatcher ().addCallback (callback);

        viewModel.getCurrentSignInUser ().observe (this, user -> {
            binding.edtxtEmailProfile.setText (user.getEmail ());
            viewModel.getAuthType ().observe (this, authType -> {
                if (authType != null) {
                    String authtype = authType.authtype;
                    binding.btnSaveProfile.setOnClickListener (view -> {
                        if (latLng != null) {
                            latitude = latLng.latitude;
                            longitude = latLng.longitude;
                        }
                        viewModel.enterIntoDB (user.getUid (), user.getEmail (), authtype,
                                binding.edtxtNameProfile.getText ().toString (),
                                binding.edtxtAddressProfile.getText ().toString (),
                                binding.txtAreaProfile.getText ().toString (),
                                latitude, longitude);
                    });
                }
            });
        });
    }

    @Override
    protected void onStart () {
        super.onStart ();
        viewModel.getAuthType ().observe (this, authtype -> {
            if (authtype != null) {
                binding.txtAuthtypeProfile.setText (authtype.authtype);
                viewModel.getCurrentSignInUser().observe(this, user -> viewModel.loadApplicationUserData (authtype.authtype, user.getUid ()));
                viewModel.getApplicationUserData ().observe (activity_profile.this, applicationUser -> {
                    binding.txtAreaProfile.setText (applicationUser.getArea ());
                    binding.edtxtAddressProfile.setText (applicationUser.getAddress ());
                    binding.edtxtNameProfile.setText (applicationUser.getName ());
                    binding.edtxtEmailProfile.setText (applicationUser.getEmail ());
                    OnBackPressedCallback callback = new OnBackPressedCallback (true) {
                        @Override
                        public void handleOnBackPressed () {
                            startActivity (new Intent (activity_profile.this, activity_home.class));
                        }
                    };
                    activity_profile.this.getOnBackPressedDispatcher ().addCallback (callback);
                });
            }
        });
    }
}
