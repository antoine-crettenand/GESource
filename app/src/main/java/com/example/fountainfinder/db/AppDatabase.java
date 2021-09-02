package com.example.fountainfinder.db;

import android.app.Activity;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import com.example.fountainfinder.scrapper.GESoifScrapper;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.Executors;

@Database(entities = {Fountain.class}, version = 6)
public abstract class AppDatabase extends RoomDatabase {
    public abstract FountainDao fountainDao();

    public GESoifScrapper dataProvider = GESoifScrapper.getInstance();

    private static final String TAG = AppDatabase.class.getSimpleName();

    public synchronized boolean insert(Fountain fountain) {
        Executors.defaultThreadFactory().newThread(() -> {
            fountainDao().insertAll(fountain);
            Log.i(TAG, String.format("Successfully added %s at (%.4f, %.4f) to fl_database", fountain.title, fountain.latitude, fountain.longitude));
        }).start();
        return true;
    }

    public LiveData<List<Fountain>> getAllFountainsWithinRectangle(Activity activity, LatLng sw, LatLng ne) {
        if (!isInternetAvailable()) {
            return queryAllFountainsWithinRectangleOffline(sw, ne);
        } else {
            LiveData<List<Fountain>> data = queryAllFountainsWithinRectangleOnline(activity, sw, ne);

            // Filter out dirty data
            LiveData<List<Fountain>> filteredData = Transformations.map(data, AppDatabase::sanitize);

            // Synchronize local database with remote one TODO configure a lifecycle instead of observing forever.
            filteredData.observeForever(fountains -> Executors.defaultThreadFactory().newThread(() -> {
                fountainDao().insertAll(fountains.toArray(new Fountain[0]));
            }).start());

            return filteredData;
        }
    }

    static boolean isSane(Fountain f) {
        if (f.title.contains("undefined"))
            return false;

        if (f.address.contains("false"))
            return false;

        return !f.title.contains("null") && !f.title.contains("false");
    }

    private static double toRad(double value) {
        return value * Math.PI / 180d;
    }

    private static double haversine(LatLng f1, LatLng f2) {
        final double UNIT_KM_TO_M = 0.001d; // ration m/km
        final double R = 6371.008d; // Radius of the earth, in km
        double lat1 = f1.latitude;
        double lat2 = f2.latitude;
        double lon1 = f1.longitude;
        double lon2 = f2.longitude;
        double latDistance = toRad(lat2 - lat1);
        double lonDistance = toRad(lon2 - lon1);
        double a = Math.sin(latDistance / 2d) * Math.sin(latDistance / 2d) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2d) * Math.sin(lonDistance / 2d);
        double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
        return R * c * UNIT_KM_TO_M;
    }

    // Naive O(n^2) algorithm TODO Find a O(n) complexity alternative
    // Reordering happens
    static List<Fountain> sanitize(List<Fountain> source) {
        List<Fountain> individuallySanitizedSource = new ArrayList<>();

        for (Fountain f : source) {
            boolean fIsTheClusterCenter = true;

            if (!isSane(f))
                continue;

            for (Fountain g : source) {
                if (f.equals(g))
                    continue;

                if (haversine(f.getPosition(), g.getPosition()) < 1)
                    if (g.title.contains(f.title)) {
                        fIsTheClusterCenter = false;
                    }
            }

            if (fIsTheClusterCenter)
                individuallySanitizedSource.add(f);
        }
        return individuallySanitizedSource;
    }

    private LiveData<List<Fountain>> queryAllFountainsWithinRectangleOnline(Activity activity, LatLng sw, LatLng ne) {
        return dataProvider.getFountainsFromRadius(activity, ((float) sw.latitude), ((float) sw.longitude), ((float) ne.latitude), ((float) ne.longitude));
    }

    private LiveData<List<Fountain>> queryAllFountainsWithinRectangleOffline(LatLng sw, LatLng ne) {
        return Transformations.map(fountainDao().getAll(), input -> {
            List<Fountain> filtered = new ArrayList<>();
            for (Fountain f : input)
                // query only visible fountains
                if (sw.latitude <= f.latitude && f.latitude <= ne.latitude && sw.longitude <= f.longitude && f.longitude <= ne.longitude)
                    filtered.add(f);
            return filtered;
        });
    }

    //@TODO find a better way to do this ?
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
