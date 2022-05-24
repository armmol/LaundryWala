package com.example.laundry2.View;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.LaundryBasketViewModel;
import com.example.laundry2.PaymentsViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityCreatebasketBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.wallet.PaymentData;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.config.PaymentButtonIntent;
import com.paypal.checkout.createorder.CreateOrderActions;
import com.paypal.checkout.createorder.CurrencyCode;
import com.paypal.checkout.createorder.OrderIntent;
import com.paypal.checkout.createorder.ProcessingInstruction;
import com.paypal.checkout.createorder.UserAction;
import com.paypal.checkout.order.Amount;
import com.paypal.checkout.order.AppContext;
import com.paypal.checkout.order.Order;
import com.paypal.checkout.order.PurchaseUnit;
import com.paypal.checkout.paymentbutton.PaymentButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class activity_createBasket extends AppCompatActivity {

    private PopupWindow window;
    private RelativeLayout googlePayButton;
    private PaymentButton payPalButton;
    private ActivityCreatebasketBinding binding;
    private LaundryBasketViewModel viewModel;
    private PaymentsViewModel paymentsViewModel;
    private AuthenticationViewModel authenticationViewModel;
    private ActivityResultLauncher<Intent> findAddress;
    private LaundryItemsAdapter adapter;
    private String laundryHouseUid, cost, address;
    private double deliveryCost = 0, dryingCost = 0, staticDelivery = 0;
    private boolean drying;
    private ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_createbasket);
        viewModel = new ViewModelProvider (this).get (LaundryBasketViewModel.class);
        paymentsViewModel = new ViewModelProvider (this).get (PaymentsViewModel.class);
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        findAddress = registerForActivityResult (
                new ActivityResultContracts.StartActivityForResult (), result -> {
                    if (result.getResultCode () == RESULT_OK) {
                        if (result.getData () != null) {
                            Place place = Autocomplete.getPlaceFromIntent (result.getData ());
                            confirmAddress (cost, place.getAddress ());
                            address = place.getAddress ();
                            if (place.getAddress ().split (",")[1].contains ("Kaunas")) {
                                authenticationViewModel.getNewDeliveryCost (place, laundryHouseUid);
                                authenticationViewModel.getNewDeliveryCost ().observe (this, cost -> {
                                    staticDelivery = cost;
                                    deliveryCost = binding.checkBoxINeedACourier.isChecked () ? staticDelivery : 0;
                                    binding.deliverypriceCreatebasket.setText (String.format ("Delivery Cost - %s", deliveryCost));
                                    double costAfterDeliveryChange = 0;
                                    if (viewModel.getCachedItems ().getValue ().size () != 0) {
                                        for (LaundryItemCache item : viewModel.getCachedItems ().getValue ()) {
                                            dryingCost = binding.checkBoxINeedDrying.isChecked () ? 0.16 * viewModel.getCachedItems ().getValue ().size () : 0;
                                            costAfterDeliveryChange += Double.parseDouble (item.getType ().split (",")[1]);
                                        }
                                        binding.txtLaundrybasketcost.setText (String.format ("%s €",
                                                new BigDecimal (costAfterDeliveryChange + deliveryCost + dryingCost).
                                                        setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ()));
                                    }
                                    Toast.makeText (activity_createBasket.this, "Delivery Price was updated", Toast.LENGTH_SHORT).show ();
                                });
                                AtomicBoolean isKaunas = new AtomicBoolean (false);
                                authenticationViewModel.getCurrentSignInUser ().observe (activity_createBasket.this, firebaseUser -> {
                                    authenticationViewModel.loadApplicationUserData (getString (R.string.customer), firebaseUser.getUid ());
                                    authenticationViewModel.getApplicationUserData ().observe (activity_createBasket.this, user -> {
                                        authenticationViewModel.enterIntoDB (firebaseUser.getUid (), firebaseUser.getEmail (), getString (R.string.customer),
                                                user.getName (), place.getAddress (), place.getAddress ().split (",")[1],
                                                place.getLatLng ().latitude, place.getLatLng ().longitude);
                                    });
                                });
                            } else
                                Toast.makeText (activity_createBasket.this, "We do not serve outside Kaunas :(", Toast.LENGTH_SHORT).show ();
                        }
                    } else
                        Toast.makeText (activity_createBasket.this, "Failed Try Again", Toast.LENGTH_SHORT).show ();
                });
        resolvePaymentForResult = registerForActivityResult (
                new ActivityResultContracts.StartIntentSenderForResult (), result -> {
                    switch (result.getResultCode ()) {
                        case Activity.RESULT_OK:
                            Intent resultData = result.getData ();
                            if (resultData != null) {
                                PaymentData paymentData = PaymentData.getFromIntent (result.getData ());
                                if (paymentData != null) {
                                    handleGPayPaymentSuccess (paymentData);
                                }
                            }
                            break;
                        case Activity.RESULT_CANCELED:
                            // The user cancelled the payment attempt
                            Toast.makeText (this, "Payment Cancelled", Toast.LENGTH_SHORT).show ();
                            break;
                    }
                });

        binding.imgbtnShirtsAdd.setOnClickListener (view -> viewModel.addItem ("Shirt"));
        binding.imgbtnPants.setOnClickListener (view -> viewModel.addItem ("Pant"));
        binding.imgbtnSuits.setOnClickListener (view -> viewModel.addItem ("Suit/Blazer/Coat"));
        binding.imgbtnJackets.setOnClickListener (view -> viewModel.addItem ("Jackets/Woolen"));
        binding.imgbtnCarpets.setOnClickListener (view -> viewModel.addItem ("Carpet/Rug"));
        binding.imgbtnBedsheets.setOnClickListener (view -> viewModel.addItem ("Bedsheet/Duvet"));
        binding.imgbtnKgs.setOnClickListener (view -> viewModel.addItem ("Kg"));

        AtomicInteger flag = new AtomicInteger (1);
        authenticationViewModel.getLaundryHouseCacheData ().observe (this, laundryHouseCache -> {
            if (flag.intValue () == 1 && laundryHouseCache != null) {
                laundryHouseUid = laundryHouseCache.getLaundryHouseID ();
                staticDelivery = Double.parseDouble (laundryHouseCache.getDeliveryCost ());
                flag.set (0);
                authenticationViewModel.removeLaundryHouseCacheData ();
            }
        });
        AtomicBoolean addressWatch = new AtomicBoolean (false);
        authenticationViewModel.getCurrentSignInUser ().observe (this, user -> authenticationViewModel.loadApplicationUserData ("Customer", user.getUid ()));
        authenticationViewModel.getApplicationUserData ().observe (this, applicationUser -> {
            if(!addressWatch.get ()){
                address = applicationUser.getAddress ();
            }
        });
        binding.checkBoxINeedACourier.setChecked (false);
        binding.deliverypriceCreatebasket.setText (String.format ("Delivery Cost - %s", deliveryCost));
        binding.checkBoxINeedACourier.setOnCheckedChangeListener ((compoundButton, b) -> {
            deliveryCost = binding.checkBoxINeedACourier.isChecked () ? staticDelivery : 0;
            binding.deliverypriceCreatebasket.setText (String.format ("Delivery Cost - %s", deliveryCost));
            double costAfterDeliveryChange = 0;
            if (viewModel.getCachedItems ().getValue ().size () != 0) {
                for (LaundryItemCache item : viewModel.getCachedItems ().getValue ()) {
                    dryingCost = binding.checkBoxINeedDrying.isChecked () ? 0.16 * viewModel.getCachedItems ().getValue ().size () : 0;
                    costAfterDeliveryChange += Double.parseDouble (item.getType ().split (",")[1]);
                }
                binding.txtLaundrybasketcost.setText (String.format ("%s €",
                        new BigDecimal (costAfterDeliveryChange + deliveryCost + dryingCost).
                                setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ()));
            }
        });

        drying = binding.checkBoxINeedDrying.isChecked ();
        binding.checkBoxINeedDrying.setChecked (false);
        binding.dryingpriceCreatebasket.setText (String.format ("Drying Cost - %s", dryingCost));
        binding.checkBoxINeedDrying.setOnCheckedChangeListener ((compoundButton, b) -> {
            drying = binding.checkBoxINeedDrying.isChecked ();
            deliveryCost = binding.checkBoxINeedACourier.isChecked () ? staticDelivery : 0;
            double costAfterDryingChange = 0;
            if (viewModel.getCachedItems ().getValue ().size () != 0) {
                for (LaundryItemCache item : viewModel.getCachedItems ().getValue ()) {
                    dryingCost = binding.checkBoxINeedDrying.isChecked () ? 0.16 * viewModel.getCachedItems ().getValue ().size () : 0;
                    binding.dryingpriceCreatebasket.setText (String.format ("Drying Cost - %s", dryingCost));
                    costAfterDryingChange += Double.parseDouble (item.getType ().split (",")[1]);
                }
                binding.txtLaundrybasketcost.setText (String.format ("%s €",
                        new BigDecimal (costAfterDryingChange + deliveryCost + dryingCost).
                                setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ()));
            }
        });
        binding.btnConfrimandpayCreatebasket.setOnClickListener (view -> {
            if (binding.txtLaundrybasketcounter.getText ().equals ("0")) {
                Toast.makeText (this, "Nothing in basket", Toast.LENGTH_SHORT).show ();
            }
        });
        viewModel.getCachedItems ().observe (this, laundryItemList -> {
            binding.txtLaundrybasketcounter.setText (MessageFormat.format ("{0}", laundryItemList.size ()));
            //Cost for the payment
            double a = 0;
            int kgs = 0, shirts = 0, pants = 0, suits = 0, jackets = 0, carpets = 0, bedsheets = 0;
            for (LaundryItemCache item : laundryItemList) {
                if (item.getType ().split (",")[0].equals ("Kg")) kgs++;
                if (item.getType ().split (",")[0].equals ("Shirt")) shirts++;
                if (item.getType ().split (",")[0].equals ("Pant")) pants++;
                if (item.getType ().split (",")[0].equals ("Suit/Blazer/Coat")) suits++;
                if (item.getType ().split (",")[0].equals ("Jackets/Woolen")) jackets++;
                if (item.getType ().split (",")[0].equals ("Carpet/Rug")) carpets++;
                if (item.getType ().split (",")[0].equals ("Bedsheet/Duvet")) bedsheets++;
                a += Double.parseDouble (item.getType ().split (",")[1]);
            }
            binding.textViewKgcounter.setText (String.format ("%s Kg", kgs));
            binding.textView6.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.jackets_woolens), jackets));
            binding.textView8.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.bedsheets_duvets), bedsheets));
            binding.textView4.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.suits_blazers_coats), suits));
            binding.textView5.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.pants_any), pants));
            binding.textView3.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.shirts_any), shirts));
            binding.textView7.setText (MessageFormat.format ("{0}\n({1})", getString (R.string.carpets_rugs), carpets));
            if (binding.checkBoxINeedDrying.isChecked ()) {
                dryingCost = 0.16 * laundryItemList.size ();
            }
            binding.dryingpriceCreatebasket.setText (String.format ("Drying Cost - %s", dryingCost));
            a += deliveryCost + dryingCost;
            cost = "" + new BigDecimal (a).setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ();
            if (laundryItemList.size () != 0)
                binding.txtLaundrybasketcost.setText (String.format ("%s €", cost));
            else
                binding.txtLaundrybasketcost.setText ("0 €");
            binding.cardLaundrybasket.setOnClickListener (view -> {
                if (laundryItemList.size () != 0) {
                    View popupWindowView = createPopUpWindow (R.layout.activity_viewitems);
                    ConstraintLayout layout = popupWindowView.findViewById (R.id.viewItemsConstraintLayout);
                    layout.setOnClickListener (mView -> window.dismiss ());
                    RecyclerView recyclerView = popupWindowView.findViewById (R.id.recyclerView_confirmorder);
                    TextView clearAll = popupWindowView.findViewById (R.id.textView_clearBasket);
                    clearAll.setVisibility (View.VISIBLE);
                    clearAll.setOnClickListener (view1 -> {
                        viewModel.clearBasket ();
                        Toast.makeText (this, "Basket Cleared", Toast.LENGTH_SHORT).show ();
                        window.dismiss ();
                    });
                    adapter = new LaundryItemsAdapter (this, laundryItemList, 0);
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (adapter);
                    adapter.setOnItemClickListener (laundryItem -> viewModel.removeItem (laundryItem));
                } else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });

            binding.btnConfrimandpayCreatebasket.setOnClickListener (view -> {
                if (laundryItemList.size () == 0) {
                    Toast.makeText (this, "Nothing in basket", Toast.LENGTH_SHORT).show ();
                } else
                    confirmAddress (binding.txtLaundrybasketcost.getText ().toString ().split (" ")[0], address);
            });
        });
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                startActivity (new Intent (activity_createBasket.this, activity_home.class));
                authenticationViewModel.removeLaundryHouseCacheData ();
            }
        };
        activity_createBasket.this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void paymentOnclick (String cost) {
        View popupWindowView = createPopUpWindow (R.layout.activtiy_payments);
        ConstraintLayout layout = popupWindowView.findViewById (R.id.paymentsConstraintLayout);
        layout.setOnClickListener (view -> window.dismiss ());
        payPalButton = popupWindowView.findViewById (R.id.paypalbtn);
        payPalRequestPayment (cost);
        googlePayButton = popupWindowView.findViewById (R.id.btn_Gpay);
        googlePayButton.setOnClickListener (view -> requestPayment (cost));
        paymentsViewModel.canPayWithGooglePay ();
        paymentsViewModel.get_canUseGooglePay ().observe (this, this::setGooglePayAvailable);
    }

    public void payPalRequestPayment (String cost) {
        CheckoutConfig config = new CheckoutConfig (getApplication (),
                "AcQbITXPS5RpHeSWljf4ujWlgeUcWaR020JBGso7hHv7NLeZcDoMyeZS26ZIkr8dKNfv1JbXPVPLUZpj", Environment.SANDBOX,
                "com.example.laundry2" + "://paypalpay", CurrencyCode.EUR, UserAction.PAY_NOW, PaymentButtonIntent.CAPTURE);
        PayPalCheckout.setConfig (config);
        payPalButton.setup (
                createOrderActions -> {
                    ArrayList<PurchaseUnit> purchaseUnits = new ArrayList<> ();
                    purchaseUnits.add (
                            new PurchaseUnit.Builder ().amount (new Amount.Builder ()
                                    .currencyCode (CurrencyCode.EUR).value (cost).build ()).build ());
                    Order order = new Order (OrderIntent.CAPTURE, new AppContext.Builder ()
                            .userAction (UserAction.PAY_NOW).build (), purchaseUnits, ProcessingInstruction.NO_INSTRUCTION);
                    createOrderActions.create (order, (CreateOrderActions.OnOrderCreated) null);
                }, approval -> approval.getOrderActions ().capture (result -> {
                    authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
                            viewModel.createOrder (user.getUid (), laundryHouseUid, deliveryCost, drying));
                    viewModel.orderPlacementStatus ().observe (this, orderStatus -> {
                        Toast.makeText (this, orderStatus.getType (), Toast.LENGTH_SHORT).show ();
                        if (orderStatus.isValid ()) {
                            viewModel.clearLaundryItemCache ();
                            authenticationViewModel.removeLaundryHouseCacheData ();
                            activity_createBasket.this.startActivity (new Intent (activity_createBasket.this, activity_home.class));
                        }
                        Log.i ("CaptureOrder", String.format ("CaptureOrderResult: %s", result));
                    });
                }), (shippingChangeData, shippingChangeActions) -> Toast.makeText (this, "Shipping Address for PayPal was changed", Toast.LENGTH_SHORT).show (),
                () -> Toast.makeText (this, "Payment Cancelled", Toast.LENGTH_SHORT).show (),
                errorInfo -> Toast.makeText (this, "Error during payment", Toast.LENGTH_SHORT).show ());
    }

    public void confirmAddress (String cost,String Naddress) {
//        authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
//                authenticationViewModel.loadApplicationUserData (getString (R.string.customer), user.getUid ()));
        View popupWindowView = createPopUpWindow (R.layout.activity_confirmaddress);
        ConstraintLayout layout = popupWindowView.findViewById (R.id.confirmaddressConstraintlayout);
        layout.setOnClickListener (view -> window.dismiss ());
        Button confirm, change;
        TextView txt;
        txt = popupWindowView.findViewById (R.id.textView_confirmaddress);
        txt.setText (Naddress);
        confirm = popupWindowView.findViewById (R.id.button_confirmaddress_confirm);
        change = popupWindowView.findViewById (R.id.button_confirmaddress_change);
//        authenticationViewModel.getApplicationUserData ().observe (this, user ->
//                txt.setText (user.getAddress ()));
        change.setOnClickListener (view -> {
            List<Place.Field> fieldList = Arrays.asList (Place.Field.ADDRESS, Place.Field.LAT_LNG, Place.Field.NAME);
            findAddress.launch (new Autocomplete.IntentBuilder (AutocompleteActivityMode.OVERLAY
                    , fieldList).build (activity_createBasket.this));
            window.dismiss ();
        });
        confirm.setOnClickListener (view -> {
            window.dismiss ();
            paymentOnclick (cost);
        });
    }

    public void requestPayment (String cost) {
        googlePayButton.setClickable (false);
        String[] euroToCents = cost.split ("\\.");
        if (euroToCents[1].length () > 1)
            cost = euroToCents[0] + euroToCents[1];
        else
            cost = euroToCents[0] + euroToCents[1] + "0";
        long totalPriceCents = Long.parseLong (cost);
        paymentsViewModel.loadPaymentDataForGPay (totalPriceCents);
        paymentsViewModel.getPaymentDataTaskMutableLiveData ().observe (this, task ->
                task.addOnCompleteListener (completedTask -> {
                    if (completedTask.isSuccessful ()) {
                        handleGPayPaymentSuccess (completedTask.getResult ());
                    } else {
                        Exception exception = completedTask.getException ();
                        if (exception instanceof ResolvableApiException) {
                            PendingIntent resolution = ((ResolvableApiException) exception).getResolution ();
                            resolvePaymentForResult.launch (new IntentSenderRequest.Builder (resolution).build ());
                        } else if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            handleError (apiException.getStatusCode (), apiException.getMessage ());
                        } else {
                            handleError (CommonStatusCodes.INTERNAL_ERROR, "Unexpected non API" +
                                    " exception when trying to deliver the task result to an activity!");
                        }
                    }
                    googlePayButton.setClickable (true);
                }));
    }

    private void setGooglePayAvailable (boolean available) {
        if (available) {
            googlePayButton.setVisibility (View.VISIBLE);
        } else {
            Toast.makeText (this, R.string.googlepay_status_unavailable, Toast.LENGTH_LONG).show ();
        }
    }

    private void handleGPayPaymentSuccess (PaymentData paymentData) {
        final String paymentInfo = paymentData.toJson ();
        try {
            JSONObject paymentMethodData = new JSONObject (paymentInfo).getJSONObject ("paymentMethodData");
            final JSONObject info = paymentMethodData.getJSONObject ("info");
            final String billingName = info.getJSONObject ("billingAddress").getString ("name");
            Toast.makeText (
                    this, getString (R.string.payments_show_name, billingName),
                    Toast.LENGTH_LONG).show ();
            Log.d ("Google Pay token", paymentMethodData
                    .getJSONObject ("tokenizationData")
                    .getString ("token"));
            authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
                    viewModel.createOrder (user.getUid (), laundryHouseUid, deliveryCost, drying));
            viewModel.orderPlacementStatus ().observe (this, authState -> {
                viewModel.clearLaundryItemCache ();
                authenticationViewModel.removeLaundryHouseCacheData ();
                startActivity (new Intent (activity_createBasket.this, activity_home.class));
            });
        } catch (JSONException e) {
            Log.e ("handlePaymentSuccess", "Error: " + e);
        }
    }

    private void handleError (int statusCode, @Nullable String message) {
        Log.e ("loadPaymentData failed",
                String.format (Locale.getDefault (), "Error code: %d, Message: %s", statusCode, message));
    }

    private View createPopUpWindow (int layout) {
        View popupWindowView = LayoutInflater.from (activity_createBasket.this).inflate (layout, null);
        window = new PopupWindow (popupWindowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (popupWindowView, Gravity.CENTER, 0, 0);
        return popupWindowView;
    }

    @Override
    protected void onResume () {
        super.onResume ();
        authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
                authenticationViewModel.loadApplicationUserData (getString (R.string.customer), user.getUid ()));
    }

    @Override
    protected void onStart () {
        super.onStart ();
        authenticationViewModel.getCurrentSignInUser ().observe (this, user ->
                authenticationViewModel.loadApplicationUserData (getString (R.string.customer), user.getUid ()));
    }
}