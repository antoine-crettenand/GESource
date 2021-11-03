package com.ancrette.gesource;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.ancrette.gesource.db.FountainDataRepository;
import com.ancrette.gesource.db.Fountain;
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

            fountainDataRepository.insert(this, ft).observe(this, value -> {
                if (value) {
                    Toast.makeText(this, "Successfully added to the local database!", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        });
    }
}