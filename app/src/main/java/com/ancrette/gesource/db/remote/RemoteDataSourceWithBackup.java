package com.ancrette.gesource.db.remote;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Transformations;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public abstract class RemoteDataSourceWithBackup implements RemoteDataSource {

    private final RemoteDataSource backupDataSource;

    protected RemoteDataSourceWithBackup(RemoteDataSource backupDataSource){
        this.backupDataSource = backupDataSource;
    }

    protected LiveData<Collection<Fountain>> fetchBackup(LatLng ne, LatLng sw){
        return backupDataSource.scanWithinBorders(ne, sw);
    }
}
