package com.example.fountainfinder.db.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import androidx.work.ListenableWorker;
import com.example.fountainfinder.db.Fountain;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.List;

@Dao
public interface FountainDao {

    @Query("SELECT * FROM fountain")
    LiveData<List<Fountain>> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(Fountain fountain);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(Fountain... fountains);

    @Delete
    void delete(Fountain fountain);
}
