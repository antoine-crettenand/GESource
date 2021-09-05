package com.example.fountainfinder.db.remote.scrapper;

import android.app.Activity;
import androidx.lifecycle.LiveData;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.Collection;

public interface RemoteDataSource {

    LiveData<Collection<Fountain>> fetch(Activity activity, LatLng ne, LatLng sw);
}
