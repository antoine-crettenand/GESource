package com.ancrette.gesource;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.amplifyframework.api.graphql.model.ModelMutation;
import com.amplifyframework.core.Amplify;
import com.amplifyframework.datastore.generated.model.AWSFountain;
import com.ancrette.gesource.db.FountainDataRepository;
import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.tasks.Task;
import dagger.hilt.android.AndroidEntryPoint;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;

@AndroidEntryPoint
public class AddFountainActivity extends AppCompatActivity {
    private static final String TAG = AddFountainActivity.class.getSimpleName();
    private FusedLocationProviderClient fusedLocationProviderClient;

    @Inject
    FountainDataRepository fountainDataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fountain);
        init();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }

    private void init() {
        linkBtnToSubmit(findViewById(R.id.btnToSubmitRequest));
    }

    private void linkBtnToSubmit(Button btn) {
        btn.setOnClickListener(v -> {
            EditText nameTV = findViewById(R.id.nameEditText);

            Location location = LocateFountainActivity.getLocation();

            if (location != null) {
                String name = nameTV.getText().toString();
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                Fountain ft = new Fountain(name, latitude, longitude);

                fountainDataRepository.insert( ft, this).observe(this, value -> {
                    if (value) {
                        Toast.makeText(this, "Successfully saved your fountain!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else
                    {
                        Toast.makeText(this, "A problem occurred while saving your fountain!", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
            } else
                Toast.makeText(this, "It seems like we cannot locate you! Please specify your position on the map!", Toast.LENGTH_SHORT).show();
        });
    }
}