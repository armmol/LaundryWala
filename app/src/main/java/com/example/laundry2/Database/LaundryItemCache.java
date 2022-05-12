package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class LaundryItemCache {

    @PrimaryKey(autoGenerate = true)
    private int uid;

    @NonNull
    @ColumnInfo(name = "type")
    private final String mType;


    public LaundryItemCache (@NonNull String mType) {
        this.mType = mType;
    }

    @NonNull
    public String getType () {
        return this.mType;
    }

    public int getUid () {
        return uid;
    }

    public void setUid (int uid) {
        this.uid = uid;
    }
}
