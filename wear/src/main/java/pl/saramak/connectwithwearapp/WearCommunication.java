package pl.saramak.connectwithwearapp;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.CircledImageView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

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
 * Created by Jana on 4/26/2017.
 */

public class WearCommunication extends WearableActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {

    private GoogleApiClient mApiClient;
    private static final int SPEECH_REQUEST_CODE = 0;
    private static final String PATH_ACTION = "/action";
    private static final String WEAR_MESSAGE_PATH = "/wearMessage";
    private static final String START_PHONE_ACTIVITY = "/start_phone_activity";

    private Button measureBtn;
    private EditText inputText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wear_activity_communication);

        initGoogleApiClient();

        setContentView(R.layout.wear_activity_communication);
        measureBtn = (Button) findViewById(R.id.measureBtn);
        measureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                inputText = (EditText) findViewById(R.id.input_text);
                String measurement = inputText.getText().toString();
                if(!measurement.isEmpty()) {
                    sendMessage(START_PHONE_ACTIVITY, measurement);
                    Wearable.MessageApi.addListener(mApiClient, WearCommunication.this);
                }
                else Toast.makeText(WearCommunication.this,"Please enter a measurement!", Toast.LENGTH_SHORT).show();
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

    private void sendMessage(final String path, final String message) {
        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, message.getBytes() ).await();
                }
            }
        }).start();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        System.out.println("onConnected");
//        displaySpeechRecognizer();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if(messageEvent.getPath().equalsIgnoreCase(WEAR_MESSAGE_PATH) ) {
                    Log.d("Izmereno: ", messageEvent.getData().toString());
                }
            }
        });
    }
}
