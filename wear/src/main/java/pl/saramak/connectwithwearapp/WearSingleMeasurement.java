package pl.saramak.connectwithwearapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.widget.TextView;

/**
 * Created by Jana on 4/24/2017.
 */

public class WearSingleMeasurement extends WearableActivity {

    TextView measurementTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_single_measurement);
        measurementTextView = (TextView) findViewById(R.id.measurementTextView);
        setAmbientEnabled();

        Intent intent = getIntent();
        String measurementName = intent.getStringExtra("measurementName");
        measurementTextView.setText(measurementName);
    }
}
