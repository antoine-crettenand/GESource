package com.example.fountainfinder.db.local;

import android.app.Activity;
import android.content.Context;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.*;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 9)
public abstract class AppDatabase extends RoomDatabase implements LocalDataSource {
    public abstract FountainDao fountainDao();

    private static final String TAG = AppDatabase.class.getSimpleName();

    public synchronized boolean insert(Collection<Fountain> fountain) {
        Thread thread = Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insertAll(fountain.toArray(new Fountain[0]));
        });
        thread.start();
        return true;
    }

    @Override
    public LiveData<Collection<Fountain>> fetch(Activity activity, LatLng ne, LatLng sw) {
        return selectAllWithinSquare(ne, sw);
    }

    @Override
    public LiveData<Boolean> update(Collection<Fountain> fountains) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        success.setValue(insert(fountains));
        return success;
    }

    private LiveData<Collection<Fountain>> selectAllWithinSquare(LatLng sw, LatLng ne) {
        return Transformations.map(fountainDao().getAll(), input -> {
            List<Fountain> filtered = new ArrayList<>();
            for (Fountain f : input)
                // query only visible fountains
                if (sw.latitude <= f.latitude && f.latitude <= ne.latitude && sw.longitude <= f.longitude && f.longitude <= ne.longitude)
                    filtered.add(f);
            return filtered;
        });
    }
}
