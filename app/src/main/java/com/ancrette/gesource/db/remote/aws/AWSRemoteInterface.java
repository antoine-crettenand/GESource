package com.ancrette.gesource.db.remote.aws;

import android.app.Activity;
import android.util.Log;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.AWSFountain;
import com.ancrette.gesource.db.Fountain;
import com.ancrette.gesource.db.remote.DataSourceWithBackup;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class AWSRemoteInterface extends DataSourceWithBackup {
    private static final String TAG = AWSRemoteInterface.class.getSimpleName();

    public AWSRemoteInterface(RemoteDataSource backupDataSource) {
        super(backupDataSource);
    }


    @Override
    public LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw, Activity activity) {
        MutableLiveData<Collection<Fountain>> fountains = new MutableLiveData<>();
        Amplify.API.query(ModelQuery.list(AWSFountain.class, AWSFountain.LATITUDE.between(ne.latitude, sw.latitude).and(AWSFountain.LONGITUDE.between(ne.longitude, sw.longitude))), response -> {
            Log.e(TAG, response.getData().toString());

            List<Fountain> queryAnswer = new ArrayList<>();
            for (AWSFountain f : response.getData())
                queryAnswer.add(AWSFountainToLocalFountain(f));
            fountains.postValue(Collections.unmodifiableCollection(queryAnswer));
        }, error -> {
            Log.e(TAG, "Query failure. Trying with backup remote data source", error);
            fetchBackup(ne, sw, activity).observe(((FragmentActivity) activity),value -> fountains.postValue(Collections.unmodifiableCollection(value)));
        });

        return fountains;
    }

    @Override
    public LiveData<Boolean> insert(Fountain fountain, Activity activity) {
        MutableLiveData<Boolean> success = new MutableLiveData<>(false);
        AWSFountain f = castFountainToAWSFountain(fountain);
        Amplify.API.mutate(ModelMutation.create(f), response -> {
            Log.i(TAG, "Fountain with id: " + response.getData().getId());
            success.postValue(true);
        }, error -> {
            Log.e(TAG, "Create failed", error);
            success.postValue(false);
        });
        return success;
    }

    private static Fountain AWSFountainToLocalFountain(AWSFountain f) {
        String id = f.getId();
        String title = f.getTitle();
        Double latitude = f.getLatitude();
        Double longitude = f.getLongitude();
        String time = f.getTime();
        String address = f.getAddress();
        boolean active = f.getActive();
        int nbottles = f.getNbottles();
        String img = f.getImg();
        return new Fountain(id, title, latitude, longitude, time, address, active, nbottles, img, "", "");
    }

    private static AWSFountain castFountainToAWSFountain(Fountain f) {
//        String id = String.valueOf(f.id);
        String title = f.title;
        Double latitude = f.latitude;
        Double longitude = f.longitude;
        String time = f.time;
        String address = f.address;
        boolean active = f.active;
        int nbottles = f.nbottles;
        String img = f.img;
        return AWSFountain.builder().title(title).latitude(latitude).longitude(longitude).time(time).address(address).active(active).nbottles(nbottles).img(img).build();
    }
}
