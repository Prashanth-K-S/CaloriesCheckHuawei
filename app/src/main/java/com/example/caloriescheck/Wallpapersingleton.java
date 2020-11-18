package com.example.caloriescheck;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.HttpStack;
import com.android.volley.toolbox.Volley;

/**
 * Created on 17/9/17.
 */

public class Wallpapersingleton {

    private static Wallpapersingleton minstance;
    private static Context mctx;
    private RequestQueue requestQueue;
    private static HttpStack mStack;


    private Wallpapersingleton(Context context) {
        mctx = context;
        requestQueue = getRequestQueue();
    }




    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(mctx.getApplicationContext());
        }
        return requestQueue;
    }

    public static synchronized Wallpapersingleton getInstance(Context context) {
        if (minstance == null) {
            minstance = new Wallpapersingleton(context);
        }
        return minstance;
    }



    public <T> void addtorequestqueue(Request<T> request) {
        requestQueue.add(request);
    }

}
