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

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, layout.activity_home_user);
        binding.recyclerViewUserhome.setLayoutManager (new LinearLayoutManager (activity_home.this));
        viewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        viewModel.getState ().observe (this, authState -> {
            if (authState.isValid ()) {
                Toast.makeText (getApplicationContext (), authState.getType (), Toast.LENGTH_SHORT).show ();
            }
        });
        iCourier = new Intent (activity_home.this, activity_maps.class)
                .putExtra ("authtype", getIntent ().getStringExtra ("authtype"));
        if (binding.switchActivestatus.isChecked ())
            binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
        else binding.switchActivestatus.setText (getString (string.switch_to_go_online));
        viewModel.loadApplicationUserData (getIntent ().getExtras ().getString ("authtype"));
        viewModel.getApplicationUserData ().observe (this, applicationUser -> {
            binding.switchActivestatus.setChecked (applicationUser.isActive ());
            area = applicationUser.getArea ();
        });
        viewModel.getCurrentSignInUser ().observe (this, user -> applicationUserUid = user.getUid ());
        binding.switchActivestatus.setOnCheckedChangeListener ((compoundButton, b) -> {
            viewModel.changeActiveStatus (b, getIntent ().getStringExtra ("authtype"), applicationUserUid);
            if (binding.switchActivestatus.isChecked ())
                binding.switchActivestatus.setText (getString (string.switch_to_go_offline));
            else binding.switchActivestatus.setText (getString (string.switch_to_go_online));
        });


        String authtype = getIntent ().getExtras ().getString ("authtype");
        switch (authtype) {
            case "Customer":
                binding.buttonArrivedCourier.setVisibility (View.INVISIBLE);
                binding.switchActivestatus.setVisibility (View.INVISIBLE);
                viewModel.loadAllOrders (getString (string.customer));
                viewModel.getOrders ().observe (this, orders -> {
                    for (Order order : orders) {
                        viewModel.getNotified (order.getOrderId ());
                    }
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
                                    i.putExtra ("laundryhouseuid", laundryHouse.getUid ());
                                    i.putExtra ("deliveryprice", laundryHouse.getDeliveryprice ());
                                    i.putExtra ("authtype", getIntent ().getStringExtra ("authtype"));
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
                binding.buttonArrivedCourier.setVisibility (View.INVISIBLE);
                viewModel.loadAllOrders (getString (string.laundryhouse));
                viewModel.getOrders ().observe (this, orders -> {
                    ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                    binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                    ordersAdapter.onItemSelectedListenerCustom (status ->
                            ordersAdapter.setOnOrderClickListener (order -> {
                                if (!status.equals ("")) {
                                    if (status.equals ("In Process") || status.equals ("Completed"))
                                        viewModel.usassignOrder (order.getCourierId (), order.getOrderId ());
                                    viewModel.updateOrderStatus (status, order.getOrderId ());
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
                                if (!courier.getOrderId ().equals ("") && courier.isActive ())
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
                binding.buttonArrivedCourier.setVisibility (View.INVISIBLE);
                binding.imageButtonOrderhistory.setVisibility (View.INVISIBLE);
                viewModel.loadAllOrders (getString (string.courier));
                viewModel.getOrders ().observe (this, orders -> {
                    binding.buttonArrivedCourier.setVisibility (View.VISIBLE);
                    ordersAdapter = new OrdersAdapter (activity_home.this, orders, authtype);
                    binding.recyclerViewUserhome.setAdapter (ordersAdapter);
                    ordersAdapter.onItemSelectedListenerCustom (status -> {
                        ordersAdapter.setOnOrderClickListener (order -> {
                            if (!status.equals ("")) {
                                if (status.equals ("In Process") || status.equals ("Completed"))
                                    viewModel.usassignOrder (order.getCourierId (), order.getOrderId ());
                                viewModel.updateOrderStatus (status, order.getOrderId ());
                            } else
                                Toast.makeText (this, "Status cannot be empty", Toast.LENGTH_SHORT).show ();
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
                    binding.buttonArrivedCourier.setOnClickListener (view -> viewModel.notifyOfArrival (orders.get (0).getOrderId (), true));
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
                        .putExtra ("authtype", getIntent ().getExtras ().get ("authtype").toString ())));

        binding.imageButtonOrderhistory.setOnClickListener (view ->
                startActivity (new Intent (activity_home.this, activity_orderHistory.class)
                        .putExtra ("authtype", getIntent ().getExtras ().get ("authtype").toString ())));

        binding.imageButtonMap.setOnClickListener (view ->
                startActivity (new Intent (activity_home.this, activity_orderHistory.class)
                        .putExtra ("authtype", getIntent ().getExtras ().get ("authtype").toString ())
                        .putExtra ("entry", "track")));


        viewModel.getApplicationUserData ().observe (this, applicationUser -> {

        });
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
//
//    private List<Order> createRefinedList (List<Order> orders, int pos) {
//        List<Order> refinedList = new ArrayList<> (orders);
//        for (Order order : orders) {
//            String[] check = order.getOrderId ().split ("_");
//            if (pos == 3) {
//                if (order.getStatus ().equals ("Completed") ||
//                        !order.getCourierId ().equals (viewModel.getCurrentSignInUser ().getValue ().getUid ()))
//                    refinedList.remove (order);
//            } else {
//                if (order.getStatus ().equals ("Completed") ||
//                        !check[pos].equals (viewModel.getCurrentSignInUser ().getValue ().getUid ()))
//                    refinedList.remove (order);
//            }
//        }
//        return refinedList;
//    }
}
