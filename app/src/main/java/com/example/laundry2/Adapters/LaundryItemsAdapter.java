package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.DataClasses.LaundryItem;
import com.example.laundry2.R;

import java.util.List;

public class LaundryItemsAdapter extends RecyclerView.Adapter<LaundryItemsAdapter.MyViewHolder> {

    private final Context context;
    private List<LaundryItem> laundryitems;
    private onItemClickListener listener;
    private int orderhistory;

    public LaundryItemsAdapter (Context context, List<LaundryItem> laundryitems, int orderhistory) {
        this.laundryitems = laundryitems;
        this.context = context;
        this.orderhistory =orderhistory;
    }

    public List<LaundryItem> getLaundryitems () {
        return laundryitems;
    }

    public void setLaundryitems (List<LaundryItem> laundryitems) {
        this.laundryitems = laundryitems;
        notifyAll ();
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_laundrybasketitem_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.laundryitemname.setText (this.laundryitems.get (position).getType ());
        holder.laundryitemcost.setText (String.format ("%s €", this.laundryitems.get (position).getCost ()));
    }

    @Override
    public int getItemCount () {
        return laundryitems.size ();
    }

    public void setOnItemClickListener (onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void onClick (LaundryItem laundryItem);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView laundryitemname, laundryitemcost;
        ImageButton delete;

        public MyViewHolder (@NonNull View itemView) {
            super (itemView);

            laundryitemname = itemView.findViewById (R.id.txt_card_laundry_itemname);
            laundryitemcost = itemView.findViewById (R.id.txt_card_laundry_itemcost);
            delete = itemView.findViewById (R.id.imgbtn_card_laundrybasket_delete);

            if(orderhistory == 1){
                delete.setVisibility (View.INVISIBLE);
            }
            delete.setOnClickListener (view -> {
                if (getAdapterPosition () > -1) {
                    listener.onClick (laundryitems.get (getAdapterPosition ()));
                    laundryitems.remove (getAdapterPosition ());
                    notifyItemRemoved (getAdapterPosition ());
                }
            });
        }
    }
}
