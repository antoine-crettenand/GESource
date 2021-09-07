package com.example.fountainfinder;

import android.app.Dialog;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.OnLifecycleEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import dagger.hilt.android.AndroidEntryPoint;

import java.util.Random;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (isServiceAvailable())
            linkButtonToMapsActivity();
    }

    private void linkButtonToMapsActivity() {
        Button btn = findViewById(R.id.btnToMap);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, LocateFountainActivity.class);
            startActivity(intent);
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void linkButtonToAddFountainActivity(){
        Button btn = findViewById(R.id.btnToAddActivity);
        btn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AddFountainActivity.class);
            startActivity(intent);
        });
    }

    public boolean isServiceAvailable() {
        Log.d(TAG, "isServiceAvailable: checking google services version)");

        int available = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (available == ConnectionResult.SUCCESS) {
            Log.d(TAG, "isServiceAvailable: Google Play Services is working");
            return true;
        } else if (GoogleApiAvailability.getInstance().isUserResolvableError(available)) {
            Log.d(TAG, "isServiceAvailable: an error occured but we can fix it");
            Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(this, available, 9001);
            dialog.show();
        } else {
            Toast.makeText(this, "You can't make map requests at the moment", Toast.LENGTH_SHORT).show();
        }

        return false;
    }
}