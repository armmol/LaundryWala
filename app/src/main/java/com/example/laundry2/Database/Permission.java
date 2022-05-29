package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class Permission {

    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "Permission")
    public String permission;

    public Permission (@NonNull String permission) {
        this.permission = permission;
    }
}
