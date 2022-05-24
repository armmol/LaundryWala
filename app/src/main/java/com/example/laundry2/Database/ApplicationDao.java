package com.example.laundry2.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ApplicationDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLaundryItem (LaundryItemCache laundryItemCache);

    @Delete
    void deleteLaundryItem (LaundryItemCache laundryItemCache);

    @Query("DELETE FROM LaundryItemCache")
    void deleteAll ();

    @Query("SELECT * FROM LaundryItemCache")
    LiveData<List<LaundryItemCache>> getAllItems ();

    @Query("SELECT * FROM LaundryItemCache")
    List<LaundryItemCache> getAllItemsAsList ();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertAuthtype(AuthType authType);

    @Query("SELECT * FROM AuthType")
    LiveData<AuthType> getAuthType();

    @Query("DELETE FROM AuthType")
    void deleteAuthType();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertLaundryHouseData(LaundryHouseCache laundryHouseCache);

    @Query("SELECT * FROM LaundryHouseCache")
    LiveData<LaundryHouseCache> getLaundryHouseCache();

    @Query("DELETE FROM LaundryHouseCache")
    void deleteLaundryHouseCache();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertIsOrderTracking(OrderTracking OrderTracking);

    @Query("SELECT * FROM OrderTracking")
    LiveData<OrderTracking> getIsOrderTracking();

    @Query ("Delete FROM OrderTracking")
    void deleteIsOrderTracking();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertCurrentOrderCourierId(CurrentOrderCourierId currentOrderCourierId);

    @Query("SELECT * FROM CurrentOrderCourierId")
    LiveData<CurrentOrderCourierId> getCurrentOrderCourierId();

    @Query ("Delete FROM CurrentOrderCourierId")
    void deleteCurrentOrderCourierId();
}
