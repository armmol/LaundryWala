package com.example.laundry2.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Adapters.LaundryItemsAdapter;
import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.LaundryBasketViewModel;
import com.example.laundry2.R;
import com.example.laundry2.databinding.ActivityCreatebasketBinding;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.MessageFormat;


public class activity_createBasket extends AppCompatActivity {

    private ActivityCreatebasketBinding binding;
    private LaundryBasketViewModel viewModel;
    private PopupWindow window;
    private LaundryItemsAdapter adapter;
    private String laundryHouseUid;
    private final PayPalConfiguration payPalConfiguration = new PayPalConfiguration ()
            .environment (PayPalConfiguration.ENVIRONMENT_SANDBOX)
            .clientId ("AcUFKfY0Zx38SI7yvmoxbf3UaapoFq_5fbnj-28dJ-x2JpySEAphEbHkjhP-TF6so2b8gN8JmdDRprE0");
    private final ActivityResultLauncher<Intent> payPalActivityResultLauncher = registerForActivityResult (new ActivityResultContracts.StartActivityForResult ()
            , new ActivityResultCallback<ActivityResult> () {
                @Override
                public void onActivityResult (ActivityResult result) {
                    if (result.getResultCode () == RESULT_OK) {
                        PaymentConfirmation confirmation = result.getData ()
                                .getParcelableExtra (PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                        if (confirmation != null) {
                            try {
                                String paymentDetails = confirmation.toJSONObject ().toString (4);
                                Toast.makeText (activity_createBasket.this, String.format ("Payment Confirmed - %s", paymentDetails), Toast.LENGTH_SHORT).show ();
                                viewModel.createOrder (laundryHouseUid);
                                startActivity (new Intent (activity_createBasket.this, activity_home.class)
                                        .putExtra ("authtype", getString (R.string.customer)));
                            } catch (JSONException e) {
                                e.printStackTrace ();
                            }
                        }
                    }
                }
            });

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        binding = DataBindingUtil.setContentView (this, R.layout.activity_createbasket);
        viewModel = new ViewModelProvider (this).get (LaundryBasketViewModel.class);
        laundryHouseUid = getIntent ().getStringExtra ("laundryhouseuid");

        binding.imgbtnShirtsAdd.setOnClickListener (view -> viewModel.addItem (1));
        binding.imgbtnPants.setOnClickListener (view -> viewModel.addItem (2));
        binding.imgbtnSuits.setOnClickListener (view -> viewModel.addItem (3));
        binding.imgbtnJackets.setOnClickListener (view -> viewModel.addItem (4));
        binding.imgbtnCarpets.setOnClickListener (view -> viewModel.addItem (5));
        binding.imgbtnBedsheets.setOnClickListener (view -> viewModel.addItem (6));

        viewModel.getLaundryItems ().observe (this, laundryItemList -> {
            binding.txtLaundrybasketcounter.setText (MessageFormat.format ("{0}", laundryItemList.size ()));
            //Cost for the payment
            double a = 0;
            DecimalFormat df = new DecimalFormat ("0.0");
            for (LaundryItem item : laundryItemList) {
                a += item.getCost ();
            }
            String cost = ""+ df.format (a);
            binding.txtLaundrybasketcost.setText (String.format ("%s €",cost));
            binding.cardLaundrybasket.setOnClickListener (view -> {
                if (binding.txtLaundrybasketcounter.getText () != "0") {
                    View popupWindowView = LayoutInflater.from (activity_createBasket.this).inflate (R.layout.activity_confirmorder, null);
                    window = new PopupWindow (popupWindowView);
                    window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setFocusable (true);
                    window.showAtLocation (popupWindowView, Gravity.CENTER, 0, 0);
                    RecyclerView recyclerView = popupWindowView.findViewById (R.id.recyclerView_confirmorder);

                    adapter = new LaundryItemsAdapter (this, laundryItemList);
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (adapter);
                    adapter.setOnItemClickListener (laundryItem -> viewModel.removeItem (laundryItem.getType ()));
                } else
                    Toast.makeText (this, "Nothing in Basket", Toast.LENGTH_SHORT).show ();
            });
            binding.btnConfrimandpayCreatebasket.setOnClickListener (view -> payPalIntent (cost));
            viewModel.orderPlacementStatus ().observe (this, isPlaced -> {
                if (isPlaced)
                    Toast.makeText (this, "Order Placed Successfully", Toast.LENGTH_SHORT).show ();
                else
                    Toast.makeText (this, "Order could not be Placed", Toast.LENGTH_SHORT).show ();
            });
        });
    }

    private void payPalIntent (String amount) {
        PayPalPayment payPalPayment = new PayPalPayment ( new BigDecimal (String.valueOf (amount)), "EUR",
                "LaundryWala Payment", PayPalPayment.PAYMENT_INTENT_SALE);
        Intent intent = new Intent (this, PaymentActivity.class)
                .putExtra (PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration)
                .putExtra (PaymentActivity.EXTRA_PAYMENT, payPalPayment);
        payPalActivityResultLauncher.launch (intent);
    }

    @Override
    protected void onStart () {
        super.onStart ();
        startService (new Intent (this, PayPalService.class)
                .putExtra (PayPalService.EXTRA_PAYPAL_CONFIGURATION, payPalConfiguration));
    }

    @Override
    protected void onDestroy () {
        super.onDestroy ();
        stopService (new Intent (this, PayPalService.class));
    }
}
