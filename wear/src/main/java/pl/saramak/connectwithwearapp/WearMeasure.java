package pl.saramak.connectwithwearapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * Created by Jana on 4/23/2017.
 */

public class WearMeasure extends WearableActivity implements GoogleApiClient.ConnectionCallbacks {

    private Button btn_measure;
    private GoogleApiClient mApiClient;
    private static final int SPEECH_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_measure);
        setAmbientEnabled();

        btn_measure = (Button) findViewById(R.id.measureBtn);
        btn_measure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                initGoogleApiClient();
            }
        });

    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks(this)
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    // Create an intent that can start the Speech Recognizer activity
    private void displaySpeechRecognizer() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
// Start the activity, the intent will be populated with the speech text
        startActivityForResult(intent, SPEECH_REQUEST_CODE);
    }

    // This callback is invoked when the Speech Recognizer returns.
// This is where you process the intent and extract the speech text from the intent.
    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        if (requestCode == SPEECH_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> results = data.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS);
            String spokenText = results.get(0);
            System.out.println("Tekst: " + spokenText);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displaySpeechRecognizer();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
