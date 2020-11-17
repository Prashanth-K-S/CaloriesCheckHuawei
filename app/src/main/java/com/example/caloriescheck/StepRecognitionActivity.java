package com.example.caloriescheck;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.airbnb.lottie.LottieAnimationView;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.SensorsController;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.DeviceInfo;
import com.huawei.hms.hihealth.data.Field;
import com.huawei.hms.hihealth.data.SamplePoint;
import com.huawei.hms.hihealth.data.Scopes;
import com.huawei.hms.hihealth.options.OnSamplePointListener;
import com.huawei.hms.hihealth.options.SensorOptions;
import com.huawei.hms.location.ActivityIdentification;
import com.huawei.hms.location.ActivityIdentificationService;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.util.ArrayList;
import java.util.List;

public class StepRecognitionActivity extends AppCompatActivity {

    private static final String TAG = "StepRecognitionActivity";
    private static final int REQUEST_SIGN_IN_LOGIN = 101;
    private SensorsController sensorsController;
    private SamplePoint mCurrentSamplePoint, mLastSamplePoint;
    private BroadcastReceiver mRecognitionBroadcastReceiver;
    private LottieAnimationView lottieAnimationView;
    private OnSamplePointListener onStepPointListener = new OnSamplePointListener() {
        @Override
        public void onSamplePoint(SamplePoint samplePoint) {
            // The step count, time, and type data reported by the pedometer is called back to the app through
            // samplePoint.
            showSamplePoint();

            mCurrentSamplePoint = samplePoint;

            if (mLastSamplePoint == null) {
                mLastSamplePoint = samplePoint;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_recognition);

        lottieAnimationView = findViewById(R.id.lav_intro);

        mRecognitionBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("Status")) {
                    String type = intent.getStringExtra("type");

                    changeLottieAnimation(type);
                }
            }
        };

        // Instantiate an object to call activity identification request and remove
        ActivityIdentificationActivity activityIdentificationActivity = new ActivityIdentificationActivity();
        // Define instances that will be send as parameters
        ActivityIdentificationService activityIdentificationService = ActivityIdentification.getService(this);
        PendingIntent pendingIntent = getPendingIntent();
        if (pendingIntent != null) {
            activityIdentificationActivity.removeActivityUpdates(activityIdentificationService, pendingIntent);
        }
        activityIdentificationActivity.requestActivityUpdates(5000, activityIdentificationService, pendingIntent);

        signInn();


    }

    private void changeLottieAnimation(String type) {
        switch (type) {
            case "VEHICLE":
                lottieAnimationView.setAnimation("bike.json");
                break;
            case "BIKE":
                lottieAnimationView.setAnimation("cycling.json");
                break;
            case "WALKING":
                lottieAnimationView.setAnimation("waling.json");
                break;
            case "RUNNING":
                lottieAnimationView.setAnimation("running.json");
                break;
            default:
                lottieAnimationView.setAnimation("still.json");
                break;
        }
    }


    private PendingIntent getPendingIntent() {
        Intent intent = new Intent(this, LocationBroadcastReceiver.class);
        intent.setAction(LocationBroadcastReceiver.ACTION_PROCESS_LOCATION);
        return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void signInn() {
        Log.i(TAG, "begin sign in");
        List<Scope> scopeList = new ArrayList<>();

        // Add scopes to apply for. The following only shows an example.
        // Developers need to add scopes according to their specific needs.

        // View and save steps in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_STEP_BOTH));

        // View and save height and weight in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_CALORIES_BOTH));

        // View and save the heart rate data in HUAWEI Health Kit.
        scopeList.add(new Scope(Scopes.HEALTHKIT_DISTANCE_BOTH));

        // Configure authorization parameters.
        HuaweiIdAuthParamsHelper authParamsHelper =
                new HuaweiIdAuthParamsHelper(HuaweiIdAuthParams.DEFAULT_AUTH_REQUEST_PARAM);
        HuaweiIdAuthParams authParams =
                authParamsHelper.setIdToken().setAccessToken().setScopeList(scopeList).createParams();

        // Initialize the HuaweiIdAuthService object.
        final HuaweiIdAuthService authService =
                HuaweiIdAuthManager.getService(this, authParams);

        // Silent sign-in. If authorization has been granted by the current account,
        // the authorization screen will not display. This is an asynchronous method.
        Task<AuthHuaweiId> authHuaweiIdTask = authService.silentSignIn();

        final Context context = this;

        // Add the callback for the call result.
        authHuaweiIdTask.addOnSuccessListener(new OnSuccessListener<AuthHuaweiId>() {
            @Override
            public void onSuccess(AuthHuaweiId huaweiId) {
                // The silent sign-in is successful.
                Log.i(TAG, "silentSignIn success");

                // readData();
                HiHealthOptions options = HiHealthOptions.builder().build();
                AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(options);
                sensorsController = HuaweiHiHealth.getSensorsController(StepRecognitionActivity.this, signInHuaweiId);

                registerSteps();

                Toast.makeText(context, "silentSignIn success", Toast.LENGTH_LONG).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception exception) {
                // The silent sign-in fails.
                // This indicates that the authorization has not been granted by the current account.
                if (exception instanceof ApiException) {
                    ApiException apiException = (ApiException) exception;
                    Log.i(TAG, "sign failed status:" + apiException.getStatusCode());
                    Log.i(TAG, "begin sign in by intent");

                    // Call the sign-in API using the getSignInIntent() method.
                    Intent signInIntent = authService.getSignInIntent();

                    startActivityForResult(signInIntent, REQUEST_SIGN_IN_LOGIN);
                }
            }
        });
    }

    public void registerSteps() {
        if (sensorsController == null) {
            Toast.makeText(StepRecognitionActivity.this, "SensorsController is null", Toast.LENGTH_LONG).show();
            return;
        }
        DataCollector dataCollector = new DataCollector.Builder()
                .setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL)
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .setPackageName(StepRecognitionActivity.this)
                .setDeviceInfo(new DeviceInfo("hw", "hw", "hw", 0))
                .build();
        SensorOptions.Builder builder = new SensorOptions.Builder();
        builder.setDataType(DataType.DT_CONTINUOUS_STEPS_TOTAL);
        builder.setDataCollector(dataCollector);
        sensorsController.register(builder.build(), onStepPointListener)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(StepRecognitionActivity.this, "Register Success", Toast.LENGTH_LONG).show();

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(Exception e) {
                        Toast.makeText(StepRecognitionActivity.this, "Register Failed" + e.getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    public void unregisterSteps() {
        if (sensorsController == null) {
            Toast.makeText(StepRecognitionActivity.this, "SensorsController is null", Toast.LENGTH_LONG).show();
            return;
        }

        // Unregister the listener for the step count.
        sensorsController.unregister(onStepPointListener).addOnSuccessListener(new OnSuccessListener<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                Toast.makeText(StepRecognitionActivity.this, "UnregisterSteps Succeed ...", Toast.LENGTH_LONG).show();
               /* mLastSamplePoint = null;
                mCurrentSamplePoint = null;
                txtSteps.setText("0");
                txtCalorie.setText("0");
                btnStartStop.setText("Start Tracking");
                isStartStop = true;*/


            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                Toast.makeText(StepRecognitionActivity.this, "UnregisterSteps Failed ...", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterSteps();
    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRecognitionBroadcastReceiver,
                new IntentFilter("pushNotification"));

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRecognitionBroadcastReceiver);
    }

    private void showSamplePoint() {

        if (mLastSamplePoint != null && mCurrentSamplePoint != null) {
            SamplePoint samplePoint = mCurrentSamplePoint;
            System.out.println("STEPS >>>" + (samplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()
                    - mLastSamplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(StepRecognitionActivity.this, "" + (samplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()
                            - mLastSamplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()), Toast.LENGTH_SHORT).show();


                  /*  txtSteps.setText(String.valueOf(Integer.parseInt(txtSteps.getText().toString()) + samplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()
                            - mLastSamplePoint.getFieldValue(Field.FIELD_STEPS).asIntValue()));
                    if(selectedWeight.equalsIgnoreCase("Kg")) {
                        txtCalorie.setText(String.format("%.2f", Integer.parseInt(weight.getText().toString()) * Long.parseLong(txtSteps.getText().toString()) * 0.4 * 0.001 * 1.036) + " Kcal");
                    }else{
                        txtCalorie.setText(String.format("%.2f", Integer.parseInt(weight.getText().toString()) * 2.2046226218 * Long.parseLong(txtSteps.getText().toString()) * 0.4 * 0.001 * 1.036) + " Kcal");
                    }*/
                    mLastSamplePoint = samplePoint;
                }
            });

        }
    }

   /* @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_SIGN_IN_LOGIN) {
            Task<AuthHuaweiId> authHuaweiIdTask = HuaweiIdAuthManager.parseAuthResultFromIntent(data);
            if (authHuaweiIdTask.isSuccessful()) {
                AuthHuaweiId huaweiAccount = authHuaweiIdTask.getResult();
                Log.i("TAG", "accessToken:" + huaweiAccount.getAccessToken());
                *//*AGConnectAuthCredential credential = HwIdAuthProvider.credentialWithToken(huaweiAccount.getAccessToken());
                AGConnectAuth.getInstance().signIn(credential).addOnSuccessListener(new OnSuccessListener<SignInResult>() {
                    @Override
                    public void onSuccess(SignInResult signInResult) {
                        mUser = AGConnectAuth.getInstance().getCurrentUser();
                        Intent intent = new Intent(SplashActivity.this,InstructionActivity.class);
                        startActivity(intent);
                        finish();
                    }
                });*//*
            } else {
                Log.e("TAG", "sign in failed : " + ((ApiException) authHuaweiIdTask.getException()).getStatusCode());
            }
        }
    }*/
}