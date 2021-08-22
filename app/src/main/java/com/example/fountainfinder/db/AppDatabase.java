package com.example.fountainfinder.db;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.fountainfinder.scrapper.GESoifScrapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 5)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FountainDao fountainDao();

    private static AppDatabase INSTANCE;
    private static final String TAG = "APP_DATABASE";

    public synchronized boolean insert(Fountain fountain) {
        Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insertAll(fountain);
            Log.i(TAG, String.format("Successfully added %s at (%f, %f) to fl_database", fountain.title, fountain.latitude, fountain.longitude));
        }).start();
        return true;
    }

    public LiveData<List<Fountain>> queryAllFountains(Activity activity, LatLng sw, LatLng ne) {
        if (!isInternetAvailable()) {
            return queryAllFountainsOffline(sw, ne);
        } else {
            LiveData<List<Fountain>> data = queryAllFountainsOnline(activity, sw, ne);
            // Update local database
            data.observeForever(fountains -> Executors.defaultThreadFactory().newThread(() -> fountainDao().insertAll(fountains.toArray(new Fountain[0]))).start());
            return data;
        }
    }

    private LiveData<List<Fountain>> queryAllFountainsOnline(Activity activity, LatLng sw, LatLng ne) {
        return GESoifScrapper.getFountainsFromRadius(activity, ((float) sw.latitude), ((float) sw.longitude), ((float) ne.latitude), ((float) ne.longitude));
    }

    private LiveData<List<Fountain>> queryAllFountainsOffline(LatLng sw, LatLng ne) {
        return Transformations.map(fountainDao().getAll(), input -> {
            List<Fountain> filtered = new ArrayList<>();
            for (Fountain f : input)
                // query only visible fountains
                if (sw.latitude <= f.latitude && f.latitude <= ne.latitude && sw.longitude <= f.longitude && f.longitude <= ne.longitude)
                    filtered.add(f);
            return filtered;
        });
    }

    private boolean isInternetAvailable() {
        String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
}
