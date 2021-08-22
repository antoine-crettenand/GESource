package com.example.fountainfinder;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.FontRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.*;
import com.example.fountainfinder.db.AppDatabase;
import com.example.fountainfinder.db.Fountain;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.common.util.concurrent.ListenableFuture;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@AndroidEntryPoint
public class AddFountainActivity extends AppCompatActivity {

    @Inject
    AppDatabase db;
    private static final String TAG = AddFountainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_fountain);
        init();
    }

    private void init() {
        linkBtnToSubmit(findViewById(R.id.btnToSubmitRequest));
    }

    private void linkBtnToSubmit(Button btn) {
        btn.setOnClickListener(v -> {
            EditText nameTV = findViewById(R.id.nameEditText);
            EditText latTV = findViewById(R.id.latitudeEditText);
            EditText longTV = findViewById(R.id.longitudeEditText);

            String name = nameTV.getText().toString();
            float latitude = Float.parseFloat(latTV.getText().toString());
            float longitude = Float.parseFloat(longTV.getText().toString());
            Fountain ft = new Fountain(name, latitude, longitude);

            if (db.insert(ft))
                finish();
        });
    }
}