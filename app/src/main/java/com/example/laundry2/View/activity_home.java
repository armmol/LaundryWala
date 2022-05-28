package com.example.laundry2.View;

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
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.laundry2.Adapters.CouriersAdapter;
import com.example.laundry2.Adapters.LaundryHousesAdapter;
import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.Adapters.OrdersAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.EspressoIdlingResource;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityHomeUserBinding;

import java.util.ArrayList;
import android.graphics.Color;


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

        binding = DataBindingUtil.setContentView (this, R.layout.activity_home_user);
        binding.recyclerViewUserhome.setLayoutManager (new LinearLayoutManager (activity_home.this));
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        startMap = new Intent (activity_home.this, activity_maps.class);
        viewModel.getState ().observe (this, authState -> {
            if (authState.isValid () && !authState.getType ().equals ("Active Status changed successfully")) {
                Toast.makeText (getApplicationContext (), authState.getType (), Toast.LENGTH_SHORT).show ();
            }
        });
        viewModel.getCurrentSignInUser ().observe (this, user -> {
            applicationUserId = user.getUid ();
            viewModel.checkIsForProfileCompleted ("", applicationUserId);
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
                        Toast.makeText (this, "Complete Profile before using application! \n If you want to change your user type logout and sign up again", Toast.LENGTH_LONG).show ();
                    }
                });
                mapsOnclick (authtype);
                if (binding.switchActivestatus.isChecked ()) {
                    binding.switchActivestatus.setText (getString (R.string.switch_to_go_offline));
                    binding.switchActivestatus.setTextColor (Color.RED);
                } else {
                    binding.switchActivestatus.setTextColor (Color.GREEN);
                    binding.switchActivestatus.setText (getString (R.string.switch_to_go_online));
                }
                binding.switchActivestatus.setOnCheckedChangeListener ((compoundButton, b) -> {
                    viewModel.changeActiveStatus (b, authtype, applicationUserId);
                    if (binding.switchActivestatus.isChecked ()) {
                        binding.switchActivestatus.setText (getString (R.string.switch_to_go_offline));
                        binding.switchActivestatus.setTextColor (Color.RED);
                    } else {
                        binding.switchActivestatus.setText (getString (R.string.switch_to_go_online));
                        binding.switchActivestatus.setTextColor (Color.GREEN);
                    }
                });
                binding.switchActivestatus.setOnClickListener (view -> {
                    Toast.makeText (this, "Active Status changed successfully", Toast.LENGTH_SHORT).show ();
                    if (binding.switchActivestatus.isChecked ()) {
                        binding.switchActivestatus.setText (getString (R.string.switch_to_go_offline));
                        binding.switchActivestatus.setTextColor (Color.RED);
                    } else {
                        binding.switchActivestatus.setText (getString (R.string.switch_to_go_online));
                        binding.switchActivestatus.setTextColor (Color.GREEN);
                    }
                });
                viewModel.getCurrentSignInUser ().observe (this, user ->
                        viewModel.loadApplicationUserData (authtype, applicationUserId));
                viewModel.getApplicationUserData ().observe (this, applicationUser -> {
                    binding.txtUserGreeting.setText (String.format ("Hello, %s", applicationUser.getName ()));
                    binding.switchActivestatus.setChecked (applicationUser.isActive ());
                    area = applicationUser.getArea ();
                });

                viewModel.loadAllOrders (authtype, applicationUserId, false);
                switch (authtype) {
                    case "Customer":
                        if (getIntent ().hasExtra ("fromNotification")) {
                            sendNotificationResponse (authtype);
                            binding.progressBarHome.setVisibility (View.INVISIBLE);
                        }
                        EspressoIdlingResource.increment ();
                        viewModel.laundryHousesUpdate (applicationUserId);
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
                                    EspressoIdlingResource.decrement ();
                                    binding.progressBarHome.setVisibility (View.INVISIBLE);
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
                                createPopUpWindow (R.layout.activity_wrongarea);
                                binding.swiperefreshlayoutHome.setOnRefreshListener (() -> {
                                    createPopUpWindow (R.layout.activity_wrongarea);
                                    binding.swiperefreshlayoutHome.setRefreshing (false);
                                });
                            }
                        });
                        break;
                    case "Laundry House":
                        if (getIntent ().hasExtra ("fromNotification")) {
                            sendNotificationResponse (authtype);
                            binding.progressBarHome.setVisibility (View.INVISIBLE);
                        }
                        EspressoIdlingResource.increment ();
                        viewModel.orderIDChange (authtype, applicationUserId);
                        viewModel.getOrders ().observe (this, orders -> {
                            ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                            viewModel.getCourierArrivalMutableLiveData ().observe (this, isHere -> {
                                for (Order order : orders)
                                    viewModel.getNotified (order.getOrderId ());
                            });
                            if (ordersAdapter.getOrders ().size () != 0) {
                                binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                                binding.progressBarHome.setVisibility (View.INVISIBLE);
                                EspressoIdlingResource.decrement ();
                            }
                            ordersAdapter.onItemSelectedListenerCustom (status ->
                                    ordersAdapter.setOnOrderClickListener (order -> {
                                        if (!status.equals ("Select Order Status")) {
                                            if (status.equals ("In Process") || status.equals ("Completed"))
                                                viewModel.unassignOrder (order.getOrderId ());
                                            viewModel.updateOrderStatus (status, order.getOrderId ());
                                        } else
                                            Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
                                    }));
                            ordersAdapter.setOnAssignClickListener (order -> {
                                if (order.getDeliveryCost () > 1) {
                                    View popupWindowView = createPopUpWindow (R.layout.activity_assigncouriers);
                                    SwipeRefreshLayout swipeRefreshLayout = popupWindowView.findViewById (R.id.swiperefreshlayout_assignorder);
                                    viewModel.loadAllCouriers (order.getOrderId ());
                                    swipeRefreshLayout.setOnRefreshListener (() -> {
                                        viewModel.loadAllCouriers (order.getOrderId ());
                                        swipeRefreshLayout.setRefreshing (false);
                                    });
                                    RecyclerView recyclerView = popupWindowView.findViewById (R.id.recyclerview_assigncouriers);
                                    ConstraintLayout layout = popupWindowView.findViewById (R.id.assignOrdersConstraintLayout);
                                    layout.setOnClickListener (view -> window.dismiss ());
                                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                                    viewModel.getCouriers ().observe (this, couriers -> {
                                        ArrayList<Courier> newList = new ArrayList<> (couriers);
                                        for (Courier courier : couriers)
                                            if (!courier.isActive ())
                                                newList.remove (courier);
                                        couriersAdapter = new CouriersAdapter (this, newList);
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
                        viewModel.orderIDChange (authtype, applicationUserId);
                        viewModel.getOrders ().observe (this, orders -> {
                            ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                            binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                            binding.progressBarHome.setVisibility (View.INVISIBLE);
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
                                ordersAdapter.setOnAssignClickListener (this::onItemCheckClick);
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
        });
        binding.backButtonHome.setOnClickListener (view -> exit ());
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                exit ();
            }
        };
        activity_home.this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void exit () {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void getPermissions () {
        if (ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission (this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions (this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 131);
        } else startActivity (startMap);
    }

    @Override
    public void onRequestPermissionsResult (int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult (requestCode, permissions, grantResults);
        if (requestCode == 131) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getPermissions ();
            else Toast.makeText (this, "Permission denied", Toast.LENGTH_SHORT).show ();
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
        if (authtype.equals (getString (R.string.courier)))
            binding.imageButtonMap.setOnClickListener (view -> getPermissions ());
        else {
            binding.imageButtonMap.setOnClickListener (view -> {
                startActivity (new Intent (activity_home.this, activity_orderHistory.class));
                viewModel.insertIsOrderTrackingData (this.getString (R.string.isordertracking));
            });
        }
    }

    private void sendNotificationResponse (String authtype) {
        binding.confirmOrderWindow.setVisibility (View.VISIBLE);
        binding.recyclerViewUserhome.setVisibility (View.INVISIBLE);
        String orderId = getIntent ().getStringExtra ("orderId");
        String type = getIntent ().getStringExtra ("type");
        String courierId = getIntent ().getStringExtra ("courierId");
        binding.textViewNotification.setText (String.format ("%s %s", binding.textViewNotification.getText (), type));
        binding.checkboxYes.setOnClickListener (view -> {
            viewModel.changeOrderPickDropStatus (orderId, courierId, authtype, type, true);
            Toast.makeText (this, "Confirmed! Thank you for your response", Toast.LENGTH_SHORT).show ();
            binding.confirmOrderWindow.setVisibility (View.INVISIBLE);
            binding.recyclerViewUserhome.setVisibility (View.VISIBLE);
        });
        binding.checkboxNo.setOnClickListener (view -> {
            viewModel.changeOrderPickDropStatus (orderId, courierId, authtype, type, false);
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

    private void onItemCheckClick (Order order) {
        View windowView = createPopUpWindow (R.layout.activity_viewitems);
        RecyclerView recyclerView = windowView.findViewById (R.id.recyclerView_confirmorder);
        ArrayList<LaundryItemCache> laundryItemCaches = new ArrayList<> ();
        for (LaundryItem laundryItem : order.getItems ())
            laundryItemCaches.add (new LaundryItemCache (laundryItem.getType () + "," + laundryItem.getCost ()));
        LaundryItemsAdapter laundryItemsAdapter = new LaundryItemsAdapter (this, laundryItemCaches, 1);
        recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
        recyclerView.setAdapter (laundryItemsAdapter);
        laundryItemsAdapter.setOnItemClickListener (laundryItem ->
                Toast.makeText (this, "Cannot change order Once placed", Toast.LENGTH_SHORT).show ());
    }
}
