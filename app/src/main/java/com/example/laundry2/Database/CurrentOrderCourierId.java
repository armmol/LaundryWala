package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class CurrentOrderCourierId {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "courier")
    public String courierId;
    public String orderId;
    public String deliveryCost;

    public CurrentOrderCourierId (@NonNull String courierId, String orderId, String deliveryCost) {
        this.courierId = courierId;
        this.orderId = orderId;
        this.deliveryCost = deliveryCost;
    }
}
