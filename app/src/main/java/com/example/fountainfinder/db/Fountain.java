package com.example.fountainfinder.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Random;

@Entity
public final class Fountain implements ClusterItem {

    public Fountain() {
    }

    @Ignore
    public Fountain(String name, double latitude, double longitude) {
        this.id = new Random().nextInt();
        this.title = name;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Ignore
    public Fountain(int id, String title, double latitude, double longitude, float time, String address, int active, int nbottles, String img, String source, int reported) {
        this.id = id;
        this.title = title;
        this.latitude = latitude;
        this.longitude = longitude;
        this.time = time;
        this.address = address;
        this.active = active;
        this.nbottles = nbottles;
        this.img = img;
        this.source = source;
        this.reported = reported;
    }

    @PrimaryKey
    public int id;

    @ColumnInfo(name = "name")
    public String title;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "time")
    public float time;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "active")
    public int active;

    @ColumnInfo(name = "nbottles")
    public int nbottles;

    @ColumnInfo(name = "img")
    public String img;

    @ColumnInfo(name = "source")
    public String source;

    @ColumnInfo(name = "reported")
    public int reported;

    @NonNull
    @Override
    public LatLng getPosition() {
        return new LatLng(latitude, longitude);
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return address;
    }
}
