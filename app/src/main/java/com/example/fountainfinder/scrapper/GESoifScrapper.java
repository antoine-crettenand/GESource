package com.example.fountainfinder.scrapper;

import android.app.Activity;
import android.util.JsonReader;
import android.util.Log;
import android.util.MalformedJsonException;
import android.widget.Toast;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.work.impl.utils.LiveDataUtils;
import com.android.volley.*;
import com.android.volley.toolbox.*;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.common.util.JsonUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.function.BiFunction;

public class GESoifScrapper {
    private static final String apiURLFormat = "https://www.ge-soif.ch/api/fountain/read_radius.php?swLat=%f&swLng=%f&neLat=%f&neLng=%f";
    private static final String TAG = "GESOIF_SCRAPPER";

    public static LiveData<List<Fountain>> getFountainsFromRadius(Activity activity, float swLat, float swLong, float neLat, float neLong) {
        MutableLiveData<List<Fountain>> mutableLiveData = new MutableLiveData<>();
        String path = String.format(Locale.FRENCH, apiURLFormat, swLat, swLong, neLat, neLong);

        // Instantiate the cache
        Cache cache = new DiskBasedCache(activity.getCacheDir(), 1024 * 1024); // 1MB cap

        // Set up the network to use HttpURLConnection as the HTTP client.
        Network network = new BasicNetwork(new HurlStack());

        // Instantiate the RequestQueue.
        RequestQueue queue = new RequestQueue(cache, network);
        queue.start();

        StringRequest stringRequest = new StringRequest(Request.Method.GET, path, response -> {
            try {
                JSONArray jsonObject = new JSONArray(response);
                List<Fountain> fountains = new ArrayList<>();
                for (int i = 0; i < jsonObject.length(); i++) {
                    JSONObject fountain = jsonObject.getJSONObject(i);
                    String id = fountain.getString("id");
                    String title = fountain.getString("title");
                    double latitude = fountain.getDouble("latitude");
                    double longitude = fountain.getDouble("longitude");

                    Fountain f = new Fountain(title, latitude, longitude);
                    fountains.add(f);
                }
                mutableLiveData.setValue(fountains);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }, error -> Toast.makeText(activity.getApplicationContext(), "An error occurred retrieving data. Try again later!", Toast.LENGTH_SHORT).show());
        queue.add(stringRequest);

        return mutableLiveData;
    }
}


