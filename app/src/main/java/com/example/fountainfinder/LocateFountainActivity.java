package com.example.fountainfinder;

import android.annotation.SuppressLint;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import com.example.fountainfinder.db.AppDatabase;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.maps.*;
import com.google.android.gms.maps.model.*;
import com.google.maps.android.clustering.ClusterManager;

import java.util.List;

public class LocateFountainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    //TODO. ADD DEPENDENCY INJECTION
    private AppDatabase db;
    private ClusterManager<Fountain> clusterManager;
    private static final LatLng DEFAULT_LOCATION_GENEVA = new LatLng(46.12266, 6.09212);
    private LatLng userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_fountain);
        if (db == null)
            db = AppDatabase.getDatabase(this);
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
        if (userLocation == null)
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(DEFAULT_LOCATION_GENEVA));
        else googleMap.moveCamera(CameraUpdateFactory.newLatLng(userLocation));

        LatLngBounds latLngBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

        clusterManager = new ClusterManager<>(this, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        // Retrieve all markers from the database
        LiveData<List<Fountain>> allFountains = db.getAll(this, latLngBounds.southwest, latLngBounds.northeast);

        allFountains.observe(this, fountains -> {
            for (Fountain fountain : fountains)
                clusterManager.addItem(fountain);
        });
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        this.userLocation = new LatLng(location.getLatitude(), location.getLongitude());
    }
}