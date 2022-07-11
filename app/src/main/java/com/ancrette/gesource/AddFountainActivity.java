package com.ancrette.gesource;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ancrette.gesource.db.Fountain;
import com.ancrette.gesource.db.FountainDataRepository;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.tasks.Task;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.Locale;

@AndroidEntryPoint
public class AddFountainActivity extends AppCompatActivity {
    private static final String TAG = AddFountainActivity.class.getSimpleName();
    private boolean locationPermissionGranted = false;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static Location lastKnownLocation;

    @Inject
    FountainDataRepository fountainDataRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fountain);
        init();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
    }


    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        locationPermissionGranted = false;
        if (requestCode == PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION) {// If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationPermissionGranted = true;
            }
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    private LiveData<Boolean> getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        MutableLiveData<Boolean> success = new MutableLiveData<>();
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.getResult();
                        success.postValue(true);
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        success.postValue(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage(), e);
        }
        return success;
    }

    private void init() {
        linkBtnToSubmit(findViewById(R.id.btnToSubmitRequest));
    }

    private void linkBtnToSubmit(Button btn) {
        btn.setOnClickListener(v -> {
            getLocationPermission();

            EditText nameTV = findViewById(R.id.nameEditText);
            getDeviceLocation().observe(this, success -> {
                if (success) {
                    Log.i(TAG, String.format(Locale.FRENCH, "Location is %s", lastKnownLocation));
                    String name = nameTV.getText().toString();

                    double latitude = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();
                    Fountain ft = new Fountain(name, latitude, longitude, false);

                    fountainDataRepository.insert(ft).observe(this, value -> {
                        if (value.isSuccessfull()) {
                            Toast.makeText(this, "Successfully saved your fountain!", Toast.LENGTH_SHORT).show();
                            //            finish();
                        } else {
                            if (value.haveRemoteSourceFailed())
                                Toast.makeText(this, "A problem occurred while saving your fountain remotely! Try again!", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    Toast.makeText(this, "It seems like we cannot locate you! You have to enable your localisation in your security parameters.", Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}