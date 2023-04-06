package com.ancrette.gesource;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
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
import java.util.Objects;

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

        setSupportActionBar(findViewById(R.id.my_toolbar));
        setUpActionBar();

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

        clusterManager.setOnClusterItemInfoWindowLongClickListener(item -> new MaterialDialog.Builder(this)
                .title("Do you want to remove this fountain ?")
                .onPositive((dialog, which) -> {
                    Log.d(TAG, "Deleting fountain " + item.toString());
                    fountainDataRepository.delete(item).observe(this, requestErrorStatus -> {
                        if (requestErrorStatus.isSuccessful())
                            clusterManager.removeItem(item);
                    });
                })
                .positiveText("Yes")
                .negativeText("Cancel")
                .show());

        map.setOnCameraIdleListener(clusterManager);
        map.setOnMarkerClickListener(clusterManager);

        UiSettings uiSettings = map.getUiSettings();
        uiSettings.setZoomControlsEnabled(true);

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the Location layer and the related control on the map.
        updateLocationUI();

        // scan for all fountains
        populateMapWithFountains(new LatLngBounds(new LatLng(-90, -180), new LatLng(90, 179)));
    }

    public void populateMapWithFountains(LatLngBounds latLngBounds) {
        // Query the repository for all surrounding fountains
        fountainDataRepository.scan(latLngBounds.southwest, latLngBounds.northeast).observe(this, collection -> {
         /*   if (collection.isEmpty())
                Toast.makeText(this, "An error occurred loading your fountains!", Toast.LENGTH_LONG).show();
            else*/
                clusterManager.addItems(collection);
        });
    }

    public void setUpActionBar() {
        ActionBar actionBar = getSupportActionBar();

        Objects.requireNonNull(actionBar);

        // providing title for the ActionBar
        actionBar.setTitle("GESource");

        // providing subtitle for the ActionBar
        //actionBar.setSubtitle("   Design a custom Action Bar");

        // adding icon in the ActionBar.
        //   actionBar.setIcon(R.mipmap.app_icon_foreground);
        //   actionBar.setLogo(R.mipmap.app_icon_foreground);

        // methods to display the icon in the ActionBar
        //  actionBar.setDisplayUseLogoEnabled(true);
        //   actionBar.setDisplayShowHomeEnabled(true);
    }

    // method to inflate the options menu when
    // the user opens the menu for the first time
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // methods to control the operations that will
    // happen when user clicks on the action buttons
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.search) {
            new MaterialDialog.Builder(this)
                    .title("Suggest a (not-listed) fountain")
                    .content("It seems that you found an unregistered fountain! Make sure you have provided a name & stand close to it before submitting.")
                    .input("Fountain's name", "", false, (dialog, input) -> {
                        EditText inputEditText = dialog.getInputEditText();
                        if (inputEditText != null) {
                            String name = inputEditText.getText().toString();
                            double latitude = lastKnownLocation.getLatitude();
                            double longitude = lastKnownLocation.getLongitude();
                            Fountain ft = new Fountain(name, latitude, longitude, false);
                            fountainDataRepository.insert(ft).observe(this, errorStatus -> {
                                if (errorStatus.isSuccessful()) {
                                    Toast.makeText(this, "Thank you for your help! Our team will now review it!", Toast.LENGTH_SHORT).show();
                                } else {
                                    if (errorStatus.haveRemoteSourceFailed())
                                        Toast.makeText(this, "A problem occurred while saving your fountain remotely! Try again!", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .positiveText("Submit")
                    .negativeText("Cancel")
                    .show();
        } else if (item.getItemId() == R.id.refresh) {
            populateMapWithFountains(map.getProjection().getVisibleRegion().latLngBounds);
        }
        return super.onOptionsItemSelected(item);
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
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(lastKnownLocation.getLatitude(),
                                            lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                        }
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