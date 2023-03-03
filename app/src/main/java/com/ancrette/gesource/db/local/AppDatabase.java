package com.ancrette.gesource.db.local;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 12)
public abstract class AppDatabase extends RoomDatabase implements LocalDatabase {
    private static final String TAG = AppDatabase.class.getSimpleName();

    public abstract FountainDao fountainDao();

    @Override
    public LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw) {
        return selectAllWithinSquare(ne, sw);
    }

    public LiveData<Boolean> insertAll(Collection<Fountain> fountains) {
        return new MutableLiveData<>(insertAllHelper(fountains));
    }

    @Override
    synchronized public LiveData<Boolean> insert(Fountain f) {
        return insertAll(Collections.<Fountain>singleton(f));
    }

    private LiveData<Collection<Fountain>> selectAllWithinSquare(LatLng sw, LatLng ne) {
        return Transformations.map(fountainDao().scan(), input -> {
            List<Fountain> filtered = new ArrayList<>();
            for (Fountain f : input)
                // query only visible fountains
                if (sw.latitude <= f.latitude && f.latitude <= ne.latitude && sw.longitude <= f.longitude && f.longitude <= ne.longitude)
                    filtered.add(f);
            return filtered;
        });
    }

    private synchronized boolean insertAllHelper(Collection<Fountain> fountains) {
        try {
            Thread thread = Executors.defaultThreadFactory().newThread(() -> {
                fountainDao().insertAll(fountains.toArray(new Fountain[0]));
            });
            thread.start();
        } catch (Exception e) {
            return false;
        }

        return true;
    }
}
