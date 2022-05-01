package com.example.laundry2.View;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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

    private AuthenticationViewModel viewModel;
    private ActivitySignupBinding binding;
    private String spinneritem="SELECT USER TYPE";
    private static final String TAG = "Sign Up Activity";

    private final ActivityResultLauncher<Intent> googlesigninresulthandler = registerForActivityResult (new ActivityResultContracts.StartActivityForResult (), new ActivityResultCallback<ActivityResult> () {
        @Override
        public void onActivityResult (ActivityResult result) {
            if (result.getResultCode () == RESULT_OK) {
                Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent (result.getData ());
                try {
                    // Google Sign In was successful, authenticate with Firebase
                    GoogleSignInAccount account = task.getResult (ApiException.class);
                    Log.d (TAG, "firebaseAuthWithGoogle:" + account.getId ());
                    viewModel.signinGoogle (spinneritem, account.getIdToken (), R.integer.Signup);
                } catch (ApiException e) {
                    // Google Sign In failed, update UI appropriately
                    Log.w (TAG, "Google sign in failed", e);
                }
            }
        }
    });

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_signup);
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);

        binding.spinner.setAdapter (ArrayAdapter.createFromResource (this, R.array.Authentication_type, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item));
        binding.spinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
            @Override
            public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                spinneritem = adapterView.getItemAtPosition (i).toString ();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {
                spinneritem = "SELECT USER TYPE";
            }
        });

        viewModel.getState ().observe (activity_signup.this, authState
                -> Toast.makeText (activity_signup.this, authState.getType (), Toast.LENGTH_SHORT).show ());

        viewModel.getCurrentSignInUser ().observe (this, user -> {
            if (!spinneritem.equals ("SELECT USER TYPE")) {
                Toast.makeText (activity_signup.this, "Signed in as -" + user.getEmail (), Toast.LENGTH_SHORT).show ();
                startActivity (new Intent (activity_signup.this, activity_profile.class)
                        .putExtra ("authtype", spinneritem));
            }
        });

        binding.btnSignup.setOnClickListener (view ->
                viewModel.signupEmail (binding.edtxtEmailSignup.getText ().toString (),
                binding.edtxtPasswordSignup.getText ().toString (),
                binding.edtxtConfirmPasswordSignup.getText ().toString (),
                spinneritem));

        binding.txtGotologinfromsignup.setOnClickListener (view ->
                startActivity (new Intent (activity_signup.this, activity_login.class)));

        binding.btnGooglesignin.setOnClickListener (view -> {
            if (!spinneritem.equals ("SELECT USER TYPE")) {
                viewModel.getGoogleSignInClient ().observe (activity_signup.this,
                        googleSignInClient -> googlesigninresulthandler.launch (googleSignInClient.getSignInIntent ()));
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });
    }
}
