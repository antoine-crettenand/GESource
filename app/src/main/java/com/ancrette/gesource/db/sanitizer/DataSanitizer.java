package com.ancrette.gesource.db.sanitizer;

import com.ancrette.gesource.db.Fountain;
import com.google.android.gms.maps.model.LatLng;

import java.util.*;

public class DataSanitizer {
    private static final String[] TITLE_BLOCKLIST = {"undefined", "null", "false"};
    private static final String[] ADDRESS_BLOCKLIST = {"false"};

    private static final String TAG = DataSanitizer.class.getSimpleName();
    private static final double CLUSTER_SIZE_THRESHOLD = 25; //in meters

    private double toRad(double value) {
        return value * Math.PI / 180d;
    }

    private synchronized double haversine(LatLng f1, LatLng f2) {
        final double UNIT_KM_TO_M = 1000d; // ration m/km
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
        //   System.out.printf("Result from haversine : %f\n", ((float) result));
        return R * c * UNIT_KM_TO_M;
    }

    private static boolean contains(String x, String... y) {
        for (String s : y) {
            if (x.contains(s))
                return true;
        }
        return false;
    }

    public static boolean isSane(Fountain f) {
        return !contains(f.address, ADDRESS_BLOCKLIST) && !contains(f.title, TITLE_BLOCKLIST);
    }

    // Naive O(n^2) algorithm
    // Reordering happens
    public Collection<Fountain> sanitize(Collection<Fountain> source) {

        Collection<Fountain> individuallySanitizedSource = new ArrayList<>();

        int numberOfUnsaneData = 0;
        int numberOfSubstringTitle = 0;
        int numberOfTooClose = 0;

        for (Fountain f : source) {
            boolean fIsTheClusterCenter = true;

            if (!isSane(f)) {
                numberOfUnsaneData++;
                continue;
            }
            for (Fountain g : source) {
                if (f.equals(g))
                    continue;

                if (haversine(f.getPosition(), g.getPosition()) <= CLUSTER_SIZE_THRESHOLD)
                    if (g.title.contains(f.title)) {
                        fIsTheClusterCenter = false;
                        numberOfSubstringTitle++;
                    } else {
                        numberOfTooClose++;
                    }
            }
            if (fIsTheClusterCenter)
                individuallySanitizedSource.add(f);
        }

        System.out.printf("%s :: numberOfUnsaneData = %d numberOfSubstringleTitle = %d numberofTooClose = %d", TAG, numberOfUnsaneData, numberOfSubstringTitle, numberOfTooClose);

        Arrays.sort(individuallySanitizedSource.toArray(new Fountain[0]), (o1, o2) -> o1.title.compareTo(o2.title));
        return Collections.unmodifiableCollection(individuallySanitizedSource);
    }
}
