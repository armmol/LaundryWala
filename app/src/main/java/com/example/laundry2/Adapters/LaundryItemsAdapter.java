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
    private List<LaundryItem> laundryItems;
    private onItemClickListener listener;
    private int isOrderHistory;

    public LaundryItemsAdapter (Context context, List<LaundryItem> laundryItems, int isOrderHistoy) {
        this.laundryItems = laundryItems;
        this.context = context;
        this.isOrderHistory =isOrderHistoy;
    }

    public List<LaundryItem> getLaundryItems () {
        return laundryItems;
    }

    public void setLaundryItems (List<LaundryItem> laundryItems) {
        this.laundryItems = laundryItems;
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
        holder.laundryitemname.setText (this.laundryItems.get (position).getType ());
        holder.laundryitemcost.setText (String.format ("%s €", this.laundryItems.get (position).getCost ()));
    }

    @Override
    public int getItemCount () {
        return laundryItems.size ();
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

            if(isOrderHistory == 1){
                delete.setVisibility (View.INVISIBLE);
            }
            delete.setOnClickListener (view -> {
                if (getAdapterPosition () > -1) {
                    listener.onClick (laundryItems.get (getAdapterPosition ()));
                    laundryItems.remove (getAdapterPosition ());
                    notifyItemRemoved (getAdapterPosition ());
                }
            });
        }
    }
}
