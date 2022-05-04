package com.ancrette.gesource.db;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

import java.util.Random;

/**
 * Class abstracts a fountain as an entity with a name, location (in spherical coordinates) and other miscellaneous.
 */
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
    public Fountain(String id, String title, double latitude, double longitude, String time, String address, boolean active, int nbottles, String img, String source, String reported) {
        this.id = new Random().nextInt();
        this.idGE = id;
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

    public String idGE;

    @ColumnInfo(name = "name")
    public String title;

    @ColumnInfo(name = "latitude")
    public double latitude;

    @ColumnInfo(name = "longitude")
    public double longitude;

    @ColumnInfo(name = "time")
    public String time;

    @ColumnInfo(name = "address")
    public String address;

    @ColumnInfo(name = "active")
    public boolean active;

    @ColumnInfo(name = "nbottles")
    public int nbottles;

    @ColumnInfo(name = "img")
    public String img;

    @ColumnInfo(name = "source")
    public String source;

    @ColumnInfo(name = "reported")
    public String reported;

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
        return getPosition() + "\n" + address;
    }

    @Override
    public String toString() {
        return "Fountain{" +
                "id=" + id +
                ", idGE='" + idGE + '\'' +
                ", title='" + title + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", time='" + time + '\'' +
                ", address='" + address + '\'' +
                ", active='" + active + '\'' +
                ", nbottles=" + nbottles +
                ", img='" + img + '\'' +
                ", source='" + source + '\'' +
                ", reported='" + reported + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Fountain fountain = (Fountain) o;

        if (id != fountain.id) return false;
        if (Double.compare(fountain.latitude, latitude) != 0) return false;
        if (Double.compare(fountain.longitude, longitude) != 0) return false;
        if (active != fountain.active) return false;
        if (nbottles != fountain.nbottles) return false;
        if (idGE != null ? !idGE.equals(fountain.idGE) : fountain.idGE != null) return false;
        if (title != null ? !title.equals(fountain.title) : fountain.title != null) return false;
        if (time != null ? !time.equals(fountain.time) : fountain.time != null) return false;
        if (address != null ? !address.equals(fountain.address) : fountain.address != null) return false;
        if (img != null ? !img.equals(fountain.img) : fountain.img != null) return false;
        if (source != null ? !source.equals(fountain.source) : fountain.source != null) return false;
        return reported != null ? reported.equals(fountain.reported) : fountain.reported == null;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = id;
        result = 31 * result + (idGE != null ? idGE.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        temp = Double.doubleToLongBits(latitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(longitude);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (address != null ? address.hashCode() : 0);
        result = 31 * result + (active ? 1 : 0);
        result = 31 * result + nbottles;
        result = 31 * result + (img != null ? img.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (reported != null ? reported.hashCode() : 0);
        return result;
    }
}
