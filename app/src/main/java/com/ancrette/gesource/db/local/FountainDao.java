package com.ancrette.gesource.db.local;

import androidx.lifecycle.LiveData;
import androidx.room.*;
import com.ancrette.gesource.db.Fountain;

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
