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
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.AuthenticationViewModel;
import com.example.laundry2.BuildConfig;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.LaundryBasketViewModel;
import com.example.laundry2.PaymentsViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityCreatebasketBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.wallet.PaymentData;
import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.approve.OnApprove;
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
import java.util.Locale;


public class activity_createBasket extends AppCompatActivity {

    private PopupWindow window;
    private RelativeLayout googlePayButton;
    private PaymentButton payPalButton;
    private ArrayList<String> items;
    private ActivityCreatebasketBinding binding;
    private LaundryBasketViewModel viewModel;
    private PaymentsViewModel paymentsViewModel;
    private AuthenticationViewModel authenticationViewModel;
    private LaundryItemsAdapter adapter;
    private String laundryHouseUid;
    private double deliveryCost;
    private final ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult (
            new ActivityResultContracts.StartIntentSenderForResult (),
            result -> {
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

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_createbasket);
        viewModel = new ViewModelProvider (this).get (LaundryBasketViewModel.class);
        paymentsViewModel = new ViewModelProvider (this).get (PaymentsViewModel.class);
        authenticationViewModel = new ViewModelProvider (this).get (AuthenticationViewModel.class);
        laundryHouseUid = getIntent ().getStringExtra ("laundryhouseuid");
        items = getIntent ().getStringArrayListExtra ("items");
        if (items != null) {
            for (String item : items)
                viewModel.addItem (item);
        } else items = new ArrayList<> ();
        binding.imgbtnShirtsAdd.setOnClickListener (view -> {
            viewModel.addItem ("Shirt");
            //viewModel.addToCache ("Shirt");
            items.add ("Shirt");
        });
        binding.imgbtnPants.setOnClickListener (view -> {
            viewModel.addItem ("Pant");
            //viewModel.addToCache ("Pant");
            items.add ("Pant");
        });
        binding.imgbtnSuits.setOnClickListener (view -> {
            viewModel.addItem ("Suit/Blazer/Coat");
            //viewModel.addToCache ("Suit/Blazer/Coat");
            items.add ("Suit/Blazer/Coat");
        });
        binding.imgbtnJackets.setOnClickListener (view -> {
            viewModel.addItem ("Jackets/Woolen");
            //viewModel.addToCache ("Jackets/Woolen");
            items.add ("Jackets/Woolen");
        });
        binding.imgbtnCarpets.setOnClickListener (view -> {
            viewModel.addItem ("Carpet/Rug");
            //viewModel.addToCache ("Carpet/Rug");
            items.add ("Carpet/Rug");
        });
        binding.imgbtnBedsheets.setOnClickListener (view -> {
            viewModel.addItem ("Bedsheet/Duvet");
            //viewModel.addToCache ("Bedsheet/Duvet");
            items.add ("Bedsheet/Duvet");
        });
        deliveryCost = getIntent ().getExtras ().getDouble ("deliveryprice", 0.0);
        binding.deliverypriceCreatebasket.setText (String.format ("Delivery Cost - %s", deliveryCost));

        viewModel.getLaundryItems ().observe (this, laundryItemList -> {
            binding.txtLaundrybasketcounter.setText (MessageFormat.format ("{0}", laundryItemList.size ()));
            //Cost for the payment
            double a = 0;
            for (LaundryItem item : laundryItemList) {
                a += item.getCost ();
            }
            a += deliveryCost;
            String cost = "" + new BigDecimal (a).setScale (2, BigDecimal.ROUND_HALF_DOWN).doubleValue ();
            if (laundryItemList.size () != 0)
                binding.txtLaundrybasketcost.setText (String.format ("%s €", cost));
            else
                binding.txtLaundrybasketcost.setText ("0 €");
            binding.cardLaundrybasket.setOnClickListener (view -> {
                if (laundryItemList.size () != 0) {
                    View popupWindowView = createPopUpWindow (R.layout.activity_confirmorder);
                    RecyclerView recyclerView = popupWindowView.findViewById (R.id.recyclerView_confirmorder);
                    adapter = new LaundryItemsAdapter (this, laundryItemList,0);
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (adapter);
                    adapter.setOnItemClickListener (laundryItem -> {
                        viewModel.removeItem (laundryItem.getType ());
                        items.remove (laundryItem.getType ());
                    });
                } else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });
            binding.btnConfrimandpayCreatebasket.setOnClickListener (view -> {
                if (laundryItemList.size () != 0)
                    confirmAddress (cost);
                else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });
            int ordeplacedflag = 0;
            viewModel.orderPlacementStatus ().observe (this, isPlaced -> {

                if (isPlaced && ordeplacedflag==0) {
                    Toast.makeText (this, "Order Placed Successfully", Toast.LENGTH_SHORT).show ();
                }
                else
                    Toast.makeText (this, "Order could not be Placed", Toast.LENGTH_SHORT).show ();
            });
        });
        OnBackPressedCallback callback = new OnBackPressedCallback (true) {
            @Override
            public void handleOnBackPressed () {
                startActivity (new Intent (activity_createBasket.this, activity_home.class)
                        .putExtra ("authtype", getString (R.string.customer)).putStringArrayListExtra ("items", items));
            }
        };
        activity_createBasket.this.getOnBackPressedDispatcher ().addCallback (callback);
    }

