package com.example.fountainfinder.db.sanitizer;

import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class DataSanitizer {
    private double toRad(double value) {
        return value * Math.PI / 180d;
    }

    private synchronized double haversine(LatLng f1, LatLng f2) {
        final double UNIT_KM_TO_M = .001d; // ration m/km
        final double R = 6371.008d; // Radius of the earth, in km
        double lat1 = f1.latitude;
        double lat2 = f2.latitude;
        double lon1 = f1.longitude;
        double lon2 = f2.longitude;
        double latDistance = toRad(lat2 - lat1);
        double lonDistance = toRad(lon2 - lon1);
        double a = Math.sin(latDistance / 2d) * Math.sin(latDistance / 2d) +
                Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) *
                        Math.sin(lonDistance / 2d) * Math.sin(lonDistance / 2d);
        double c = 2d * Math.atan2(Math.sqrt(a), Math.sqrt(1d - a));
        double result = R * c * UNIT_KM_TO_M;
        //   System.out.printf("Result from haversine : %f\n", ((float) result));
        return result;
    }

    private boolean isSane(Fountain f) {
        if (f.title.contains("undefined"))
            return false;

        if (f.address.contains("false"))
            return false;

        return !f.title.contains("null") && !f.title.contains("false");
    }

    // Naive O(n^2) algorithm
    // Reordering happens
    public Collection<Fountain> sanitize(Collection<Fountain> source) {
        Collection<Fountain> individuallySanitizedSource = Collections.synchronizedCollection(new ArrayList<>());

        for (Fountain f : Collections.synchronizedCollection(source)) {
            boolean fIsTheClusterCenter = true;

            if (!isSane(f))
                continue;

            for (Fountain g : source) {
                if (f.equals(g))
                    continue;

                if (haversine(f.getPosition(), g.getPosition()) < 1)
                    if (f.title.contains(g.title) && !f.title.equals(g.title)) {
                        fIsTheClusterCenter = false;
                    } else
                        fIsTheClusterCenter = false;
            }

            if (fIsTheClusterCenter)
                individuallySanitizedSource.add(f);
        }
        return Collections.unmodifiableCollection(individuallySanitizedSource);
    }
}
