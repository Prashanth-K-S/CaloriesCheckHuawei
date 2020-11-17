package com.example.caloriescheck;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.common.ApiException;
import com.huawei.hms.hihealth.DataController;
import com.huawei.hms.hihealth.HiHealthOptions;
import com.huawei.hms.hihealth.HuaweiHiHealth;
import com.huawei.hms.hihealth.data.DataCollector;
import com.huawei.hms.hihealth.data.DataType;
import com.huawei.hms.hihealth.data.SampleSet;
import com.huawei.hms.hihealth.data.Scopes;
import com.huawei.hms.hihealth.options.ReadOptions;
import com.huawei.hms.hihealth.result.ReadReply;
import com.huawei.hms.support.api.entity.auth.Scope;
import com.huawei.hms.support.hwid.HuaweiIdAuthManager;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParams;
import com.huawei.hms.support.hwid.request.HuaweiIdAuthParamsHelper;
import com.huawei.hms.support.hwid.result.AuthHuaweiId;
import com.huawei.hms.support.hwid.service.HuaweiIdAuthService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class StepsActivity extends AppCompatActivity {

    private static final String TAG = "StepsActivity";
    private static final int REQUEST_SIGN_IN_LOGIN = 101 ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        signInn();
    }

    private void signInn() {
        Log.i(TAG, "begin sign in");
        List<Scope> scopeList = new ArrayList<>();

        // Add scopes to apply for. The following only shows an example.
        // Developers need to add scopes according to their specific needs.

        // View and save steps in HUAWEI Health Kit.
        scopeList.add(new Scope( Scopes.HEALTHKIT_STEP_BOTH));

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

                try {
                    readData();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
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


    public void readData() throws ParseException {

        HiHealthOptions hiHealthOptions = HiHealthOptions.builder()

                .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_READ)

                .addDataType(DataType.DT_CONTINUOUS_STEPS_DELTA, HiHealthOptions.ACCESS_WRITE)

                .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_READ)

                .addDataType(DataType.DT_INSTANTANEOUS_HEIGHT, HiHealthOptions.ACCESS_WRITE)

                .build();

        AuthHuaweiId signInHuaweiId = HuaweiIdAuthManager.getExtendedAuthResult(hiHealthOptions);

        DataController dataController = HuaweiHiHealth.getDataController(this, signInHuaweiId);

        DataCollector dataCollector = new DataCollector.Builder().setPackageName(this)
                .setDataType(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .setDataStreamName("STEPS_DELTA")
                .setDataGenerateType(DataCollector.DATA_TYPE_RAW)
                .build();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date startDate = dateFormat.parse("2020-11-10 09:00:00");
        Date endDate = dateFormat.parse("2020-11-16 09:05:00");
        ReadOptions readOptions = new ReadOptions.Builder()
                .read(DataType.DT_CONTINUOUS_STEPS_DELTA)
                .read(DataType.DT_CONTINUOUS_CALORIES_BURNT)
                .read(DataType.DT_CONTINUOUS_DISTANCE_DELTA)
                .setTimeRange(startDate.getTime(), endDate.getTime(), TimeUnit.MILLISECONDS)
                .build();
        Task<ReadReply> readReplyTask = dataController.read(readOptions);
        readReplyTask.addOnSuccessListener(new OnSuccessListener<ReadReply>() {
            @Override
            public void onSuccess(ReadReply readReply) {
                for (SampleSet sampleSet : readReply.getSampleSets()) {
                    //showSampleSet(sampleSet);
                    Log.i(TAG,"*****************"+sampleSet);
                    Toast.makeText(StepsActivity.this, ""+sampleSet, Toast.LENGTH_SHORT).show();
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                //checkData.setText(e.toString()+"read");
                Log.d(TAG, "onFailure: "+e.getMessage());
                Toast.makeText(StepsActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


}