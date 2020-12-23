package com.averi.worldscribe;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

public class WorldScribeApplication extends MultiDexApplication {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        WorldScribeApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return WorldScribeApplication.context;
    }
}
