package com.ancrette.gesource.db;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.ancrette.gesource.SettingsParameters;
import com.ancrette.gesource.db.local.LocalDatabase;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.ancrette.gesource.db.sanitizer.DataSanitizer;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

/**
 * Manage local and remote data storage. Ensures synchronicity.
 */
public final class FountainDataRepository {

    public enum RequestErrorStatus {
        NO_ERROR, REMOTE_DATA_SOURCE_FAILED, LOCAL_DATA_SOURCE_FAILED, REMOTE_AND_LOCAL_SOURCE_FAILED;


        public static RequestErrorStatus valueOf(boolean local, boolean remote) {
            if (local)
                if (remote)
                    return NO_ERROR;
                else
                    return REMOTE_DATA_SOURCE_FAILED;
            else
                if (remote)
                    return LOCAL_DATA_SOURCE_FAILED;
                else
                    return REMOTE_AND_LOCAL_SOURCE_FAILED;
        }

        public boolean haveRemoteSourceFailed() {
            return this == REMOTE_AND_LOCAL_SOURCE_FAILED || this == REMOTE_DATA_SOURCE_FAILED;
        }

        public boolean haveLocalSourceFailed() {
            return this == LOCAL_DATA_SOURCE_FAILED || this == REMOTE_AND_LOCAL_SOURCE_FAILED;
        }

        public boolean isSuccessful() {
            return this == NO_ERROR;
        }
    }

    private final String TAG = FountainDataRepository.class.getSimpleName();
    private final RemoteDataSource remoteDataSource;
    private final LocalDatabase localDatabase;
    private final DataSanitizer dataSanitizer;
    private static final int ONE_MINUTE_IN_MS = 1 * 60 * 1000;
    private static long MS_ELAPSED_FROM_PREVIOUS_API_CALL = 0L;


    FountainDataRepository(LocalDatabase localDatabase, RemoteDataSource remoteDataSource, DataSanitizer dataSanitizer) {
        this.localDatabase = localDatabase;
        this.remoteDataSource = remoteDataSource;
        this.dataSanitizer = dataSanitizer;
    }

    public LiveData<RequestErrorStatus> insert(Fountain f) {
        return Transformations.switchMap(localDatabase.insert(f),
                localSuccess -> {
                    if (localSuccess)
                        return Transformations.map(remoteDataSource.insert(f),
                                remoteSuccess -> RequestErrorStatus.valueOf(true, remoteSuccess));
                    else
                        return new MutableLiveData<>(RequestErrorStatus.valueOf(false, false));
                }
        );
    }

    public LiveData<Collection<Fountain>> scan(LatLng ne, LatLng sw) {
        long currentTime = System.currentTimeMillis();
        if (fiveMinutesHasElapsedFromPreviousAPICall(currentTime)) {
            Log.d(TAG, "More than five minutes have elapsed. Fetching from remote datasource...");
            LiveData<Collection<Fountain>> remoteData = remoteDataSource.scanWithinBorders(ne, sw);
            MS_ELAPSED_FROM_PREVIOUS_API_CALL = currentTime;

            // sanitize data
            LiveData<Collection<Fountain>> sanitizedRemoteData = Transformations.map(remoteData, dataSanitizer::sanitize);

            // priority over online data
            if (SettingsParameters.SYNC_LOCAL_WITH_UPSTREAM_DB)
                Transformations.map(sanitizedRemoteData, localDatabase::insertAll);
            return sanitizedRemoteData;
        } else {
            Log.d(TAG, "Less than fives minutes have elapsed since last API call. Fetching from local datasource...");
            return localDatabase.scanWithinBorders(ne, sw);
        }
    }

    private boolean fiveMinutesHasElapsedFromPreviousAPICall(long currentTime) {
        return currentTime - MS_ELAPSED_FROM_PREVIOUS_API_CALL > 5 * ONE_MINUTE_IN_MS;
    }
}
