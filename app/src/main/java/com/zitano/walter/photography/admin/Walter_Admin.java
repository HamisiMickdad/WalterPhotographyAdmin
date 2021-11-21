package com.zitano.walter.photography.admin;

import android.content.Context;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class Walter_Admin extends MultiDexApplication {
    public static final String TAG = Walter_Admin.class
            .getSimpleName();

    //private RequestQueue mRequestQueue;

    private static Walter_Admin mInstance;
    private static Context context;

    @Override
    public void onCreate() {

        FirebaseApp.initializeApp(this);
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        mInstance = this;
        Walter_Admin.context = getApplicationContext();
        FirebaseDatabase.getInstance();

        super.onCreate();
    }
    public  static Context getAppContext() {
        return Walter_Admin.context;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }


}

