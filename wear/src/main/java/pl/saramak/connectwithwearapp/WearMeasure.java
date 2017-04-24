package pl.saramak.connectwithwearapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by Jana on 4/23/2017.
 */

public class WearMeasure extends WearableActivity implements GoogleApiClient.ConnectionCallbacks,  MessageApi.MessageListener {

    private Button btn_measure;
    private GoogleApiClient mApiClient;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final String PATH_ACTION = "/action";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_measurements);

        WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
//                btn_measure = (Button) findViewById(R.id.measureBtn);
//                btn_measure.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View view) {
//                        initGoogleApiClient();
//                    }
//                });
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
            final byte[] voiceNoteBytes =
                    spokenText.getBytes(Charset.forName("utf-8"));
            // Get a list of all of the devices that you're
            // connected to. Usually this will just be your
            // phone. Any other devices will ignore your message.
            Wearable.NodeApi.getConnectedNodes(mApiClient)
                    .setResultCallback(
                            new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                                @Override
                                public void onResult(
                                        NodeApi.GetConnectedNodesResult nodes)
                                {
                                    for (Node node : nodes.getNodes()) {

                                        // Send the phone a message requesting that
                                        // it add the task to the database
                                        Wearable.MessageApi.sendMessage(
                                                mApiClient,
                                                node.getId(),
                                                PATH_ACTION,
                                                voiceNoteBytes
                                        );
                                    }
                                    finish();
                                }
                            }
                    );
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

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if( messageEvent.getPath().equalsIgnoreCase(PATH_ACTION) ) {
                    System.out.println("Poraka od mob");
                }
            }
        });
    }
}
