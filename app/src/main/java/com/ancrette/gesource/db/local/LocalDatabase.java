package com.ancrette.gesource.db.local;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface LocalDatabase {

    LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw);

    LiveData<Boolean> insert(Fountain f);

    LiveData<Boolean> insertAll(Collection<Fountain> fountains);
}
