package com.example.fountainfinder.db;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.util.Log;
import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.work.impl.utils.LiveDataUtils;
import com.example.fountainfinder.scrapper.GESoifScrapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 2)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FountainDao fountainDao();

    private static AppDatabase INSTANCE;
    private static final String TAG = "APP_DATABASE";

    public boolean insert(Fountain fountain) {
        Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insertAll(fountain);
            Log.i(TAG, String.format("Successfully added %s at (%f, %f)to fl_database", fountain.title, fountain.latitude, fountain.longitude));
        }).start();
        return true;
    }

    LiveData<List<Fountain>> getAllOnline(Activity activity, LatLng sw, LatLng ne) {
        return GESoifScrapper.getFountainsFromRadius(activity, ((float) sw.latitude), ((float) sw.longitude), ((float) ne.latitude), ((float) ne.longitude));
    }

    LiveData<List<Fountain>> getAllOffline(LatLng sw, LatLng ne) {
        return Transformations.map(fountainDao().getAll(), input -> {
            List<Fountain> filtered = new ArrayList<>();
            for (Fountain f : input)
                if (sw.latitude <= f.latitude && f.latitude <= ne.latitude && sw.longitude <= f.longitude && f.longitude <= ne.longitude)
                    filtered.add(f);
            return filtered;
        });
    }

    public LiveData<List<Fountain>> getAll(Activity activity, LatLng sw, LatLng ne) {
        if (!isInternetAvailable()) {
            return getAllOffline(sw, ne);
        } else {
            LiveData<List<Fountain>> data = getAllOnline(activity, sw, ne);
            // Update local database
            data.observeForever(fountains -> Executors.defaultThreadFactory().newThread(() -> fountainDao().insertAll(fountains.toArray(new Fountain[0]))).start());
            return data;
        }
    }

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                            AppDatabase.class, "fl_database")
                            // Wipes and rebuilds instead of migrating
                            // if no Migration object.
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return INSTANCE;
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
