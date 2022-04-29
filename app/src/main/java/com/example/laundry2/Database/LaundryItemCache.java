package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "laundryItemCache")
public class LaundryItemCache {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "type")
    private String type;

    public LaundryItemCache(@NonNull String type){
        this.type = type;
    }

    @NonNull
    public String getType () {
        return type;
    }

    public void setType (@NonNull String type) {
        this.type = type;
    }
}
