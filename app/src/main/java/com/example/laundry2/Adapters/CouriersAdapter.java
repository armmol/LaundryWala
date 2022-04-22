package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
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
        holder.couriername.setText (couriers.get (position).getName ());
        holder.checkBox.setChecked (couriers.get (position).isActive ());
    }

    @Override
    public int getItemCount () {
        return couriers.size ();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder{

        TextView couriername;
        CheckBox checkBox;
        public MyViewHolder (@NonNull View itemView) {
            super (itemView);

            couriername = itemView.findViewById (R.id.txt_card_couriername);
            checkBox = itemView.findViewById (R.id.checkBox_Active_CouriersCard);
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
