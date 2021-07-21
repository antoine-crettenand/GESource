package com.example.fountainfinder.db;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import java.util.List;

public class FountainRepository {


    private FountainDao fountainDao;
    private LiveData<List<Fountain>> allFountains;

    FountainRepository(Application application) {
        AppDatabase db = AppDatabase.getDatabase(application);
        fountainDao = db.fountainDao();
        allFountains = fountainDao.getAll();
    }

    LiveData<List<Fountain>> getAll() {
        return allFountains;
    }
}
