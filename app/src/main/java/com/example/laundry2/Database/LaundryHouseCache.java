package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
@Entity
public class LaundryHouseCache {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    @ColumnInfo(name = "laundryHouseID")
    private final String laundryHouseID;

    @NonNull
    @ColumnInfo(name = "deliveryCost")
    private final String deliveryCost;



    public LaundryHouseCache (@NonNull String laundryHouseID, @NonNull String deliveryCost) {
        this.laundryHouseID = laundryHouseID;
        this.deliveryCost = deliveryCost;
    }

    public int getUid () {
        return uid;
    }

    public void setUid (int uid) {
        this.uid = uid;
    }

    @NonNull
    public String getLaundryHouseID () {
        return laundryHouseID;
    }

    @NonNull
    public String getDeliveryCost () {
        return deliveryCost;
    }
}
