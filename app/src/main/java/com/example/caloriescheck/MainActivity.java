package com.example.caloriescheck;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageCaptureException;
import androidx.camera.core.Preview;
import androidx.camera.extensions.HdrImageCaptureExtender;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.LifecycleOwner;

import com.airbnb.lottie.LottieAnimationView;
import com.google.common.util.concurrent.ListenableFuture;
import com.huawei.hmf.tasks.OnFailureListener;
import com.huawei.hmf.tasks.OnSuccessListener;
import com.huawei.hmf.tasks.Task;
import com.huawei.hms.mlsdk.MLAnalyzerFactory;
import com.huawei.hms.mlsdk.classification.MLImageClassification;
import com.huawei.hms.mlsdk.classification.MLImageClassificationAnalyzer;
import com.huawei.hms.mlsdk.classification.MLRemoteClassificationAnalyzerSetting;
import com.huawei.hms.mlsdk.common.MLApplication;
import com.huawei.hms.mlsdk.common.MLException;
import com.huawei.hms.mlsdk.common.MLFrame;
import com.huawei.hms.mlsdk.objects.MLObject;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzer;
import com.huawei.hms.mlsdk.objects.MLObjectAnalyzerSetting;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {


    private final String[] REQUIRED_PERMISSIONS = new String[]{"android.permission.CAMERA", "android.permission.WRITE_EXTERNAL_STORAGE"};
    PreviewView mPreviewView;
    LottieAnimationView captureImage;
    ImageCapture imageCapture;
    MLObjectAnalyzer objectAnalyzer;
    MLImageClassificationAnalyzer cloudImageClassificationAnalyzer;
    private Executor executor = Executors.newSingleThreadExecutor();
    private final int REQUEST_CODE_PERMISSIONS = 1001;
    private static final String TAG = "MainActivity";
    private Dialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mPreviewView = findViewById(R.id.camera);
        captureImage = findViewById(R.id.btncapture);

        alertDialog = new Dialog(this);

        MLApplication.getInstance().setApiKey("CgB6e3x9p6mgCjKI3w8iJXygdLCIvAeavxWDFaSGLygWgWBeLSh01gVdkz2lk1BIPu0eOq4VyK0lu4UJCtp+sXq+");
        if (allPermissionsGranted()) {
            startCamera(); //start camera if permission has been granted by user
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }


    private void startCamera() {

        final ListenableFuture<ProcessCameraProvider> cameraProviderFuture = ProcessCameraProvider.getInstance(this);

        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {

                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    // No errors need to be handled for this Future.
                    // This should never be reached.
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }

    //To show no internet dialog
    private void showLoadingDialog() {
        //alertDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        alertDialog.setContentView(R.layout.loadingdialog);
        if (alertDialog.getWindow() != null)
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void dismissLoadingDialog() {
        if (alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {

        Preview preview = new Preview.Builder()
                .build();
        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                .build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .build();
        ImageCapture.Builder builder = new ImageCapture.Builder();
        HdrImageCaptureExtender hdrImageCaptureExtender = HdrImageCaptureExtender.create(builder);
        if (hdrImageCaptureExtender.isExtensionAvailable(cameraSelector)) {
            hdrImageCaptureExtender.enableExtension(cameraSelector);
        }

        imageCapture = builder
                .setTargetRotation(this.getWindowManager().getDefaultDisplay().getRotation())
                .build();
        preview.setSurfaceProvider(mPreviewView.getSurfaceProvider());
        Camera camera = cameraProvider.bindToLifecycle((LifecycleOwner) this, cameraSelector, preview, imageAnalysis, imageCapture);

        captureImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoadingDialog();
                SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
                File file = new File(getBatchDirectoryName(), mDateFormat.format(new Date()) + ".jpg");

                ImageCapture.OutputFileOptions outputFileOptions = new ImageCapture.OutputFileOptions.Builder(file).build();

                imageCapture.takePicture(outputFileOptions, executor, new ImageCapture.OnImageSavedCallback() {
                    @Override
                    public void onImageSaved(@NonNull ImageCapture.OutputFileResults outputFileResults) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                //   Toast.makeText(CameraActivity.this, file.getPath(), Toast.LENGTH_SHORT).show();
                                Bitmap myBitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                                MLObjectAnalyzerSetting setting = new MLObjectAnalyzerSetting.Factory()
                                        .setAnalyzerType(MLObjectAnalyzerSetting.TYPE_PICTURE)
                                        .allowMultiResults()
                                        .allowClassification()
                                        .create();
                                objectAnalyzer = MLAnalyzerFactory.getInstance().getLocalObjectAnalyzer(setting);
                                MLFrame frame = MLFrame.fromBitmap(myBitmap);
                                Task<List<MLObject>> task = objectAnalyzer.asyncAnalyseFrame(frame);
                                // Asynchronously process the result returned by the object detector.
                                task.addOnSuccessListener(new OnSuccessListener<List<MLObject>>() {
                                    @Override
                                    public void onSuccess(List<MLObject> objects) {
                                        SparseArray<MLObject> objectSparseArray = objectAnalyzer.analyseFrame(frame);
                                        for (int i = 0; i < objectSparseArray.size(); i++) {
                                            if (objectSparseArray.valueAt(i).getTypeIdentity() == MLObject.TYPE_FOOD) {
                                                // Toast.makeText(CameraActivity.this, "It is FOOD", Toast.LENGTH_SHORT).show();

                                                // IMAGE Classification ...

                                                MLRemoteClassificationAnalyzerSetting cloudSetting =
                                                        new MLRemoteClassificationAnalyzerSetting.Factory()
                                                                .setMinAcceptablePossibility(0.8f)
                                                                .create();
                                                cloudImageClassificationAnalyzer = MLAnalyzerFactory.getInstance().getRemoteImageClassificationAnalyzer(cloudSetting);
                                                MLFrame frame = MLFrame.fromBitmap(myBitmap);
                                                Task<List<MLImageClassification>> task = cloudImageClassificationAnalyzer.asyncAnalyseFrame(frame);
                                                task.addOnSuccessListener(new OnSuccessListener<List<MLImageClassification>>() {
                                                    @Override
                                                    public void onSuccess(List<MLImageClassification> classifications) {

                                                        dismissLoadingDialog();

                                                        //   String result = "";
                                                        ArrayList<String> result = new ArrayList<>();
                                                        for (MLImageClassification classification : classifications) {
                                                            result.add(classification.getName());
                                                        }
                                                        Log.d(TAG, "onSuccess: " + result);
                                                        Toast.makeText(MainActivity.this, "" + result.get(0) + "," + result.get(result.size() - 1), Toast.LENGTH_LONG).show();
                                                       /* Intent intent = new Intent(CameraActivity.this, CalorieTrackerActivity.class);
                                                        intent.putExtra("result", result);
                                                        startActivity(intent);
                                                        finish();*/
                                                    }
                                                }).addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(Exception e) {
                                                        try {
                                                            MLException mlException = (MLException) e;
                                                            int errorCode = mlException.getErrCode();
                                                            String errorMessage = mlException.getMessage();
                                                            Toast.makeText(MainActivity.this, "" + errorMessage, Toast.LENGTH_SHORT).show();
                                                        } catch (Exception error) {
                                                            // Handle the conversion error.
                                                            Toast.makeText(MainActivity.this, "" + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });

                                            } else {
                                                dismissLoadingDialog();
                                                Toast.makeText(MainActivity.this, "NOT FOOD", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(Exception e) {
                                        dismissLoadingDialog();
                                        // Detection failure.
                                        Toast.makeText(MainActivity.this, "Detection Failed", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        });
                    }

                    @Override
                    public void onError(@NonNull ImageCaptureException exception) {
                        exception.printStackTrace();
                        dismissLoadingDialog();
                        Toast.makeText(MainActivity.this, "" + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }


                });

            }
        });
    }

    public String getBatchDirectoryName() {

        String app_folder_path = "";
        app_folder_path = Environment.getExternalStorageDirectory().toString() + "/Pandavas";
        File dir = new File(app_folder_path);
        if (!dir.exists() && !dir.mkdirs()) {

        }

        return app_folder_path;
    }

    private boolean allPermissionsGranted() {

        for (String permission : REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {
                startCamera();
            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT).show();
                this.finish();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (objectAnalyzer != null) {
            try {
                objectAnalyzer.stop();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (cloudImageClassificationAnalyzer != null) {
            try {
                cloudImageClassificationAnalyzer.stop();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

}