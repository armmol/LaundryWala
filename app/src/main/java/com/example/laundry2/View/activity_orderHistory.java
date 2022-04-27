package com.example.laundry2.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.Adapters.OrderHistoryAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.LocationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityOrderhistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class activity_orderHistory extends AppCompatActivity {

    private ActivityOrderhistoryBinding binding;
    private AuthenticationViewModel viewModel;
    private LocationViewModel locationViewModel;
    private String authType;
    private Intent i;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_orderhistory);
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        locationViewModel = new ViewModelProvider (this).get (LocationViewModel.class);
        i = new Intent (activity_orderHistory.this, activity_maps.class)
                .putExtra ("authtype", getIntent ().getExtras ().get ("authtype").toString ());
        authType = getIntent ().getStringExtra ("authtype");
        if (getIntent ().hasExtra ("entry")) {
            binding.textView10.setText (getString (R.string.start_tracking));
        }
        binding.recyclerViewOrderhistory.setLayoutManager (new LinearLayoutManager (this));
        viewModel.loadAllOrders (authType);
        viewModel.getOrders ().observe (this, orders -> {
            List<Order> refinedList = new ArrayList<> (orders);
            for (Order order : orders) {
                String[] check = order.getOrderId ().split ("_");
                if (authType.equals (getString (R.string.laundryhouse))) {
                    if (!check[2].equals (viewModel.getCurrentSignInUser ().getValue ().getUid ()))
                        refinedList.remove (order);
                } else if (authType.equals (getString (R.string.customer))) {
                    if (!check[0].equals (viewModel.getCurrentSignInUser ().getValue ().getUid ()))
                        refinedList.remove (order);
                }
            }

            OrderHistoryAdapter adapter = new OrderHistoryAdapter (this, refinedList);
            binding.recyclerViewOrderhistory.setAdapter (adapter);
            adapter.setOnItemClickListener (order -> {
                if (getIntent ().hasExtra ("entry")) {
                    locationViewModel.getCustomerOrder (order.getOrderId ());
                    locationViewModel.getOrder ().observe (this, order1 -> {
                        if (!order1.getCourierId ().equals ("")) {
                            i.putExtra ("courierId", order1.getCourierId ());
                            getPermissions ();
                        } else
                            Toast.makeText (this, "Order is not in pick/delivery phase or\n " +
                                    "no courier is assigned to order yet.\n" +
                                    "Please try after some time.", Toast.LENGTH_SHORT).show ();
                    });
                } else {
                    View windowView = LayoutInflater.from (activity_orderHistory.this).inflate (R.layout.activity_confirmorder, null);
                    PopupWindow window = new PopupWindow (windowView);
                    window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setFocusable (true);
                    window.showAtLocation (windowView, Gravity.CENTER, 0, 0);
                    RecyclerView recyclerView = windowView.findViewById (R.id.recyclerView_confirmorder);
                    LaundryItemsAdapter laundryItemsAdapter = new LaundryItemsAdapter (this, order.getItems ());
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (laundryItemsAdapter);
                    laundryItemsAdapter.setOnItemClickListener (laundryItem ->
                            Toast.makeText (this, "Cannot change order Once placed", Toast.LENGTH_SHORT).show ());
                }

            });
        });
        OnBackPressedCallback callback = new OnBackPressedCallback (true /* enabled by default */) {
            @Override
            public void handleOnBackPressed () {
                startActivity (new Intent (activity_orderHistory.this, activity_home.class)
                        .putExtra ("authtype", authType));
            }
        };
        this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else {
            startActivity (i);
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
}
