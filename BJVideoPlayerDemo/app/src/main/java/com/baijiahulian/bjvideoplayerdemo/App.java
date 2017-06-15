package com.baijiahulian.bjvideoplayerdemo;

import android.app.Application;

import com.baijiahulian.downloader.DownloadClient;

/**
 * Created by wangkangfei on 17/5/13.
 */

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        /**
         * !!!下载功能正在测试中，请谨慎使用下载模块
         * */
        DownloadClient.init(this);
    }
}
