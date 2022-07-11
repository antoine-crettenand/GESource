package com.ancrette.gesource;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.ancrette.gesource.db.Fountain;
import com.ancrette.gesource.db.FountainDataRepository;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.clustering.ClusterManager;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.Collection;

@AndroidEntryPoint
public class LocateFountainActivity extends AppCompatActivity implements OnMapReadyCallback {

    @Inject
    public FountainDataRepository fountainDataRepository;

    private static final float DEFAULT_ZOOM = 15;
    private static final LatLng DEFAULT_LOCATION_GENEVA = new LatLng(46.12266, 6.09212);
    private boolean locationPermissionGranted;
    private final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private GoogleMap map;
    private final String TAG = LocateFountainActivity.class.getSimpleName();
    private static Location lastKnownLocation;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private ClusterManager<Fountain> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_fountain);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * <p>
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.map = googleMap;
        this.clusterManager = new ClusterManager<>(this, map);
        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        LatLngBounds latLngBounds = map.getProjection().getVisibleRegion().latLngBounds;

        // Query the repository for all surrounding fountains
        LiveData<Collection<Fountain>> allFountains = fountainDataRepository.scan(latLngBounds.southwest, latLngBounds.northeast);

        allFountains.observe(this, collection -> {
            if (collection.isEmpty())
                Toast.makeText(this, "An error occurred loading your fountains!", Toast.LENGTH_LONG).show();
            else
                clusterManager.addItems(collection);
        });
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
                        //            if (lastKnownLocation != null) {
                        //              map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                        //                    new LatLng(lastKnownLocation.getLatitude(),
                        //                          lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        //    }
                        success.postValue(true);
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.");
                        Log.e(TAG, "Exception: %s", task.getException());
                        map.moveCamera(CameraUpdateFactory
                                .newLatLngZoom(DEFAULT_LOCATION_GENEVA, DEFAULT_ZOOM));
                        map.getUiSettings().setMyLocationButtonEnabled(false);
                        success.postValue(false);
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, e.getMessage(), e);
        }
        return success;
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
        updateLocationUI();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
                // Get the current location of the device and set the position of the map.
                getDeviceLocation().observe(this, success -> {
                    if (success)
                        map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude())));
                    else
                        map.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION_GENEVA));
                });
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
//                getLocationPermission();
            }
        } catch (SecurityException e) {
            Log.e("Exception: %s", e.getMessage());
        }
    }

}