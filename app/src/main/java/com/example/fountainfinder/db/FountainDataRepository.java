package com.example.fountainfinder.db;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.example.fountainfinder.db.local.LocalDataSource;
import com.example.fountainfinder.db.remote.scrapper.RemoteDataSource;
import com.example.fountainfinder.db.sanitizer.DataSanitizer;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collection;


public final class FountainDataRepository {

    private final RemoteDataSource remoteDataSource;
    private final LocalDataSource localDataSource;
    private final DataSanitizer dataSanitizer;

    private static final int ONE_MINUTE_IN_MS = 1 * 60 * 1000;
    private static long TIMECODE_OF_PREVIOUS_API_CALL;

    FountainDataRepository(LocalDataSource localDataSource, RemoteDataSource remoteDataSource, DataSanitizer dataSanitizer) {
        this.localDataSource = localDataSource;
        this.remoteDataSource = remoteDataSource;
        this.dataSanitizer = dataSanitizer;
    }

    public LiveData<Boolean> insert(Collection<Fountain> fountains) {
        return localDataSource.update(fountains);
    }

    public LiveData<Boolean> insert(FragmentActivity activity, Fountain fountain) {
        return remoteDataSource.update(activity, fountain);
    }

    public LiveData<Collection<Fountain>> getAll(FragmentActivity activity, LatLng ne, LatLng sw) {
        long currentTime = System.currentTimeMillis();
        if (isInternetAvailable() && currentTime - TIMECODE_OF_PREVIOUS_API_CALL >= ONE_MINUTE_IN_MS) {
            LiveData<Collection<Fountain>> remoteData = remoteDataSource.fetch(activity, ne, sw);
            TIMECODE_OF_PREVIOUS_API_CALL = currentTime;

            // sanitize data
            LiveData<Collection<Fountain>> sanitizedRemoteData = Transformations.map(remoteData, dataSanitizer::sanitize);

            // priority over online data
            sanitizedRemoteData.observe(activity, localDataSource::update);
            return sanitizedRemoteData;
        } else
            return localDataSource.fetch(activity, ne, sw);
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
