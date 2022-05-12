package com.example.laundry2.View;

import static com.example.laundry2.R.id;
import static com.example.laundry2.R.layout;
import static com.example.laundry2.R.string;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import androidx.appcompat.app.AlertDialog;
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
import com.example.laundry2.ExpressoIdlingResource;
import com.example.laundry2.databinding.ActivityHomeUserBinding;


public class activity_home extends AppCompatActivity {

    private AuthenticationViewModel viewModel;
    private ActivityHomeUserBinding binding;
    private LaundryHousesAdapter laundryHousesAdapter;
    private OrdersAdapter ordersAdapter;
    private CouriersAdapter couriersAdapter;
    private PopupWindow window;
    private String area;
    private Intent startMap;
    private String applicationUserId;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, layout.activity_home_user);
        binding.recyclerViewUserhome.setLayoutManager (new LinearLayoutManager (activity_home.this));
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        startMap = new Intent (activity_home.this, activity_maps.class);
        viewModel.getState ().observe (this, authState -> {
            if (authState.isValid ()) {
                Toast.makeText (getApplicationContext (), authState.getType (), Toast.LENGTH_SHORT).show ();
            }
        });
        viewModel.getAuthType ().observe (this, authType -> {
            if (authType != null) {
                String authtype = authType.authtype;
                viewModel.getCurrentSignInUser ().observe (this, user -> {
                    applicationUserId = user.getUid ();
                    viewModel.checkIsForProfileCompleted (authtype, applicationUserId);
                });
                viewModel.getState ().observe (this, authState -> {
                    if (!authState.isValid () && authState.getType ().equals ("User Data Failed to load")) {
                        startActivity (new Intent (activity_home.this, activity_profile.class));
                        Toast.makeText (this, "You must complete Profile before using application", Toast.LENGTH_SHORT).show ();
                    }
                });
                mapsOnclick (authtype);
                if (binding.switchActivestatus.isChecked ())
                    binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
                else binding.switchActivestatus.setText (getString (string.switch_to_go_online));
                viewModel.getCurrentSignInUser ().observe (this, user -> {
                    viewModel.loadApplicationUserData (authtype, applicationUserId);
                    binding.switchActivestatus.setOnCheckedChangeListener ((compoundButton, b) -> {
                        viewModel.changeActiveStatus (b, authtype, applicationUserId);
                        if (binding.switchActivestatus.isChecked ())
                            binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
                        else
                            binding.switchActivestatus.setText (getString (string.switch_to_go_online));
                    });
                });
                viewModel.getApplicationUserData ().observe (this, applicationUser -> {
                    binding.txtUserGreeting.setText (String.format ("Hello, %s", applicationUser.getName ()));
                    binding.switchActivestatus.setChecked (applicationUser.isActive ());
                    area = applicationUser.getArea ();
                });

                viewModel.loadAllOrders (authtype, applicationUserId, false);
                switch (authtype) {
                    case "Customer":
                        if (getIntent ().hasExtra ("fromNotification"))
                            sendNotificationResponse (authtype);
                        binding.switchActivestatus.setVisibility (View.INVISIBLE);
                        viewModel.getApplicationUserData ().observe (this, applicationUser -> {
                            area = applicationUser.getArea ();
                            if (area.contains ("Kaunas")) {
                                viewModel.loadAllLaundryHouses (applicationUserId);
                                viewModel.getCourierArrivalMutableLiveData ().observe (this, isHere ->
                                        viewModel.getOrders ().observe (this, orders -> {
                                    for (Order order : orders) {
                                        viewModel.getNotified (order.getOrderId ());
                                    }
                                }));
                                viewModel.getLaundryHouses ().observe (this, laundryHouses -> {
                                    laundryHousesAdapter = new LaundryHousesAdapter (activity_home.this, laundryHouses);
                                    binding.recyclerViewUserhome.setAdapter (laundryHousesAdapter);
                                    laundryHousesAdapter.setOnItemClickListener (laundryHouse -> {
                                        if (laundryHouse.isActive ()) {
                                            startActivity (new Intent (activity_home.this, activity_createBasket.class));
                                            viewModel.insertLaundryHouseCacheData (laundryHouse.getUid (), "" + laundryHouse.getDeliveryPrice ());

                                        } else
                                            Toast.makeText (this, "This Laundry House is not active at the moment :(", Toast.LENGTH_SHORT).show ();
                                    });
                                });
                                binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                                    viewModel.getCurrentSignInUser ().observe (this, user -> viewModel.loadAllLaundryHouses (user.getUid ()));
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
                        if (getIntent ().hasExtra ("fromNotification"))
                            sendNotificationResponse (authtype);
                        ExpressoIdlingResource.increment ();
                        viewModel.getOrders ().observe (this, orders -> {
                            ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                            viewModel.getCourierArrivalMutableLiveData ().observe (this, isHere -> {
                                for (Order order : orders)
                                    viewModel.getNotified (order.getOrderId ());
                            });
                            if (ordersAdapter.getOrders ().size () != 0) {
                                binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                                ExpressoIdlingResource.decrement ();
                            }
                            ordersAdapter.onItemSelectedListenerCustom (status ->
                                    ordersAdapter.setOnOrderClickListener (order -> {
                                        if (!status.equals ("Order Status")) {
                                            if (status.equals ("In Process") || status.equals ("Completed"))
                                                viewModel.unassignOrder (order.getOrderId ());
                                            viewModel.updateOrderStatus (status, order.getOrderId ());
                                        } else
                                            Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
                                    }));
                            ordersAdapter.setOnAssignClickListener (order -> {
                                if (order.getDeliveryCost () > 1) {
                                    View popupWindowView = createPopUpWindow (layout.activity_assigncouriers);
                                    SwipeRefreshLayout swipeRefreshLayout = popupWindowView.findViewById (id.swiperefreshlayout_assignorder);
                                    viewModel.loadAllCouriers (order.getOrderId ());
                                    swipeRefreshLayout.setOnRefreshListener (() -> {
                                        viewModel.loadAllCouriers (order.getOrderId ());
                                        swipeRefreshLayout.setRefreshing (false);
                                    });
                                    RecyclerView recyclerView = popupWindowView.findViewById (id.recyclerview_assigncouriers);
                                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                                    viewModel.getCouriers ().observe (this, couriers -> {
                                        for (Courier courier : couriers) {
                                            if (!courier.isActive ())
                                                couriers.remove (courier);
                                        }
                                        couriersAdapter = new CouriersAdapter (this, couriers);
                                        recyclerView.setAdapter (couriersAdapter);
                                        couriersAdapter.onItemCourierListener (courier -> {
                                            viewModel.assignOrder (courier.getUid (), order.getOrderId ());
                                            window.dismiss ();
                                        });
                                    });
                                } else
                                    Toast.makeText (this, "This order does not require a courier,\n" +
                                            "Customer will come to Laundry House", Toast.LENGTH_SHORT).show ();
                            });
                        });
                        binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                            viewModel.loadAllOrders (authtype, applicationUserId, false);
                            binding.swiperefreshlayoutHome.setRefreshing (false);
                        });
                        break;
                    case "Courier":
                        binding.imageButtonOrderhistory.setVisibility (View.INVISIBLE);
                        viewModel.getOrders ().observe (this, orders -> {
                            ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                            binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                            ordersAdapter.onItemSelectedListenerCustom (status -> {
                                ordersAdapter.setOnOrderClickListener (order -> {
                                    if (status.equals ("Order Status")) {
                                        Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
                                    } else {
                                        if (status.equals ("In Process") || status.equals ("Completed"))
                                            viewModel.unassignOrder (order.getOrderId ());
                                        viewModel.updateOrderStatus (status, order.getOrderId ());
                                        viewModel.loadAllOrders (authtype, applicationUserId, false);
                                    }
                                });
                                ordersAdapter.setOnAssignClickListener (order -> {
                                    viewModel.getUserAndLaundryHouseMarkerLocation (order.getOrderId ());
                                    viewModel.getLatLngMutableLiveData ().observe (this, latLngList -> {
                                        if (latLngList.size () > 1) {
                                            getPermissions ();
                                        }
                                    });
                                });
                            });
                        });
                        binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                            viewModel.loadAllOrders (authtype, applicationUserId, false);
                            binding.swiperefreshlayoutHome.setRefreshing (false);
                        });
                        break;
                    default:
                        Toast.makeText (this, "There is no Data to show", Toast.LENGTH_SHORT).show ();
                }
            }
            binding.imageButtonProfile.setOnClickListener (view ->
                    startActivity (new Intent (activity_home.this, activity_profile.class)));
            binding.imageButtonOrderhistory.setOnClickListener (view ->
                    startActivity (new Intent (activity_home.this, activity_orderHistory.class)));
            OnBackPressedCallback callback = new OnBackPressedCallback (true) {
                @Override
                public void handleOnBackPressed () {
                    System.exit (0);
                }
            };
            activity_home.this.getOnBackPressedDispatcher ().addCallback (callback);
        });
    }


    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else startActivity (startMap);
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

    private void mapsOnclick (String authtype) {
        if (authtype.equals (getString (string.courier)))
            binding.imageButtonMap.setOnClickListener (view -> getPermissions ());
        else {
            binding.imageButtonMap.setOnClickListener (view -> {
                startActivity (new Intent (activity_home.this, activity_orderHistory.class));
                viewModel.insertIsOrderTrackingData (this.getString (string.isordertracking));
            });
        }
    }

    private void sendNotificationResponse (String authtype) {
        binding.confirmOrderWindow.setVisibility (View.VISIBLE);
        binding.recyclerViewUserhome.setVisibility (View.INVISIBLE);
        String orderId = getIntent ().getStringExtra ("orderId");
        String type = getIntent ().getStringExtra ("type");
        binding.textViewNotification.setText (String.format ("%s %s", binding.textViewNotification.getText (), type));
        binding.checkboxYes.setOnClickListener (view -> {
            viewModel.changeOrderPickDropStatus (orderId, authtype, type, true);
            Toast.makeText (this, "Confirmed! Thank you for your response", Toast.LENGTH_SHORT).show ();
            binding.confirmOrderWindow.setVisibility (View.INVISIBLE);
            binding.recyclerViewUserhome.setVisibility (View.VISIBLE);
        });
        binding.checkboxNo.setOnClickListener (view -> {
            viewModel.changeOrderPickDropStatus (orderId, authtype, type, false);
            binding.confirmOrderWindow.setVisibility (View.INVISIBLE);
            binding.recyclerViewUserhome.setVisibility (View.VISIBLE);
        });
    }

    @Override
    protected void onStart () {
        super.onStart ();
        createDialog (isInternetConnection ());
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
        }
    }
}
