package com.example.laundry2.View;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.R;

public class activity_start extends AppCompatActivity {

    private AuthenticationViewModel viewModel;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_start);
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        new Handler ().postDelayed (() -> {
            createDialog (isInternetConnection ());
            finish ();
        }, 3000);
    }

    @Override
    protected void onStart () {
        super.onStart ();
    }

    private boolean isInternetConnection () {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getNetworkInfo (ConnectivityManager.TYPE_MOBILE).getState () == NetworkInfo.State.CONNECTED ||
                connectivityManager.getNetworkInfo (ConnectivityManager.TYPE_WIFI).getState () == NetworkInfo.State.CONNECTED;
    }

    private void createDialog (Boolean connected) {
        if (!connected) {
            new AlertDialog.Builder (this)
                    .setTitle ("NO INTERNET")
                    .setMessage ("Please Connect to the Internet")
                    .setPositiveButton (android.R.string.yes, (dialog, which) -> createDialog (isInternetConnection ()))
                    .setIcon (android.R.drawable.ic_dialog_alert)
                    .show ();
        } else {
            if (viewModel.getCurrentSignInUser ().getValue () != null) {
                if (getIntent ().hasExtra ("fromNotification"))
                    startActivity (new Intent (activity_start.this, activity_home.class)
                            .putExtra ("fromNotification", true)
                            .putExtra ("orderId", getIntent ().getStringExtra ("orderId"))
                            .putExtra ("courierId", getIntent ().getStringExtra ("courierId"))
                            .putExtra ("type", getIntent ().getStringExtra ("type")));
                else if (getIntent ().hasExtra ("update"))
                    startActivity (new Intent (activity_start.this, activity_orderHistory.class)
                            .putExtra ("update", getIntent ().getStringExtra ("update"))
                            .putExtra ("orderId", getIntent ().getStringExtra ("orderId")));
                else {
                    startActivity (new Intent (activity_start.this, activity_home.class));
                }
            } else
                startActivity (new Intent (activity_start.this, activity_login.class));
        }
    }

}
