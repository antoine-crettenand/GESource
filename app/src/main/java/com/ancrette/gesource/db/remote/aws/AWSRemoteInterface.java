package com.ancrette.gesource.db.remote.aws;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.api.graphql.model.ModelQuery;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.AWSFountain;
import com.ancrette.gesource.db.Fountain;
import com.ancrette.gesource.db.remote.RemoteDataSourceWithBackup;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.google.android.gms.maps.model.LatLng;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;


public class AWSRemoteInterface extends RemoteDataSourceWithBackup {
    private static final String TAG = AWSRemoteInterface.class.getSimpleName();

    public AWSRemoteInterface(RemoteDataSource backupDataSource) {
        super(backupDataSource);
    }

    @Override
    public LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw) {
        MutableLiveData<Collection<Fountain>> fountains = new MutableLiveData<>();
        Amplify.API.query(ModelQuery.list(AWSFountain.class, AWSFountain.LATITUDE.between(ne.latitude, sw.latitude).and(AWSFountain.LONGITUDE.between(ne.longitude, sw.longitude).and(AWSFountain.ACTIVE.eq(true)))), response -> {
            Log.d(TAG, String.format(Locale.GERMAN, "Borders | latitude [%f, %f] | longitude [%f, %f]", ne.latitude, sw.latitude, ne.longitude, sw.longitude));
            Log.d(TAG, response.getData().getItems().toString());
            fountains.postValue(Collections.unmodifiableCollection(AWSFountainsToFountains(response.getData().getItems())));
        }, error -> {
            Log.d(TAG, "Query failure. Trying with backup remote data source", error);
//            Handler handler = new android.os.Handler(Looper.getMainLooper());
  //          handler.post(() -> fetchBackup(ne, sw, activity).observe(((FragmentActivity) activity), value -> fountains.get().postValue(Collections.unmodifiableCollection(value))));
           // fountains(MutableLiveData<Collection<Fountain>>) fetchBackup(ne, sw));
            Transformations.switchMap(fountains, result -> fetchBackup(ne, sw));
        });

        return fountains;
    }

    @Override
    public LiveData<Boolean> insert(Fountain fountain) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        AWSFountain f = castFountainToAWSFountain(fountain);
        Amplify.API.mutate(ModelMutation.create(f), response -> {
            Log.d(TAG, String.format(Locale.FRENCH, "Fountain with id: %s with coordinates (%f, %f)", response.getData().getId(), response.getData().getLatitude(), response.getData().getLongitude()));
            success.postValue(true);
        }, error -> {
            Log.e(TAG, "Create failed", error);
            success.postValue(false);
        });
        return success;
    }

    @Override
    public LiveData<Boolean> delete(Fountain fountain) {
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        fountain.active = false;
        AWSFountain f = castFountainToAWSFountain(fountain);
        Amplify.API.mutate(ModelMutation.update(f), response -> {
            Log.d(TAG, String.format(Locale.FRENCH, "Fountain with id: %s with coordinates (%f, %f)", response.getData().getId(), response.getData().getLatitude(), response.getData().getLongitude()));
            success.postValue(true);
        }, error -> {
            Log.e(TAG, "Create failed", error);
            success.postValue(false);
        });
        return success;
    }

    private static Collection<Fountain> AWSFountainsToFountains(Iterable<AWSFountain> awsFountains){
        List<Fountain> queryAnswer = new ArrayList<>();
        for (AWSFountain f : awsFountains)
            queryAnswer.add(castAWSFountainToFountain(f));
        return  queryAnswer;
    }

    private static Fountain castAWSFountainToFountain(AWSFountain f) {
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
