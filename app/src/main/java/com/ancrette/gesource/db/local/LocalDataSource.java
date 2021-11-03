package com.ancrette.gesource.db.local;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface LocalDataSource {
    LiveData<Collection<Fountain>> fetch(Activity activity, LatLng ne, LatLng sw);

    LiveData<Boolean> update(Collection<Fountain> fountains);
}
