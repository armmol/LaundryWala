package com.example.laundry2.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivitySignupBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


public class activity_signup extends AppCompatActivity {

    private static final String TAG = "Sign Up Activity";
    private AuthenticationViewModel viewModel;
    private ActivitySignupBinding binding;
    private String spinnerItem = "SELECT USER TYPE";
    private ActivityResultLauncher<Intent> googleSignInResultHandler;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_signup);
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        googleSignInResultHandler = registerForActivityResult
                (new ActivityResultContracts.StartActivityForResult (), result -> {
                    if (result.getResultCode () == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent (result.getData ());
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult (ApiException.class);
                            Log.d (TAG, "firebaseAuthWithGoogle:" + account.getId ());
                            viewModel.signinGoogle (spinnerItem, account.getIdToken (), R.integer.Signup);
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w (TAG, "Google sign in failed", e);
                        }
                    }
                });
        binding.spinnerSignup.setAdapter (ArrayAdapter.createFromResource (this, R.array.Authentication_type, R.layout.spinner_item));
        binding.spinnerSignup.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
            @Override
            public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                spinnerItem = adapterView.getItemAtPosition (i).toString ();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {
                spinnerItem = "SELECT USER TYPE";
            }
        });
        viewModel.getState ().observe (activity_signup.this, authState
                -> Toast.makeText (activity_signup.this, authState.getType (), Toast.LENGTH_SHORT).show ());
        viewModel.getCurrentSignInUser ().observe (this, user -> {
            if (!spinnerItem.equals ("SELECT USER TYPE"))
                startActivity (new Intent (activity_signup.this, activity_profile.class));
        });
        binding.btnSignup.setOnClickListener (view ->
                viewModel.signupEmail (binding.edtxtEmailSignup.getText ().toString (),
                        binding.edtxtPasswordSignup.getText ().toString (),
                        binding.edtxtConfirmPasswordSignup.getText ().toString (),
                        spinnerItem));
        binding.txtGotologinfromsignup.setOnClickListener (view ->
                startActivity (new Intent (activity_signup.this, activity_login.class)));
        binding.btnGooglesigninSignup.setOnClickListener (view -> {
            if (!spinnerItem.equals ("SELECT USER TYPE")) {
                viewModel.getGoogleSignInClient ().observe (activity_signup.this,
                        googleSignInClient -> googleSignInResultHandler.launch (googleSignInClient.getSignInIntent ()));
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });
    }
}
