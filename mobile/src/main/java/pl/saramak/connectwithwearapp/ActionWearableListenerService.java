package pl.saramak.connectwithwearapp;

import android.content.Intent;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.Charset;

/**
 * Created by Jana on 4/24/2017.
 */

public class ActionWearableListenerService extends WearableListenerService {

    private static final String PATH_ACTION = "/action";
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        if (PATH_ACTION.equals(messageEvent.getPath())) {
            // Create the ContentValues object that stores all
            // of the information for the task
            String title = new String(messageEvent.getData(),
                    Charset.forName("utf-8"));


            if (messageEvent.getPath().equalsIgnoreCase(PATH_ACTION)) {
                Intent intent = new Intent(this, MobileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            } else {
                super.onMessageReceived(messageEvent);
            }
            System.out.println("Tekst: " + title);
        }
    }
}
