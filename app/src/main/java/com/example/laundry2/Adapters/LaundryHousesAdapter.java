package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.DataClasses.LaundryHouse;
import com.example.laundry2.R;

import java.util.List;

public class LaundryHousesAdapter extends RecyclerView.Adapter<LaundryHousesAdapter.MyViewHolder> {

    public LaundryHousesAdapter (Context context, List<LaundryHouse> laundryHouseList) {
        this.context = context;
        this.laundryHouseList = laundryHouseList;
    }

    private final Context context;
    private List<LaundryHouse> laundryHouseList;
    private onItemClickListener listener;

    public List<LaundryHouse> getLaundryHouseList () {
        return laundryHouseList;
    }

    public void setLaundryHouseList (List<LaundryHouse> laundryHouseList) {
        this.laundryHouseList = laundryHouseList;
        notifyAll ();
    }

    @NonNull
    @Override
    public LaundryHousesAdapter.MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_laundryhouses_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.name.setText (this.laundryHouseList.get (position).getName ());
        holder.area.setText (this.laundryHouseList.get (position).getArea ());
        holder.deliverycost.setText (String.format ("%s €", this.laundryHouseList.get (position).getDeliveryprice ()));
        holder.active.setChecked (this.laundryHouseList.get(position).isActive ());
    }

    @Override
    public int getItemCount () {
        if (!laundryHouseList.isEmpty ())
            return laundryHouseList.size ();
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView name, area, deliverycost;
        CheckBox active;

        public MyViewHolder (@NonNull View itemView) {
            super (itemView);
            name = itemView.findViewById (R.id.txt_laundryhouse_name);
            area = itemView.findViewById (R.id.txt_laundryhouse_address);
            deliverycost = itemView.findViewById (R.id.txt_laundryhouse_deliverycost);
            active = itemView.findViewById (R.id.checkBox_active);
            active.setEnabled (false);

            itemView.setOnClickListener (view -> listener.onClick (laundryHouseList.get (getAdapterPosition ())));
        }
    }

    public interface onItemClickListener{
         void onClick(LaundryHouse laundryHouse);
    }

    public void setOnItemClickListener(onItemClickListener listener){
        this.listener = listener;
    }
}
