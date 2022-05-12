package com.example.laundry2.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityLoginBinding;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;


public class activity_login extends AppCompatActivity {

    private static final String TAG = "Activity Login";
    private ActivityLoginBinding binding;
    private AuthenticationViewModel authenticationViewModel;
    private String spinnerItem = "SELECT USER TYPE";
    private ActivityResultLauncher<Intent> googleSignInResultHandler;
    private PopupWindow window;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_login);
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        googleSignInResultHandler = registerForActivityResult (new ActivityResultContracts.StartActivityForResult (), result -> {
                    if (result.getResultCode () == RESULT_OK) {
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent (result.getData ());
                        try {
                            // Google Sign In was successful, authenticate with Firebase
                            GoogleSignInAccount account = task.getResult (ApiException.class);
                            Log.d (TAG, "firebaseAuthWithGoogle:" + account.getId ());
                            authenticationViewModel.signinGoogle (spinnerItem, account.getIdToken (), R.integer.Login);
                        } catch (ApiException e) {
                            // Google Sign In failed, update UI appropriately
                            Log.w (TAG, "Google sign in failed", e);
                        }
                    }
                });

        //Spinner
        binding.spinnerLogin.setAdapter (ArrayAdapter.createFromResource (this, R.array.Authentication_type, R.layout.spinner_item));
        binding.spinnerLogin.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
            @Override
            public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                spinnerItem = adapterView.getItemAtPosition (i).toString ();
                //saveState ();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {
                spinnerItem = "SELECT USER TYPE";
            }
        });

        authenticationViewModel.getAuthType ().observe (this, authType -> {
            if (authType != null)
                spinnerItem = authType.authtype;
        });
        authenticationViewModel.getState ().observe (this, authState ->
                Toast.makeText (activity_login.this, authState.getType (), Toast.LENGTH_SHORT).show ());
        authenticationViewModel.getLogoutMutableLiveData ().observe (this, isLoggedOut ->
                authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
                    authenticationViewModel.checkIsForProfileCompleted (spinnerItem, user.getUid ());
                    authenticationViewModel.getState ().observe (activity_login.this, authState -> {
                        if (!spinnerItem.equals ("SELECT USER TYPE") && !isLoggedOut && authState.isValid ()) {
                            startActivity (new Intent (activity_login.this, activity_home.class));
                        } else if (authState.getType ().equals ("User Data Failed to load"))
                            startActivity (new Intent (activity_login.this, activity_profile.class));
                    });
                }));

        //Login Button
        binding.btnLogin.setOnClickListener (view -> {
            if (!spinnerItem.equals ("SELECT USER TYPE")) {
//                binding.btnLogin.setClickable (false);
                authenticationViewModel.loginEmail (binding.edtxtEmailLogin.getText ().toString (),
                        binding.edtxtPasswordLogin.getText ().toString (), spinnerItem);
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });

        //Google Sign In Button
        binding.btnGooglesigninLogin.setOnClickListener (view -> {
            if (!spinnerItem.equals ("SELECT USER TYPE")) {
                authenticationViewModel.getGoogleSignInClient ().observe (activity_login.this, googleSignInClient ->
                        googleSignInResultHandler.launch (googleSignInClient.getSignInIntent ()));
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });
        binding.txtGotosignupfromlogin.setOnClickListener (view ->
                startActivity (new Intent (activity_login.this, activity_signup.class)));
        binding.txtForgotpasswordLogin.setOnClickListener (view ->
                forgotPasswordHandler ());
    }

    private void forgotPasswordHandler () {
        @SuppressLint("InflateParams") View windowView = LayoutInflater.from (activity_login.this).inflate (R.layout.activity_forgotpassword, null);
        window = new PopupWindow (windowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (windowView, Gravity.CENTER, 0, 0);
        EditText txt = windowView.findViewById (R.id.edtxt_email_forgotpassword);
        Button forgotButton = windowView.findViewById (R.id.button_forgotpassword);
        forgotButton.setOnClickListener (view -> {
            authenticationViewModel.forgotPassword (txt.getText ().toString ());
            authenticationViewModel.getState ().observe (activity_login.this, authState -> {
                if (authState.isValid ()) {
                    window.dismiss ();
                }
            });
        });
    }

    @Override
    protected void onStart () {
        super.onStart ();
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        if (getIntent ().hasExtra ("fromNotification")) {
            authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
                startActivity (new Intent (activity_login.this, activity_home.class)
                        .putExtra ("fromNotification", true)
                        .putExtra ("orderId", getIntent ().getStringExtra ("orderId"))
                        .putExtra ("type", getIntent ().getStringExtra ("type")));
            });
        }
    }
}