    private void paymentOnclick (String cost) {
        View popupWindowView = createPopUpWindow (R.layout.activtiy_payments);
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
                BuildConfig.APPLICATION_ID + "://paypalpay", CurrencyCode.EUR, UserAction.PAY_NOW, PaymentButtonIntent.CAPTURE);
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
                }, (OnApprove) approval -> approval.getOrderActions ().capture (result -> {
                    viewModel.createOrder (laundryHouseUid, deliveryCost);
                    activity_createBasket.this.startActivity (new Intent (activity_createBasket.this, activity_home.class)
                            .putExtra ("authtype", activity_createBasket.this.getString (R.string.customer)));
                    Log.i ("CaptureOrder", String.format ("CaptureOrderResult: %s", result));
                }), (shippingChangeData, shippingChangeActions) -> Toast.makeText (this, "Shipping Address for PayPal was changed", Toast.LENGTH_SHORT).show (),
                () -> Toast.makeText (this, "Payment Cancelled", Toast.LENGTH_SHORT).show (),
                errorInfo -> Toast.makeText (this, "Error during payment", Toast.LENGTH_SHORT).show ());
    }

    public void confirmAddress (String cost) {
        authenticationViewModel.loadApplicationUserData (getString (R.string.customer));
        View popupWindowView = createPopUpWindow (R.layout.activity_confirmaddress);
        Button confirm, change;
        TextView txt;
        txt = popupWindowView.findViewById (R.id.textView_confirmaddress);
        confirm = popupWindowView.findViewById (R.id.button_confirmaddress_confirm);
        change = popupWindowView.findViewById (R.id.button_confirmaddress_change);
        authenticationViewModel.getApplicationUserData ().observe (this, user -> txt.setText (user.getAddress ()));
        change.setOnClickListener (view -> {
            Toast.makeText (this, "Change your address in profile and then come back and continue", Toast.LENGTH_SHORT).show ();
            startActivity (new Intent (activity_createBasket.this, activity_profile.class)
                    .putExtra ("authtype", getString (R.string.customer)).putStringArrayListExtra ("items", items));
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
            viewModel.createOrder (laundryHouseUid, deliveryCost);
            startActivity (new Intent (activity_createBasket.this, activity_home.class)
                    .putExtra ("authtype", getString (R.string.customer)));
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
    protected void onStart () {
        super.onStart ();
        //viewModel.addToCache ("");
        viewModel.getCachedItems ().observe (this, laundryItemCaches -> {
            if (laundryItemCaches != null)
                for (LaundryItemCache laundryItemCache : laundryItemCaches) {
                    viewModel.addItem (laundryItemCache.getType ());
                }
        });
    }
}
