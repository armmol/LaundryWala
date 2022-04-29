package com.example.laundry2.View;

import static com.example.laundry2.R.id;
import static com.example.laundry2.R.layout;
import static com.example.laundry2.R.string;

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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.laundry2.Adapters.CouriersAdapter;
import com.example.laundry2.Adapters.LaundryHousesAdapter;
import com.example.laundry2.Adapters.OrdersAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.databinding.ActivityHomeUserBinding;

import java.util.ArrayList;


public class activity_home extends AppCompatActivity {

    private AuthenticationViewModel viewModel;
    private ActivityHomeUserBinding binding;
    private LaundryHousesAdapter laundryHousesAdapter;
    private OrdersAdapter ordersAdapter;
    private CouriersAdapter couriersAdapter;
    private PopupWindow window;
    private String applicationUserUid;
    private Intent iCourier;
    private String area;
    private ArrayList<String> items = null;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, layout.activity_home_user);
        binding.recyclerViewUserhome.setLayoutManager (new LinearLayoutManager (activity_home.this));
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        if (getIntent ().hasExtra ("items")) items = getIntent ().getStringArrayListExtra ("items");
        String authtype = getIntent ().getStringExtra ("authtype");
        viewModel.getState ().observe (this, authState -> {
            if (authState.isValid ()) {
                Toast.makeText (getApplicationContext (), authState.getType (), Toast.LENGTH_SHORT).show ();
            }
        });
        iCourier = new Intent (activity_home.this, activity_maps.class)
                .putExtra ("authtype", authtype);
        if (binding.switchActivestatus.isChecked ())
            binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
        else binding.switchActivestatus.setText (getString (string.switch_to_go_online));
        viewModel.loadApplicationUserData (authtype);
        viewModel.getApplicationUserData ().observe (this, applicationUser -> {
            binding.switchActivestatus.setChecked (applicationUser.isActive ());
            area = applicationUser.getArea ();
        });
        viewModel.getCurrentSignInUser ().observe (this, user -> applicationUserUid = user.getUid ());
        binding.switchActivestatus.setOnCheckedChangeListener ((compoundButton, b) -> {
            viewModel.changeActiveStatus (b, authtype, applicationUserUid);
            if (binding.switchActivestatus.isChecked ())
                binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
            else binding.switchActivestatus.setText (getString (string.switch_to_go_online));
        });

        switch (authtype) {
            case "Customer":
                if (getIntent ().hasExtra ("fromNotification")) {
                    binding.confirmOrderWindow.setVisibility (View.VISIBLE);
                    binding.recyclerViewUserhome.setVisibility (View.INVISIBLE);
                    String orderId = getIntent ().getStringExtra ("orderId");
                    String type = getIntent ().getStringExtra ("type");
                    binding.textViewNotification.setText (String.format ("%s %s", binding.textViewNotification.getText (), type));
                    binding.checkBoxYes.setOnClickListener (view -> {
                        viewModel.changeOrderPickDropStatus (orderId, authtype, type, true);
                        binding.checkBoxYes.setChecked (true);
                        Toast.makeText (this, "thank you for your response", Toast.LENGTH_SHORT).show ();
                        binding.confirmOrderWindow.setVisibility (View.INVISIBLE);
                        binding.recyclerViewUserhome.setVisibility (View.VISIBLE);
                    });
                    binding.checkBoxNo.setOnClickListener (view -> {
                        viewModel.changeOrderPickDropStatus (orderId, authtype, type, false);
                        binding.checkBoxYes.setChecked (true);
                        Toast.makeText (this, "thank you for your response", Toast.LENGTH_SHORT).show ();
                        binding.confirmOrderWindow.setVisibility (View.INVISIBLE);
                        binding.recyclerViewUserhome.setVisibility (View.VISIBLE);
                    });
                }
                binding.switchActivestatus.setVisibility (View.INVISIBLE);
                viewModel.getCourierArrivalMutableLiveData ().observe (this, isHere -> {
                    viewModel.loadAllOrders (getString (string.customer));
                    viewModel.getOrders ().observe (this, orders -> {
                        for (Order order : orders) {
                            viewModel.getNotified (order.getOrderId ());
                        }
                    });
                });
                viewModel.getApplicationUserData ().observe (this, applicationUser -> {
                    area = applicationUser.getArea ();
                    if (area.contains ("Kaunas")) {
                        viewModel.loadAllLaundryHouses ();
                        Intent i = new Intent (activity_home.this, activity_createBasket.class);
                        viewModel.getLaundryHouses ().observe (this, laundryHouses -> {
                            laundryHousesAdapter = new LaundryHousesAdapter (activity_home.this, laundryHouses);
                            binding.recyclerViewUserhome.setAdapter (laundryHousesAdapter);
                            laundryHousesAdapter.setOnItemClickListener (laundryHouse -> {
                                if (laundryHouse.isActive ()) {
                                    i.putExtra ("laundryhouseuid", laundryHouse.getUid ()).putExtra ("deliveryprice", laundryHouse.getDeliveryprice ())
                                            .putExtra ("authtype", authtype).putExtra ("items", items);
                                    startActivity (i);
                                } else
                                    Toast.makeText (this, "This Laundry House is not active at the moment :(", Toast.LENGTH_SHORT).show ();
                            });
                        });
                        binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                            viewModel.loadAllLaundryHouses ();
                            binding.swiperefreshlayoutHome.setRefreshing (false);
                        });

                    } else {
                        createPopUpWindow (layout.activity_wrongarea);
                        binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                            createPopUpWindow (layout.activity_wrongarea);
                            binding.swiperefreshlayoutHome.setRefreshing (false);
                        });
                    }
                });
                break;
            case "Laundry House":
                viewModel.loadAllOrders (getString (string.laundryhouse));
                viewModel.getOrders ().observe (this, orders -> {
                    ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                    binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                    ordersAdapter.onItemSelectedListenerCustom (status ->
                            ordersAdapter.setOnOrderClickListener (order -> {
                                if (!status.equals ("")) {
                                    if (status.equals ("In Process") || status.equals ("Completed"))
                                        viewModel.unassignOrder (order.getOrderId ());
                                    viewModel.updateOrderStatus (authtype, status, order.getOrderId ());
                                    viewModel.loadAllOrders (authtype);
                                } else
                                    Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
                            }));
                    ordersAdapter.setOnAssignClickListener (order -> {
                        View popupWindowView = createPopUpWindow (layout.activity_assigncouriers);
                        SwipeRefreshLayout swipeRefreshLayout = popupWindowView.findViewById (id.swiperefreshlayout_assignorder);
                        viewModel.loadAllCouriers ();
                        swipeRefreshLayout.setOnRefreshListener (() -> {
                            viewModel.loadAllCouriers ();
                            swipeRefreshLayout.setRefreshing (false);
                        });
                        RecyclerView recyclerView = popupWindowView.findViewById (id.recyclerview_assigncouriers);
                        recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                        viewModel.getCouriers ().observe (this, couriers -> {
                            for (Courier courier : couriers) {
                                if (courier.isActive ())
                                    couriers.remove (courier);
                            }
                            couriersAdapter = new CouriersAdapter (this, couriers);
                            recyclerView.setAdapter (couriersAdapter);
                            couriersAdapter.onItemCourierListener (courier -> {
                                viewModel.assignOrder (courier.getUid (), order.getOrderId ());
                                window.dismiss ();
                            });
                        });
                    });
                });
                binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                    viewModel.loadAllOrders (getString (string.laundryhouse));
                    binding.swiperefreshlayoutHome.setRefreshing (false);
                });
                break;
            case "Courier":
                binding.imageButtonMap.setVisibility (View.INVISIBLE);
                binding.imageButtonOrderhistory.setVisibility (View.INVISIBLE);
                viewModel.loadAllOrders (getString (string.courier));
                viewModel.getOrders ().observe (this, orders -> {
                    ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                    binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                    ordersAdapter.onItemSelectedListenerCustom (status -> {
                        ordersAdapter.setOnOrderClickListener (order -> {
                            if (status.equals ("")) {
                                Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
                            } else {
                                if (status.equals ("In Process") || status.equals ("Completed"))
                                    viewModel.unassignOrder (order.getOrderId ());
                                viewModel.updateOrderStatus (authtype, status, order.getOrderId ());
                                viewModel.loadAllOrders (authtype);
                            }
                        });
                        ordersAdapter.setOnAssignClickListener (order -> {
                            viewModel.getUserAndLaundryHouseMarkerLocation (order.getOrderId ());
                            viewModel.getLatLngMutableLiveData ().observe (this, latLngList -> {
                                if (latLngList.size () > 1) {
                                    iCourier.putExtra ("LaundryHouseLatLng", latLngList.get (1));
                                    iCourier.putExtra ("CustomerLatLng", latLngList.get (0));
                                    getPermissions ();
                                }
                            });
                        });
                    });
                    if (orders.size () != 0) {
                        binding.buttonArrivedCourierAtCustomer.setVisibility (View.VISIBLE);
                        binding.buttonArrivedCourierAtLaundryHouse.setVisibility (View.VISIBLE);
                        String[] check = orders.get (0).getOrderId ().split ("_");
                        binding.buttonArrivedCourierAtCustomer.setOnClickListener (view -> viewModel.notifyOfArrival (orders.get (0).getOrderId (), check[0], "Courier Arrival", String.format ("Arrived at Customer location\n" +
                                "-For Pick Up-%s", orders.get (0).getOrderId ())));
                        binding.buttonArrivedCourierAtLaundryHouse.setOnClickListener (view -> viewModel.notifyOfArrival (orders.get (0).getOrderId (), check[2], "Courier Arrival", String.format ("Arrived at LaundryHouse location\n" +
                                "OrderId-%s", orders.get (0).getOrderId ())));
                    }
                });
                binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                    viewModel.loadAllOrders (getString (string.courier));
                    binding.swiperefreshlayoutHome.setRefreshing (false);
                });
                break;
            default:
                Toast.makeText (this, "There is no Data to show", Toast.LENGTH_SHORT).show ();
        }
        viewModel.getCurrentSignInUser ().observe (this, firebaseUser ->
                binding.txtUserGreeting.setText (String.format ("Hello, %s", firebaseUser.getEmail ())));
        binding.imageButtonProfile.setOnClickListener (view ->
                startActivity (new Intent (activity_home.this, activity_profile.class)
                        .putExtra ("authtype", authtype)
                        .putStringArrayListExtra ("items", items)));
        binding.imageButtonOrderhistory.setOnClickListener (view ->
                startActivity (new Intent (activity_home.this, activity_orderHistory.class)
                        .putExtra ("authtype", authtype)
                        .putStringArrayListExtra ("items", items)));
        binding.imageButtonMap.setOnClickListener (view ->
                startActivity (new Intent (activity_home.this, activity_orderHistory.class)
                        .putExtra ("authtype", authtype)
                        .putExtra ("entry", "track")
                        .putStringArrayListExtra ("items", items)));
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                startActivity (new Intent (activity_home.this, activity_home.class)
                        .putExtra ("authtype", authtype).putStringArrayListExtra ("items", items));
            }
        };
        activity_home.this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else {
            startActivity (iCourier);
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

    private View createPopUpWindow (int layout) {
        View popupWindowView = LayoutInflater.from (activity_home.this).inflate (layout, null);
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
}
