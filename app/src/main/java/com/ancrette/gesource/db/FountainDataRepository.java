package com.ancrette.gesource.db;

import android.widget.Toast;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.ancrette.gesource.SettingsParameters;
import com.ancrette.gesource.db.local.LocalDataSource;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.ancrette.gesource.db.sanitizer.DataSanitizer;
import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

/**
 * Manage local and remote data storage. Ensures synchronicity.
 */
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

    public LiveData<Boolean> insert(Fountain fountain, FragmentActivity activity) {
        localDataSource.insert(fountain, activity);
        return remoteDataSource.insert(fountain, activity);
    }

    public LiveData<Collection<Fountain>> scan(LatLng ne, LatLng sw, FragmentActivity activity) {
        long currentTime = System.currentTimeMillis();
        if (isInternetAvailable()) {
            LiveData<Collection<Fountain>> remoteData = remoteDataSource.scanWithinBorders(ne, sw, activity);
            TIMECODE_OF_PREVIOUS_API_CALL = currentTime;
            // sanitize data
            LiveData<Collection<Fountain>> sanitizedRemoteData = Transformations.map(remoteData, dataSanitizer::sanitize);

            // priority over online data
            if (SettingsParameters.SYNC_LOCAL_WITH_UPSTREAM_DB)
                sanitizedRemoteData.observe(activity, localDataSource::insertAll);

            return sanitizedRemoteData;
        } else
            Toast.makeText(activity, "It seems like you're not connected to Internet!", Toast.LENGTH_SHORT).show();
        return localDataSource.scanWithinBorders(ne, sw, activity);
    }

    //TODO find a better way to do this ?
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
