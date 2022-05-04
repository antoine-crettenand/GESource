package com.ancrette.gesource.db.remote.scrapper;

import android.app.Activity;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ancrette.gesource.db.remote.RemoteDataSource;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class GESoifScrapper implements RemoteDataSource {
    private static final String GET_API_STRING_FORMAT = "https://www.ge-soif.ch/api/fountain/read_radius.php?swLat=%f&swLng=%f&neLat=%f&neLng=%f";
    private static final String POST_API_STRING_FORMAT = "https://www.ge-soif.ch/api/fountain/add_fountain.php";
    private static final String TAG = GESoifScrapper.class.getSimpleName();

    @Override
    public LiveData<Collection<Fountain>> scanWithinBorders(LatLng ne, LatLng sw, Activity activity) {
        return scanWithinBordersHelper(ne.latitude, ne.longitude, sw.latitude, sw.longitude, activity);
    } 

    @Override
    public LiveData<Boolean> insert(Fountain fountain, Activity activity) {
        MutableLiveData<Boolean> mutableLiveData = new MutableLiveData<>(false);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.start();

        JSONObject postData = new JSONObject();

        try {
            postData.put("id", fountain.id);
            postData.put("title", fountain.title);
            postData.put("latitude", fountain.latitude);
            postData.put("longitude", fountain.longitude);
            postData.put("time", fountain.time);
            postData.put("address", fountain.address);
            postData.put("active", 0);
            postData.put("nBottles", 0);
            postData.put("img", "");
            postData.put("source", "null");
            postData.put("reported", 0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        JsonObjectRequest fountainRequest = new JsonObjectRequest(Request.Method.POST, POST_API_STRING_FORMAT, postData, response -> {
            mutableLiveData.postValue(true);
            queue.stop();
        }, error -> mutableLiveData.postValue(false));
        queue.add(fountainRequest);
        return mutableLiveData;
    }

    private LiveData<Collection<Fountain>> scanWithinBordersHelper(double swLat, double swLong, double neLat, double neLong, Activity activity) {
        MutableLiveData<Collection<Fountain>> mutableLiveData = new MutableLiveData<>();
        String path = String.format(Locale.FRENCH, GET_API_STRING_FORMAT, swLat, swLong, neLat, neLong);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.start();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, path, response -> {
            try {
                mutableLiveData.setValue(castJSONStringAsCollectionOfFountain(response));
                queue.stop();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> {
            Toast.makeText(activity.getApplicationContext(), "An error occurred retrieving data. Try again later!", Toast.LENGTH_SHORT).show();
            queue.stop();
        });
        queue.add(stringRequest);

        return mutableLiveData;
    }

    private Fountain castJSONToFountain(JSONObject jsonObject) throws JSONException {
        String id = jsonObject.getString("id");
        String title = jsonObject.getString("title");
        double latitude = jsonObject.getDouble("latitude");
        double longitude = jsonObject.getDouble("longitude");
        String time = jsonObject.getString("time");
        String address = jsonObject.getString("address");
        boolean active = jsonObject.getBoolean("active");
        int nbottles = jsonObject.getInt("nbottles");
        String img = jsonObject.getString("img");
        String source = jsonObject.getString("source");
        String reported = jsonObject.getString("reported");
        return new Fountain(id, title, latitude, longitude, time, address, active, nbottles, img, source, reported);
    }

    public Collection<Fountain> castJSONStringAsCollectionOfFountain(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        List<Fountain> fountains = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            fountains.add(castJSONToFountain(jsonObject));
        }
        return fountains;
    }

}


