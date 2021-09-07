package com.example.fountainfinder.db;

import com.example.fountainfinder.db.remote.scrapper.GESoifScrapper;
import com.example.fountainfinder.db.sanitizer.DataSanitizer;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.*;


public class AppDatabaseTest {

    Collection<Fountain> fountains;
    DataSanitizer dataSanitizer;

    @Before
    public void initMockData() throws FileNotFoundException, JSONException {
        File file = new File(System.getProperty("user.dir") + "/../db_json.txt");
        Scanner scanner = new Scanner(file);
        String response = scanner.nextLine();
        this.fountains = new GESoifScrapper().convertJSONToFountainCollection(response);
        this.dataSanitizer = new DataSanitizer();
    }

    private Set<Fountain> helperSubtractionOfCollections(Collection<Fountain> x, Collection<Fountain> y) {
        Set<Fountain> buffer = new HashSet<>(x);
        buffer.removeIf(y::contains);
        return buffer;
    }

    @Test
    public void doubleSanitizeShouldNotHaveAnyAdditionnalEffects() {
        Collection<Fountain> sanitizedDataA = dataSanitizer.sanitize(fountains);
        List<Fountain> sanitizedDataAbis = new ArrayList<>(sanitizedDataA);

        Collection<Fountain> sanitizedDataB = dataSanitizer.sanitize(sanitizedDataAbis);
        List<Fountain> sanitizedDataBbis = new ArrayList<>(sanitizedDataB);

        sanitizedDataAbis.sort(Comparator.comparing(o -> o.title));
        sanitizedDataBbis.sort(Comparator.comparing(o -> o.title));

        assertArrayEquals(sanitizedDataAbis.toArray(), sanitizedDataBbis.toArray());
    }

    @Test
    public void sanitizeTest() {
        Collection<Fountain> sanitizedData = dataSanitizer.sanitize(fountains);
        List<Fountain> sanitizedDatabis = new ArrayList<>(sanitizedData);

        sanitizedDatabis.sort(Comparator.comparing(o -> o.title));
        for (Fountain f : sanitizedData) {
            //        System.out.println(f.title);
        }

        Set<Fountain> removedFountains = helperSubtractionOfCollections(fountains, sanitizedData);

        System.out.println("Removed Fountains....");
        for (Fountain i : removedFountains)
            System.out.printf("%s = %s = (%f, %f) \n", i.title, i.address, i.latitude, i.longitude);

        System.out.println(fountains.size());
        System.out.printf("%d fountains were removed which is about %f percent of the total dataset%n", removedFountains.size(), (double) removedFountains.size() / fountains.size() * 100d);
    }
}