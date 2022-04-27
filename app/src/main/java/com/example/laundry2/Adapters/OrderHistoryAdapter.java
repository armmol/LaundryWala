package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.DataClasses.Order;
import com.example.laundry2.R;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.MyViewHolder> {
    private final Context context;
    private List<Order> orders;
    private onItemClickListener listener;

    public OrderHistoryAdapter (Context context, List<Order> laundryItems) {
        this.context = context;
        this.orders = laundryItems;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_orderhistory_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.orderId.setText (orders.get (position).getOrderId ());
        holder.orderTime.setText (orders.get (position).getDateTime ());
        holder.orderCost.setText (String.format ("%s €", orders.get (position).getTotalCost ()));
        holder.orderStatus.setText (orders.get (position).getStatus ());
        holder.deliveryCost.setText (String.format ("%s €", orders.get (position).getDeliveryCost ()));
    }

    @Override
    public int getItemCount () {
        return orders.size ();
    }

    public List<Order> getOrders () {
        return orders;
    }

    public void setOrders (List<Order> orders) {
        this.orders = orders;
    }

    public void setOnItemClickListener (onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void onClick (Order order);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderId, orderStatus, orderTime, orderCost, deliveryCost;

        public MyViewHolder (@NonNull View itemView) {
            super (itemView);

            orderId = itemView.findViewById (R.id.textView_card_orderhistory_orderid);
            orderStatus = itemView.findViewById (R.id.textView_card_orderhistory_orderstatus);
            orderTime = itemView.findViewById (R.id.textView_card_orderhistory_ordertime);
            orderCost = itemView.findViewById (R.id.textView_card_orderhistory_ordercost);
            deliveryCost = itemView.findViewById (R.id.textView_card_orderhistory_deliveryCost);

            itemView.setOnClickListener (view -> listener.onClick (orders.get (getAdapterPosition ())));
        }
    }
}
