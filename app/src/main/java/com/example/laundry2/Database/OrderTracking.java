package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class OrderTracking {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "isOrderTracking")
    public String isOrderTracking;

    public OrderTracking (@NonNull String isOrderTracking) {
        this.isOrderTracking = isOrderTracking;
    }
}
