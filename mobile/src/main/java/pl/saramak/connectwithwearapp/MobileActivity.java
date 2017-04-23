package pl.saramak.connectwithwearapp;

import android.content.Intent;
import android.speech.RecognizerIntent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MobileActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, MessageApi.MessageListener {


    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    private static final String PHONE_MESSAGE_PATH = "/pmessage";
    private GoogleApiClient mApiClient;
    private Button sendButton;
    private String textToSend = "";
    private EditText inputText;

    static int i = 0;
    private TextView messageFromWatchTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_activity_main);
        inputText = (EditText)findViewById(R.id.send_message_input);
        sendButton = (Button) findViewById(R.id.send_button);
        messageFromWatchTextView = (TextView)findViewById(R.id.text_view);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textToSend = inputText.getText().toString();
                sendAsyncMessage(WEAR_MESSAGE_PATH, textToSend);
            }
        });
        initGoogleApiClient();
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

                showToastMT(message);
            }
        }).start();
    }

    private void sendAsyncMessage(final String path, final String message) {

        Wearable.NodeApi.getConnectedNodes( mApiClient ).setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
            @Override
            public void onResult(@NonNull NodeApi.GetConnectedNodesResult nodes) {
                for(Node node : nodes.getNodes()) {
                    Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, message.getBytes() ).setResultCallback(new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(@NonNull MessageApi.SendMessageResult sendMessageResult) {
                            if (sendMessageResult.getStatus().isSuccess()){
                                inputText.getText().clear();
                                showToastMT(message);
                            }
                        }
                    });
                }
               
            }
        });


    }

    private void showToastMT(final String message) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "message send " + message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if( mApiClient != null )
            mApiClient.unregisterConnectionCallbacks( this );
        mApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        sendAsyncMessage(START_ACTIVITY, "");
        Wearable.MessageApi.addListener( mApiClient, this );
    }

    @Override
    protected void onStop() {
        super.onStop();
        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onMessageReceived(final MessageEvent messageEvent) {
        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if( messageEvent.getPath().equalsIgnoreCase( PHONE_MESSAGE_PATH ) ) {
                    messageFromWatchTextView.setText("Received text: " + new String(messageEvent.getData()));
                }
            }
        });
    }
}
