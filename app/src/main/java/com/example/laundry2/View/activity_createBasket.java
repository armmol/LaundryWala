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
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.Toast;

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
import com.example.laundry2.BuildConfig;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.LaundryBasketViewModel;
import com.example.laundry2.PaymentsViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityCreatebasketBinding;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.wallet.PaymentData;
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

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Locale;


public class activity_createBasket extends AppCompatActivity {
    private RelativeLayout googlePayButton;
    private PaymentButton payPalButton;
    private ActivityCreatebasketBinding binding;
    private LaundryBasketViewModel viewModel;
    private PaymentsViewModel paymentsViewModel;
    private LaundryItemsAdapter adapter;
    private String laundryHouseUid;
    private double deliveryprice;
    private final ActivityResultLauncher<IntentSenderRequest> resolvePaymentForResult = registerForActivityResult (
            new ActivityResultContracts.StartIntentSenderForResult (),
            result -> {
                switch (result.getResultCode ()) {
                    case Activity.RESULT_OK:
                        Intent resultData = result.getData ();
                        if (resultData != null) {
                            PaymentData paymentData = PaymentData.getFromIntent (result.getData ());
                            if (paymentData != null) {
                                handlePaymentSuccess (paymentData);
                            }
                        }
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user cancelled the payment attempt
                        Toast.makeText (this, "Payment Cancelled", Toast.LENGTH_SHORT).show ();
                        break;
                }
            });

    public activity_createBasket () {
    }

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_createbasket);
        viewModel = new ViewModelProvider (this).get (LaundryBasketViewModel.class);
        paymentsViewModel = new ViewModelProvider (this).get (PaymentsViewModel.class);
        laundryHouseUid = getIntent ().getStringExtra ("laundryhouseuid");

        binding.imgbtnShirtsAdd.setOnClickListener (view -> viewModel.addItem (1));
        binding.imgbtnPants.setOnClickListener (view -> viewModel.addItem (2));
        binding.imgbtnSuits.setOnClickListener (view -> viewModel.addItem (3));
        binding.imgbtnJackets.setOnClickListener (view -> viewModel.addItem (4));
        binding.imgbtnCarpets.setOnClickListener (view -> viewModel.addItem (5));
        binding.imgbtnBedsheets.setOnClickListener (view -> viewModel.addItem (6));
        deliveryprice = getIntent ().getExtras().getDouble ("deliveryprice", 0.0);
        binding.deliverypriceCreatebasket.setText (String.format ("Delivery Cost - %s",deliveryprice));

        viewModel.getLaundryItems ().observe (this, laundryItemList -> {
            binding.txtLaundrybasketcounter.setText (MessageFormat.format ("{0}", laundryItemList.size ()));
            //Cost for the payment
            double a = 0;
            DecimalFormat df = new DecimalFormat ("0.0");
            for (LaundryItem item : laundryItemList) {
                a += item.getCost ();
            }
            a+= deliveryprice;
            String cost = "" + df.format (a);
            binding.txtLaundrybasketcost.setText (String.format ("%s €", cost));
            binding.cardLaundrybasket.setOnClickListener (view -> {
                if (!binding.txtLaundrybasketcounter.getText () .equals ("0")) {
                    View popupWindowView = createPopUpWindow (R.layout.activity_confirmorder);
                    RecyclerView recyclerView = popupWindowView.findViewById (R.id.recyclerView_confirmorder);
                    adapter = new LaundryItemsAdapter (this, laundryItemList);
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (adapter);
                    adapter.setOnItemClickListener (laundryItem -> viewModel.removeItem (laundryItem.getType ()));
                } else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });
            binding.btnConfrimandpayCreatebasket.setOnClickListener (view -> {
                if (!binding.txtLaundrybasketcounter.getText () .equals ("0"))
                    paymentOnclick (cost);
                else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });
            viewModel.orderPlacementStatus ().observe (this, isPlaced -> {
                if (isPlaced)
                    Toast.makeText (this, "Order Placed Successfully", Toast.LENGTH_SHORT).show ();
                else
                    Toast.makeText (this, "Order could not be Placed", Toast.LENGTH_SHORT).show ();
            });
        });
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
                                    .currencyCode (CurrencyCode.USD).value (cost).build ()).build ());
                    Order order = new Order (OrderIntent.CAPTURE, new AppContext.Builder ()
                            .userAction (UserAction.PAY_NOW).build (), purchaseUnits, ProcessingInstruction.NO_INSTRUCTION);
                    createOrderActions.create (order, (CreateOrderActions.OnOrderCreated) null);
                },
                approval -> approval.getOrderActions ().capture (result -> {
                    viewModel.createOrder (laundryHouseUid);
                    startActivity (new Intent (activity_createBasket.this, activity_home.class)
                            .putExtra ("authtype", getString (R.string.customer)));
                    Log.i ("CaptureOrder", String.format ("CaptureOrderResult: %s", result));
                }));
    }

    public void requestPayment (String cost) {
        googlePayButton.setClickable (false);
        String[] eurotocents = cost.split ("\\.");
        cost = eurotocents[0] + eurotocents[1]+"0";
        long totalPriceCents = Long.parseLong (cost);
        paymentsViewModel.loadPaymentDataForGPay (totalPriceCents);
        paymentsViewModel.getPaymentDataTaskMutableLiveData ().observe (this, task -> task.addOnCompleteListener (completedTask -> {
            if (completedTask.isSuccessful ()) {
                handlePaymentSuccess (completedTask.getResult ());
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

    private void handlePaymentSuccess (PaymentData paymentData) {
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
            viewModel.createOrder (laundryHouseUid);
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
        PopupWindow window = new PopupWindow (popupWindowView);
        window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setFocusable (true);
        window.showAtLocation (popupWindowView, Gravity.CENTER, 0, 0);
        return popupWindowView;
    }
}
