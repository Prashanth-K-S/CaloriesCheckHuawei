package com.example.caloriescheck;

import android.app.PendingIntent;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hms.location.ActivityIdentificationService;

public class ActivityIdentificationActivity extends AppCompatActivity {
    public String TAG = "ActivityTransitionUpdate";

    public void requestActivityUpdates(long detectionIntervalMillis, ActivityIdentificationService activityIdentificationService, PendingIntent pendingIntent) {
        try {
            LocationBroadcastReceiver.addIdentificationListener();
            activityIdentificationService.createActivityIdentificationUpdates(detectionIntervalMillis, pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "createActivityIdentificationUpdates onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "createActivityIdentificationUpdates onFailure:" + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "createActivityIdentificationUpdates exception: " + e.getMessage());
        }
    }

    public void removeActivityUpdates(ActivityIdentificationService activityIdentificationService, PendingIntent pendingIntent) {
        //reSet();
        try {
            LocationBroadcastReceiver.removeIdentificationListener();
            Log.i(TAG, "start to removeActivityUpdates");
            activityIdentificationService.deleteActivityIdentificationUpdates(pendingIntent)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "deleteActivityIdentificationUpdates onSuccess: ");
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(Exception e) {
                            Log.d(TAG, "deleteActivityIdentificationUpdates onFailure: " + e.getMessage());
                        }
                    });
        } catch (Exception e) {
            Log.d(TAG, "removeActivityUpdates exception: " + e.getMessage());
        }
    }
}
