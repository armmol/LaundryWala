package com.example.laundry2.Database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {LaundryItemCache.class, AuthType.class, LaundryHouseCache.class,
        OrderTracking.class, CurrentOrderCourierId.class, Permission.class}, version = 30, exportSchema = false)
public abstract class ApplicationDatabase extends RoomDatabase {

    private static volatile ApplicationDatabase INSTANCE;
    public static ApplicationDatabase getDatabase (final Context context) {
        if (INSTANCE == null) {
            synchronized (ApplicationDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder (context.getApplicationContext (),
                            ApplicationDatabase.class, "order_database")
                            .fallbackToDestructiveMigration ()
                            .allowMainThreadQueries ()
                            .build ();
                }
            }
        }
        return INSTANCE;
    }

    public abstract ApplicationDao appDao ();
}
