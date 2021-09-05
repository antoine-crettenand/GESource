package com.example.fountainfinder;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import com.example.fountainfinder.db.FountainDataRepository;
import com.example.fountainfinder.db.local.AppDatabase;
import com.example.fountainfinder.db.Fountain;
import dagger.hilt.android.AndroidEntryPoint;

import javax.inject.Inject;

@AndroidEntryPoint
public class AddFountainActivity extends AppCompatActivity {

    @Inject
    FountainDataRepository fountainDataRepository;
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

            fountainDataRepository.insert(ft).observe(this, value -> {
                if (value) finish();
            });
        });
    }
}