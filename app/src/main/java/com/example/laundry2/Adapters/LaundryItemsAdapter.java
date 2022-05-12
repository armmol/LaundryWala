package com.example.laundry2.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundry2.Database.LaundryItemCache;
import com.example.laundry2.R;

import java.util.List;

public class LaundryItemsAdapter extends RecyclerView.Adapter<LaundryItemsAdapter.MyViewHolder> {

    private final Context context;
    private final List<LaundryItemCache> laundryItems;
    private onItemClickListener listener;
    private final int isOrderHistory;

    public LaundryItemsAdapter (Context context, List<LaundryItemCache> laundryItems, int isOrderHistory) {
        this.laundryItems = laundryItems;
        this.context = context;
        this.isOrderHistory =isOrderHistory;
    }


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder (@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.cardview_laundrybasketitem_adapter, parent, false);
        return new MyViewHolder (view);
    }

    @Override
    public void onBindViewHolder (@NonNull MyViewHolder holder, int position) {
        holder.laundryItemName.setText (this.laundryItems.get (position).getType ().split (",")[0]);
        holder.laundryItemCost.setText (String.format ("%s €", this.laundryItems.get (position).getType ().split (",")[1]));
    }

    @Override
    public int getItemCount () {
        return laundryItems.size ();
    }

    public void setOnItemClickListener (onItemClickListener listener) {
        this.listener = listener;
    }

    public interface onItemClickListener {
        void onClick (LaundryItemCache laundryItem);
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView laundryItemName, laundryItemCost;
        ImageButton delete;

        public MyViewHolder (@NonNull View itemView) {
            super (itemView);

            laundryItemName = itemView.findViewById (R.id.txt_card_laundry_itemname);
            laundryItemCost = itemView.findViewById (R.id.txt_card_laundry_itemcost);
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
