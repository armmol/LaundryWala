package com.example.laundry2.View;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
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
    private static final String sharedPreferences_authType = "notSelected";
    private ActivityLoginBinding binding;
    private AuthenticationViewModel authenticationViewModel;
    private String spinnerItem = "";
    private final ActivityResultLauncher<Intent> googleSignInResultHandler = registerForActivityResult (new ActivityResultContracts.StartActivityForResult ()
            , new ActivityResultCallback<ActivityResult> () {
                @Override
                public void onActivityResult (ActivityResult result) {
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
                }
            });
    private PopupWindow window;

    @Override
    protected void onCreate (Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_login);
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);

        //Spinner
        binding.spinner.setAdapter (ArrayAdapter.createFromResource (this, R.array.Authentication_type, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item));
        binding.spinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
            @Override
            public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                spinnerItem = adapterView.getItemAtPosition (i).toString ();
                saveState ();
            }

            @Override
            public void onNothingSelected (AdapterView<?> adapterView) {
                spinnerItem = "";
            }
        });

        authenticationViewModel.getState ().observe (activity_login.this, authState ->
                Toast.makeText (activity_login.this, authState.getType (), Toast.LENGTH_SHORT).show ());

        authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
            if (!spinnerItem.equals ("")) {
                authenticationViewModel.getLogoutMutableLiveData ().observe (this, aBoolean -> {
                    if (! aBoolean) {
                        startActivity (new Intent (activity_login.this, activity_home.class)
                                .putExtra ("authtype", spinnerItem));
                    }
                });
            }
        });

        //Login Button
        binding.btnLogin.setOnClickListener (view -> {
            if (!spinnerItem.equals ("")) {
                authenticationViewModel.loginEmail (binding.edtxtEmailLogin.getText ().toString (),
                        binding.edtxtPasswordLogin.getText ().toString (), spinnerItem);
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });

        //Google Sign In Button
        binding.btnGooglesignin.setOnClickListener (view -> {
            if (!spinnerItem.equals ("")) {
                authenticationViewModel.getGoogleSignInClient ().observe (activity_login.this, googleSignInClient -> googleSignInResultHandler.launch (googleSignInClient.getSignInIntent ()));
            } else {
                Toast.makeText (getApplicationContext (), "Select User Type", Toast.LENGTH_SHORT).show ();
            }
        });

        binding.txtGotosignupfromlogin.setOnClickListener (view ->
                startActivity (new Intent (activity_login.this, activity_signup.class)));

        binding.txtvForgotpassword.setOnClickListener (view ->
                forgotPasswordHandler ());

    }

    private void forgotPasswordHandler () {
        @SuppressLint("InflateParams") View windowView = LayoutInflater.from (activity_login.this).inflate (R.layout.activity_assigncouriers, null);
        window = new PopupWindow (windowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (windowView, Gravity.CENTER, 0, 0);
        EditText txt = windowView.findViewById (R.id.edtxt_email_forgotpassword);
        Button forgotButton = windowView.findViewById (R.id.button);
        forgotButton.setOnClickListener (view -> {
            authenticationViewModel.forgotPassword (txt.getText ().toString ());
            authenticationViewModel.getState ().observe (activity_login.this, authState -> {
                if (authState.isValid ()) {
                    window.dismiss ();
                }
            });
        });
    }

    public void saveState () {
        SharedPreferences sharedPreferences = getSharedPreferences (sharedPreferences_authType, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit ();
        editor.putString (sharedPreferences_authType, spinnerItem);
        editor.apply ();
    }

    public void loadState () {
        SharedPreferences sharedPreferences = getSharedPreferences (sharedPreferences_authType, MODE_PRIVATE);
        spinnerItem = sharedPreferences.getString (sharedPreferences_authType, "");
    }

    @Override
    protected void onStart () {
        super.onStart ();
        loadState ();
//        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
//        if (getIntent ().hasExtra ("fromNotification")) {
//            authenticationViewModel.getCurrentSignInUser ().observe (this, user -> {
//                startActivity (new Intent (activity_login.this, activity_home.class)
//                        .putExtra ("authtype", getIntent ().getStringExtra ("authtype"))
//                        .putExtra ("fromNotification", true)
//                        .putExtra ("orderId", getIntent ().getStringExtra ("orderId"))
//                        .putExtra ("type", getIntent ().getStringExtra ("type")));
//            });
//        }
    }
}

