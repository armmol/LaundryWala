package com.example.laundry2.Database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class AuthType {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "type")
    public String authtype;

    public AuthType (@NonNull String authtype) {
        this.authtype = authtype;
    }
}
