package com.baijiahulian;

import android.app.Application;

/**
 * Created by wangkangfei on 17/5/13.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        BJVideoPlayerSDK.getInstance().init(this);
    }
}
