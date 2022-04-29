package com.example.laundry2.Database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface OrderDao {
    @Query("SELECT * FROM laundryItemCache")
    LiveData<List<LaundryItemCache>> getAll();

    @Query("DELETE FROM laundryItemCache")
    void deleteAll();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(LaundryItemCache laundryItemCache);

    @Delete
    void delete(LaundryItemCache laundryItemCache);
}
