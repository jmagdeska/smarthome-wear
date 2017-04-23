package pl.saramak.connectwithwearapp;

import android.content.ContentValues;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import java.nio.charset.Charset;

/**
 * Created by Jana on 4/23/2017.
 */

public class AddTaskWearableListenerService extends WearableListenerService {

    private static final String PATH_ADD_TASK = "/addTask";
    @Override
    public void onMessageReceived(
            MessageEvent messageEvent)
    {
        if( PATH_ADD_TASK.equals(messageEvent.getPath())) {
            // Create the ContentValues object that stores all
            // of the information for the task
            String title = new String(messageEvent.getData(),
                    Charset.forName("utf-8"));
//            ContentValues values = new ContentValues();
//            values.put(COLUMN_TITLE, title);
//            values.put(COLUMN_NOTES, "");
//            values.put(COLUMN_DATE_TIME, System.currentTimeMillis());
//            // Insert the task into the database
//            getContentResolver().insert(CONTENT_URI, values);

            System.out.println("Tekst: " + title);
        }
    }

}
