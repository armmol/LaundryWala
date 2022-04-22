package com.example.laundry2.View;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Toast;

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

import java.text.DecimalFormat;
import java.text.MessageFormat;


public class activity_createBasket extends AppCompatActivity {

    private ActivityCreatebasketBinding binding;
    private LaundryBasketViewModel viewModel;
    private PopupWindow window;
    private LaundryItemsAdapter adapter;

    @Override
    protected void onCreate (@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);

        String laundryhouseuid = getIntent ().getStringExtra ("laundryhouseuid");
        binding = DataBindingUtil.setContentView (this, R.layout.activity_createbasket);
        viewModel = new ViewModelProvider (this).get (LaundryBasketViewModel.class);

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
            for (LaundryItem item : laundryItemList) { a+=item.getCost ();}
            binding.txtLaundrybasketcost.setText (String.format ("%s €", df.format (a)));
            binding.cardLaundrybasket.setOnClickListener (view -> {
                if (binding.txtLaundrybasketcounter.getText () != "0") {
                    View mview = LayoutInflater.from (activity_createBasket.this).inflate (R.layout.activity_confirmorder, null);
                    window = new PopupWindow (mview);
                    window.setHeight (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setWidth (ViewGroup.LayoutParams.WRAP_CONTENT);
                    window.setFocusable (true);
                    window.showAtLocation (mview, Gravity.CENTER, 0, 0);
                    RecyclerView recyclerView = mview.findViewById (R.id.recyclerView_confirmorder);

                    adapter = new LaundryItemsAdapter (this, laundryItemList);
                    recyclerView.setLayoutManager (new LinearLayoutManager (getApplicationContext ()));
                    recyclerView.setAdapter (adapter);
                    adapter.setOnItemClickListener (laundryItem -> {
                        viewModel.removeItem (laundryItem.getType ());
                    });
                }
                else
                    Toast.makeText(this,"Nothing in Basket",Toast.LENGTH_SHORT).show ();
            });
            binding.btnConfrimandpayCreatebasket.setOnClickListener ( view -> {
                viewModel.createOrder (laundryhouseuid);
                startActivity (new Intent (activity_createBasket.this, activity_home.class)
                        .putExtra ("authtype", getString (R.string.customer)));
            });

            viewModel.orderPlacementStatus ().observe (this, isPlaced -> {
                if(isPlaced)
                    Toast.makeText(this,"Order Placed Successfully",Toast.LENGTH_SHORT).show ();
                else
                    Toast.makeText(this,"Order could not be Placed",Toast.LENGTH_SHORT).show ();
            });
        });

        //Activity result
    }
}
