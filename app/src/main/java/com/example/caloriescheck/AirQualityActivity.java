package com.example.caloriescheck;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

public class AirQualityActivity extends AppCompatActivity {

    TextView mAQI;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_air_quality);
        mAQI = findViewById(R.id.tv_aqi);
        //To get details from ambee server
        getDetailsFromAmbeeServer(12.426183, 76.390479);
    }

    private void getDetailsFromAmbeeServer(double lat, double lng) {
        StringRequest quotesCategoryRequest = new StringRequest(Request.Method.GET, API.AIRQUALITY + "lat=" + lat + "&lng=" + lng, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    JSONObject responseFromServer = new JSONObject(response);
                    String message = responseFromServer.getString("message");
                    if (message.equals("Success")) {
                        JSONArray stations = responseFromServer.getJSONArray("stations");
                        JSONObject particularStation = stations.getJSONObject(0);
                        String mAQIPoint = particularStation.getString("AQI");
                        mAQI.setText("AQI point is " + mAQIPoint);
                        Toast.makeText(AirQualityActivity.this, "AQI point is" + mAQIPoint, Toast.LENGTH_SHORT).show();

                    } else {
                        Toast.makeText(AirQualityActivity.this, message, Toast.LENGTH_SHORT).show();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (error.networkResponse != null) {
                    parseVolleyError(error);
                }
                if (error instanceof ServerError) {
                    Toast.makeText(AirQualityActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", String.valueOf(error));
                    error.printStackTrace();
                } else if (error instanceof AuthFailureError) {
                    Toast.makeText(AirQualityActivity.this, "Authentication Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Authentication Error");
                    error.printStackTrace();
                } else if (error instanceof ParseError) {
                    Toast.makeText(AirQualityActivity.this, "Parse Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Parse Error");
                    error.printStackTrace();
                } else if (error instanceof NoConnectionError) {
                    Toast.makeText(AirQualityActivity.this, "Server is under maintenance.Please try later.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "No Connection Error");
                    error.printStackTrace();
                } else if (error instanceof NetworkError) {
                    Toast.makeText(AirQualityActivity.this, "Please check your connection.", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Network Error");
                    error.printStackTrace();
                } else if (error instanceof TimeoutError) {
                    Toast.makeText(AirQualityActivity.this, "Timeout Error", Toast.LENGTH_LONG).show();
                    Log.d("Error", "Timeout Error");
                    error.printStackTrace();
                } else {
                    Toast.makeText(AirQualityActivity.this, "Something went wrong", Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }

            }
        }) {

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<>();
                params.put("x-api-key", API.APIKEY);
                params.put("Content-Type", "application/json");
                return params;
            }
        };
        quotesCategoryRequest.setRetryPolicy(new DefaultRetryPolicy(30000, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        Wallpapersingleton.getInstance(getApplicationContext()).addtorequestqueue(quotesCategoryRequest);
    }

    //To Handle volley response error
    public void parseVolleyError(VolleyError error) {
        try {
            String responseBody = new String(error.networkResponse.data, Charset.forName("UTF-8"));
            JSONObject data = new JSONObject(responseBody);
            String message = data.getString("Message");
            Toast.makeText(AirQualityActivity.this, message, Toast.LENGTH_LONG).show();
            android.app.AlertDialog.Builder loginErrorBuilder = new android.app.AlertDialog.Builder(AirQualityActivity.this);
            loginErrorBuilder.setTitle("Error");
            loginErrorBuilder.setMessage(message);
            loginErrorBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            });
            loginErrorBuilder.show();
        } catch (JSONException e) {
            Log.d("error", e.getMessage());
        }
    }
}