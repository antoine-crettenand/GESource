package com.ancrette.gesource.db.local;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface LocalDataSource {

    LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw, Activity activity);

    LiveData<Boolean> insert(Fountain f, Activity activity);

    LiveData<Boolean> insertAll(Collection<Fountain> fountains);
}
