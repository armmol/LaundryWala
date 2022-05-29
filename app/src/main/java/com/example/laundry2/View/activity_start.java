package com.example.laundry2.View;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

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
                    .setPositiveButton (android.R.string.yes, (dialog, which) ->
                            createDialog (isInternetConnection ()))
                    .setIcon (android.R.drawable.ic_dialog_alert)
                    .show ();
        } else {
            viewModel.getPermission ().observe (this, permission -> {
                if (permission != null && permission.permission.equals ("granted")) {
                    if (viewModel.getCurrentSignInUser ().getValue () != null) {
                        if (getIntent ().hasExtra ("type"))
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
                } else
                    permissionDialog ();
            });
        }
    }

    public void permissionDialog () {
        new AlertDialog.Builder (this)
                .setTitle ("LOCATION ACCESS")
                .setMessage ("LaundryWala collects location data to enable live location sharing when required even when the app is closed or not in use. Press OK to continue. Scenarios of use - \n" +
                        "[Customer - never]\n" +
                        "[Laundry House - never]\n" +
                        "[Courier - during Order Tracking]\n")
                .setPositiveButton (android.R.string.yes, (dialog, which) -> {
                    viewModel.insertPermission ("granted");
                    createDialog (isInternetConnection ());
                })
                .setNegativeButton ("I don't want to continue", (dialog, which) -> {
                    Toast.makeText (this, "We are sorry to see you go :(", Toast.LENGTH_SHORT).show ();
                    exit ();
                })
                .setIcon (android.R.drawable.ic_dialog_alert)
                .show ();
    }

    private void exit () {
        Intent intent = new Intent (Intent.ACTION_MAIN);
        intent.addCategory (Intent.CATEGORY_HOME);
        intent.setFlags (Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity (intent);
    }
}
