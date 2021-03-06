package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.R;

import java.util.ArrayList;
import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {
    private final Context context;
    private List<Order> orders;
    private onOrderClickListener listener;
    private onAssignClickListener listenerAssign;
    private onItemSelectedListenerCustom listenerSpinner;
    private String authType;

    public OrdersAdapter (Context context, List<Order> orders, String authType) {
        this.context = context;
        this.orders = orders;
        this.authType = authType;
    }

    public List<Order> getOrders () {
        return orders;
    }

    public void setOrders (ArrayList<Order> orders) {
        this.orders = orders;
        notifyAll ();
    }

    public String getAuthType () {
        return authType;
    }

    public void setAuthType (String authType) {
        this.authType = authType;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_orders_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.ordercost.setText (String.format ("%s €", this.orders.get (position).getTotalCost ()));
        holder.ordername.setText (String.format ("Order ID-%s", this.orders.get (position).getOrderId ()));
        holder.ordertime.setText (this.orders.get (position).getDateTime ());
        holder.courierId.setText (String.format ("Courier ID-%s", this.orders.get (position).getCourierId ()));
        holder.orderStatus.setText (this.orders.get (position).getStatus ());
    }

    @Override
    public int getItemCount () {
        return orders.size ();
    }

    public void setOnOrderClickListener (onOrderClickListener listener) {
        this.listener = listener;
    }

    public void setOnAssignClickListener (onAssignClickListener listenerAssign) {
        this.listenerAssign = listenerAssign;
    }

    public void onItemSelectedListenerCustom (onItemSelectedListenerCustom listenerSpinner) {
        this.listenerSpinner = listenerSpinner;
    }

    public interface onItemSelectedListenerCustom {
        void onSelected (String status);
    }

    public interface onOrderClickListener {
        void onClick (Order order);
    }

    public interface onAssignClickListener {
        void onClick (Order order);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView ordername, ordercost, ordertime, courierId, orderStatus;
        Button assign, changestatus;
        Spinner spinner;

        public MyViewHolder (@NonNull View itemView) {
            super (itemView);
            ordername = itemView.findViewById (R.id.txt_ordernumber);
            ordertime = itemView.findViewById (R.id.txt_card_ordertime_display);
            ordercost = itemView.findViewById (R.id.txt_card_ordercost_display);
            orderStatus = itemView.findViewById (R.id.txt_card_orders_orderstatus);
            assign = itemView.findViewById (R.id.button_assign);
            courierId = itemView.findViewById (R.id.txt_card_order_courierID);
            changestatus = itemView.findViewById (R.id.button_changestatus);
            spinner = itemView.findViewById (R.id.spinner_status);
            if (authType.equals (context.getString (R.string.laundryhouse))) {
                spinner.setAdapter (ArrayAdapter.createFromResource (itemView.getContext (), R.array.Order_Status_LaundryHouse,
                        R.layout.spinner_item));
            } else
                spinner.setAdapter (ArrayAdapter.createFromResource (itemView.getContext (), R.array.Order_Status_Courier,
                        R.layout.spinner_item));
            changestatus.setOnClickListener (view -> {
                listener.onClick (orders.get (getAdapterPosition ()));
            });

            if (authType.equals (context.getString (R.string.courier))) {
//                assign.setVisibility (View.INVISIBLE);
//            }
                assign.setText (context.getString(R.string.view_items));
            }
            assign.setOnClickListener (view -> {
                listenerAssign.onClick (orders.get (getAdapterPosition ()));
            });

            spinner.setOnItemSelectedListener (new AdapterView.OnItemSelectedListener () {
                @Override
                public void onItemSelected (AdapterView<?> adapterView, View view, int i, long l) {
                    listenerSpinner.onSelected (adapterView.getItemAtPosition (i).toString ());
                }

                @Override
                public void onNothingSelected (AdapterView<?> adapterView) {

                }
            });
        }
    }
}

