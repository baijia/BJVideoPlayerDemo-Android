package com.baijiahulian.bjvideoplayerdemo;

import android.app.Application;

import com.baijiahulian.BJVideoPlayerSDK;

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
