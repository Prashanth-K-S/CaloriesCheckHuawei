package com.example.caloriescheck;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.huawei.hms.location.ActivityIdentificationData;
import com.huawei.hms.location.ActivityIdentificationResponse;

import java.util.List;

public class LocationBroadcastReceiver extends BroadcastReceiver {

    public static final String ACTION_PROCESS_LOCATION = "com.huawei.hms.location.ACTION_PROCESS_LOCATION";
    private static final String TAG = "LocationReceiver";

    public static boolean isListenActivityIdentification = false;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_PROCESS_LOCATION.equals(action)) {
                ActivityIdentificationResponse activityIdentificationResponse = ActivityIdentificationResponse.getDataFromIntent(intent);
                if (activityIdentificationResponse != null && isListenActivityIdentification == true) {
                    Log.d(TAG, "activityRecognitionResult:: " + activityIdentificationResponse);
                    List<ActivityIdentificationData> list = activityIdentificationResponse.getActivityIdentificationDatas();
                    for (int i = 0; i < list.size(); i++) {
                        int type = list.get(i).getIdentificationActivity();
                        String activity_status;
                        Intent statusIntent = new Intent("Status");
                        switch (type) {
                            case 100:
                                activity_status = "VEHICLE";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity :" + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 101:
                                activity_status = "BIKE";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 102:

                                activity_status = "FOOT";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 103:
                                activity_status = "STILL";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 104:
                                activity_status = "OTHERS";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 105:
                                activity_status = "TILTING";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 107:
                                activity_status = "WALKING";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                                break;
                            case 108:
                                activity_status = "RUNNING";
                                statusIntent.putExtra("type",activity_status);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(statusIntent);
                                Toast.makeText(context, "Identified Activity : " + activity_status, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        }
    }

    public static void addIdentificationListener() {
        isListenActivityIdentification = true;
    }

    public static void removeIdentificationListener() {
        isListenActivityIdentification = false;
    }
}
