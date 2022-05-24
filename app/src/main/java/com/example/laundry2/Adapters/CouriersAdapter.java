package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.DataClasses.Courier;
import com.example.laundry2.R;

import java.util.List;

public class CouriersAdapter extends RecyclerView.Adapter<CouriersAdapter.MyViewHolder>{

    private final Context context;
    private List<Courier> couriers;
    private onItemCourierListener listener;

    public CouriersAdapter (Context context, List<Courier> couriers) {
        this.context = context;
        this.couriers = couriers;
    }

    public List<Courier> getCouriers () {
        return couriers;
    }

    public void setCouriers (List<Courier> couriers) {
        this.couriers = couriers;
        notifyAll ();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_couriers_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.courierName.setText (String.format ("Name: %s",couriers.get (position).getName ()));
        holder.courierOrderNumber.setText (String.format ("Orders in queue: %s", couriers.get (position).getOrderId ().size ()));
        holder.courierDistanceCustomer.setText (String.format ("Distance from Customer: %skm", couriers.get (position).getDistanceFromCustomer ()));
        holder.courierDistanceToLaundryHouse.setText (String.format ("Distance from Laundry House: %skm", couriers.get (position).getDistanceFromLaundryHouse ()));
    }

    @Override
    public int getItemCount () {
        return couriers.size ();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView courierName, courierDistanceCustomer, courierDistanceToLaundryHouse, courierOrderNumber;
        public MyViewHolder (@NonNull View itemView) {
            super (itemView);

            courierName = itemView.findViewById (R.id.txt_card_couriername);
            courierDistanceCustomer = itemView.findViewById (R.id.txt_card_courierdistanceToCustomer);
            courierDistanceToLaundryHouse = itemView.findViewById (R.id.txt_card_courierdistanceToLaundryHouse);
            courierOrderNumber = itemView.findViewById (R.id.txt_card_courierOrders);
            itemView.setOnClickListener (view -> {
                listener.onClick (couriers.get (getAdapterPosition ()));
            });
        }
    }

    public interface onItemCourierListener{
        void onClick(Courier courier);
    }

    public void onItemCourierListener(onItemCourierListener listener){
        this.listener = listener;
    }
}
