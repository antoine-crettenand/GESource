package com.ancrette.gesource.db.local;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 11)
public abstract class AppDatabase extends RoomDatabase implements LocalDataSource {
    private static final String TAG = AppDatabase.class.getSimpleName();

    public abstract FountainDao fountainDao();

    @Override
    public LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw, Activity activity) {
        return selectAllWithinSquare(ne, sw);
    }

    public LiveData<Boolean> insertAll(Collection<Fountain> fountains) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        success.setValue(insertAllHelper(fountains));
        return success;
    }

    @Override
    public LiveData<Boolean> insert(Fountain f, Activity activity) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        Thread thread = Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insert(f);
            success.setValue(true);
        });
        thread.start();
        return success;
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
        Thread thread = Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insertAll(fountains.toArray(new Fountain[0]));
        });
        thread.start();
        return true;
    }
}
