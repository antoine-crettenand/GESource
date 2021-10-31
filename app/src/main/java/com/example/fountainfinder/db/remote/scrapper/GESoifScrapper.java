package com.example.fountainfinder.db.remote.scrapper;

import android.app.Activity;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class GESoifScrapper implements RemoteDataSource {
    private static final String GET_API_STRING_FORMAT = "https://www.ge-soif.ch/api/fountain/read_radius.php?swLat=%f&swLng=%f&neLat=%f&neLng=%f";
    private static final String POST_API_STRING_FORMAT = "https://www.ge-soif.ch/api/fountain/add_fountain.php";
    private static final String TAG = GESoifScrapper.class.getSimpleName();

    public LiveData<Collection<Fountain>> getFountainsFromRadius(Activity activity, double swLat, double swLong, double neLat, double neLong) {
        MutableLiveData<Collection<Fountain>> mutableLiveData = new MutableLiveData<>();
        String path = String.format(Locale.FRENCH, GET_API_STRING_FORMAT, swLat, swLong, neLat, neLong);

        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.start();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, path, response -> {
            try {
                mutableLiveData.setValue(convertJSONToFountainCollection(response));
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

    @Override
    public LiveData<Boolean> update(Activity activity, Fountain fountain) {
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
        } catch (Exception e){
            e.printStackTrace();
        }

        JsonObjectRequest fountainRequest = new JsonObjectRequest(Request.Method.POST, POST_API_STRING_FORMAT, postData, response -> {
            mutableLiveData.setValue(true);
            queue.stop();
        }, null);
        queue.add(fountainRequest);
        return mutableLiveData;
    }

    public Collection<Fountain> convertJSONToFountainCollection(String data) throws JSONException {
        JSONArray jsonArray = new JSONArray(data);
        List<Fountain> fountains = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String id = jsonObject.getString("id");
            String title = jsonObject.getString("title");
            double latitude = jsonObject.getDouble("latitude");
            double longitude = jsonObject.getDouble("longitude");
            String time = jsonObject.getString("time");
            String address = jsonObject.getString("address");
            String active = jsonObject.getString("active");
            int nbottles = jsonObject.getInt("nbottles");
            String img = jsonObject.getString("img");
            String source = jsonObject.getString("source");
            String reported = jsonObject.getString("reported");

            Fountain f = new Fountain(id, title, latitude, longitude, time, address, active, nbottles, img, source, reported);
            fountains.add(f);
        }
        return fountains;
    }

    @Override
    public LiveData<Collection<Fountain>> fetch(Activity activity, LatLng ne, LatLng sw) {
        return getFountainsFromRadius(activity, ne.latitude, ne.longitude, sw.latitude, sw.longitude);
    }
}


