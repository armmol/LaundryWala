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
    private List<Order> orders;
    private final Context context;
    private onOrderClickListener listener;
    private onAssignClickListener listenerAssign;
    private onItemSelectedListenerCustom listenerSpinner;
    private String authType;

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

    public OrdersAdapter (Context context, List<Order> orders, String authType) {
        this.context = context;
        this.orders = orders;
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
        holder.ordercost.setText (String.format ("%s €",this.orders.get (position).getTotalCost ()));
        holder.ordername.setText (String.format ("Order ID-%s", this.orders.get (position).getOrderId ()));
        holder.ordertime.setText (this.orders.get (position).getDateTime ());
        holder.courierId.setText (String.format ("Courier assigned-%s",this.orders.get (position).getCourierId ()));
    }

    @Override
    public int getItemCount () {
        return orders.size ();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView ordername, ordercost, ordertime, courierId;
        Button assign, changestatus;
        Spinner spinner;
        public MyViewHolder (@NonNull View itemView) {
            super (itemView);
            ordername = itemView.findViewById (R.id.txt_ordernumber);
            ordertime = itemView.findViewById (R.id.txt_card_ordertime_display);
            ordercost = itemView.findViewById (R.id.txt_card_ordercost_display);
            assign = itemView.findViewById (R.id.button_assign);
            courierId = itemView.findViewById (R.id.txt_card_order_courierID);
            changestatus = itemView.findViewById (R.id.button_changestatus);
            spinner = itemView.findViewById (R.id.spinner_status);
            spinner.setAdapter (ArrayAdapter.createFromResource (itemView.getContext (), R.array.Order_Status,
                    androidx.appcompat.R.layout.support_simple_spinner_dropdown_item));

            changestatus.setOnClickListener (view -> {
                listener.onClick (orders.get (getAdapterPosition ()));
            });

            if(authType.equals (context.getString (R.string.courier))){
                assign.setText (context.getString(R.string.start_tracking));
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

    public interface onItemSelectedListenerCustom{
        void onSelected(String status);
    }

    public interface onOrderClickListener{
        void onClick(Order order);
    }

    public interface onAssignClickListener{
        void onClick(Order order);
    }

    public void setOnOrderClickListener(onOrderClickListener listener){
        this.listener = listener;
    }

    public void setOnAssignClickListener(onAssignClickListener listenerAssign){
        this.listenerAssign = listenerAssign;
    }

    public void onItemSelectedListenerCustom(onItemSelectedListenerCustom listenerSpinner){
        this.listenerSpinner = listenerSpinner;
    }
}
