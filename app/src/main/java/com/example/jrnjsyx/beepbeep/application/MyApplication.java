package com.example.jrnjsyx.beepbeep.application;

import android.app.Application;

public class MyApplication extends Application {

    private static MyApplication app;

    public static MyApplication getInstance() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

    }

}

