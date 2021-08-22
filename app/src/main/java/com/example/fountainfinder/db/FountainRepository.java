package com.example.fountainfinder.db;

import android.app.Application;
import android.os.AsyncTask;
import androidx.lifecycle.LiveData;

import javax.inject.Inject;
import java.util.List;

public class FountainRepository {

    @Inject
    FountainDao fountainDao;

    LiveData<List<Fountain>> getAll() {
        return fountainDao.getAll();
    }
}
