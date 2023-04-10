package com.ancrette.gesource.db.remote;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface RemoteDataSource {

    LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw);

    LiveData<Boolean> insert(Fountain fountain);

    LiveData<Boolean> delete(Fountain fountain);
}
