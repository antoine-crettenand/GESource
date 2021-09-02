package com.example.fountainfinder.db;

import com.example.fountainfinder.scrapper.GESoifScrapper;
import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

import static org.junit.Assert.*;


public class AppDatabaseTest {

    List<Fountain> fountains;

    @Before
    public void initMockData() throws FileNotFoundException, JSONException {
        File file = new File(System.getProperty("user.dir") + "/../db_json.txt");
        Scanner scanner = new Scanner(file);
        String response = scanner.nextLine();
        this.fountains = GESoifScrapper.getInstance().JSONToFountainList(response);
    }

    private Set<Fountain> helperInstersectionOfCollections(Collection<Fountain> x, Collection<Fountain> y) {
        Set<Fountain> buffer = new HashSet<>(x);

        for (Fountain i : x) {
            if (y.contains(i)) {
                buffer.remove(i);
            }
        }

        return buffer;
    }

    @Test
    public void doubleSanitizeShouldNotHaveAnyAdditionnalEffects() throws Exception {
        List<Fountain> sanitizedDataA = AppDatabase.sanitize(fountains);
        List<Fountain> sanitizedDataB = AppDatabase.sanitize(sanitizedDataA);

        sanitizedDataA.sort(Comparator.comparing(o -> o.title));
        sanitizedDataB.sort(Comparator.comparing(o -> o.title));

        assertArrayEquals(sanitizedDataA.toArray(), sanitizedDataB.toArray());
    }

    @Test
    public void sanitizeTest() throws Exception {
        List<Fountain> sanitizedData = AppDatabase.sanitize(fountains);
        sanitizedData.sort(Comparator.comparing(o -> o.title));
        for (Fountain f : sanitizedData) {
            //        System.out.println(f.title);
        }

        Set<Fountain> removedFountains = helperInstersectionOfCollections(fountains, sanitizedData);

        System.out.println("Removed Fountains....");
        for (Fountain i : removedFountains)
            System.out.printf("%s = %s = (%f, %f) \n", i.title, i.address, i.latitude, i.longitude);

        System.out.println(fountains.size());
        System.out.printf("%d fountains were removed which is about %f percent of the total dataset%n", removedFountains.size(), (double) removedFountains.size() / fountains.size() * 100d);
    }
}