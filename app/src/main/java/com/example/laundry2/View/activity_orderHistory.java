package com.example.laundry2.View;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.Adapters.OrderHistoryAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.LocationViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityOrderhistoryBinding;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class activity_orderHistory extends AppCompatActivity {

    private ActivityOrderhistoryBinding binding;
    private AuthenticationViewModel viewModel;
    private LocationViewModel locationViewModel;
    private boolean isOrderTracking;
    private PopupWindow window;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = DataBindingUtil.setContentView (this, R.layout.activity_orderhistory);
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        locationViewModel = new ViewModelProvider (this).get (LocationViewModel.class);
        AtomicInteger flag = new AtomicInteger (1);
        viewModel.getOrderTracking ().observe (this, orderTracking -> {
            if (flag.intValue () == 1 && orderTracking != null) {
                if (orderTracking.isOrderTracking.equals (getString (R.string.isordertracking))) {
                    binding.textView10.setText (getString (R.string.start_tracking));
                    isOrderTracking = true;
                }
                flag.set (0);
            }
        });
        viewModel.getAuthType ().observe (this, authtype -> {
            if (authtype != null) {
                String authType = authtype.authtype;
                binding.recyclerViewOrderhistory.setLayoutManager (new LinearLayoutManager (this));
                viewModel.getCurrentSignInUser ().observe (this, user ->
                        viewModel.loadAllOrders (authType, user.getUid (), true));
                viewModel.getOrders ().observe (this, orders -> {
                    if (isOrderTracking) {
                        ArrayList<Order> trackOrderList = new ArrayList<> (orders);
                        for (Order order : orders)
                            if (order.getStatus ().equals ("Completed"))
                                trackOrderList.remove (order);
                        if (trackOrderList.size () != 0)
                            binding.noDisplayText.setVisibility (View.INVISIBLE);
                        OrderHistoryAdapter adapter = new OrderHistoryAdapter (this, trackOrderList, 1);
                        binding.recyclerViewOrderhistory.setAdapter (adapter);
                        adapter.setOnItemClickListener (this::trackerOnClick);
                    } else {
                        if (orders.size () != 0)
                            binding.noDisplayText.setVisibility (View.INVISIBLE);
                        OrderHistoryAdapter adapter = new OrderHistoryAdapter (this, orders, 0);
                        binding.recyclerViewOrderhistory.setAdapter (adapter);
                        adapter.setOnItemClickListener (this::onItemCheckClick);
                        adapter.setOnStatusCheckListener (order -> onStatusCheckClick (order, authType));
                    }
                });
            }
        });
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                startActivity (new Intent (activity_orderHistory.this, activity_home.class));
                viewModel.removeIsOrderTrackingData ();
            }
        };
        this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else {
            startActivity (new Intent (activity_orderHistory.this, activity_maps.class));
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
                Toast.makeText (this, "Permission denied", Toast.LENGTH_SHORT).show ();
        }
    }

    private View createPopUpWindow (int layout) {
        View popupWindowView = LayoutInflater.from (activity_orderHistory.this).inflate (layout, null);
        window = new PopupWindow (popupWindowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (popupWindowView, Gravity.CENTER, 0, 0);
        return popupWindowView;
    }

    @Override
    protected void onStart () {
        super.onStart ();
    }

    private void onItemCheckClick (Order order) {
        View windowView = createPopUpWindow (R.layout.activity_viewitems);
        ConstraintLayout layout = windowView.findViewById (R.id.viewItemsConstraintLayout);
        layout.setOnClickListener (mView -> window.dismiss ());
        RecyclerView recyclerView = windowView.findViewById (R.id.recyclerView_confirmorder);
        ArrayList<LaundryItemCache> laundryItemCaches = new ArrayList<> ();
        for (LaundryItem laundryItem : order.getItems ())
            laundryItemCaches.add (new LaundryItemCache (laundryItem.getType () + "," + laundryItem.getCost ()));
        LaundryItemsAdapter laundryItemsAdapter = new LaundryItemsAdapter (this, laundryItemCaches, 1);
        recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
        recyclerView.setAdapter (laundryItemsAdapter);
    }

    private void onStatusCheckClick (Order order, String authtype) {
        if (authtype.equals (getString (R.string.courier))) {
            Toast.makeText (this, "You have completed this order", Toast.LENGTH_SHORT).show ();
        } else {
            View windowView = createPopUpWindow (R.layout.window_orderstatus_adapter);
            ConstraintLayout layout = windowView.findViewById (R.id.orderStatusConstraintLayout);
            layout.setOnClickListener (mView -> window.dismiss ());
            CheckBox checkBox1, checkBox2, checkBox3, checkBox4;
            checkBox1 = windowView.findViewById (R.id.cardview_orderstatus_checkBox);
            checkBox2 = windowView.findViewById (R.id.cardview_orderstatus_checkBox2);
            checkBox3 = windowView.findViewById (R.id.cardview_orderstatus_checkBox3);
            checkBox4 = windowView.findViewById (R.id.cardview_orderstatus_checkBox4);
            checkBox1.setChecked (order.getCustomerPickUp ());
            checkBox2.setChecked (order.getLaundryHouseDrop ());
            checkBox3.setChecked (order.getLaundryHousePickUp ());
            checkBox4.setChecked (order.getCustomerDrop ());
            checkBox1.setTextColor (order.getCustomerPickUp () ? Color.GREEN : Color.RED);
            checkBox2.setTextColor (order.getLaundryHouseDrop () ? Color.GREEN : Color.RED);
            checkBox3.setTextColor (order.getLaundryHousePickUp () ? Color.GREEN : Color.RED);
            checkBox4.setTextColor (order.getCustomerDrop () ? Color.GREEN : Color.RED);
        }
    }

    private void trackerOnClick (Order order) {
        locationViewModel.getCustomerOrder (order.getOrderId ());
        AtomicBoolean toastDone = new AtomicBoolean (false);
        locationViewModel.getOrder ().observe (this, order1 -> {
            if (!order1.getCourierId ().equals ("")) {
                viewModel.insertCurrentOrderCourierId (order1.getCourierId (), order1.getOrderId ());
                getPermissions ();
            } else if (!toastDone.get ()) {
                Toast.makeText (this, order.getStatus () + "\nNo Courier assigned", Toast.LENGTH_SHORT).show ();
                toastDone.set (true);
            }
        });
    }
}
