package com.baijiahulian.download;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.baijiayun.download.DownloadManager;

import java.util.List;

/**
 * Created by Shubo on 2017/12/8.
 */

public class CustomDownloadService extends Service {

    private static DownloadManager manager;
    private static final String CHANNEL_ID = "bj_download_channel_id";
    private static Context mContext;
    private static boolean hasStartedService;
    /**
     * start 方式开启服务，保存全局的下载管理对象
     */
    public static DownloadManager getDownloadManager(Context context) {
        if (CustomDownloadService.manager == null) {
            CustomDownloadService.manager = DownloadManager.getInstance(context);
        }
        mContext = context.getApplicationContext();
        return manager;
    }

    public static boolean isServiceRunning(Context context) {
        boolean isRunning = false;
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> serviceList = null;
        if (activityManager != null) {
            serviceList = activityManager.getRunningServices(Integer.MAX_VALUE);
        }
        if (serviceList == null || serviceList.size() == 0) return false;
        for (int i = 0; i < serviceList.size(); i++) {
            if (serviceList.get(i).service.getClassName().equals(CustomDownloadService.class.getName())) {
                isRunning = true;
                break;
            }
        }
        return isRunning;
    }

    //真正开启service
    public static void startService(){
        if(mContext != null && !hasStartedService) {
            if (!CustomDownloadService.isServiceRunning(mContext)) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    mContext.startForegroundService(new Intent(mContext, CustomDownloadService.class));
                } else {
                    mContext.startService(new Intent(mContext, CustomDownloadService.class));
                }
            }
            hasStartedService = true;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,"下载", NotificationManager.IMPORTANCE_LOW);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
            //TODO 业务方自行决定notification的样式
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                        .setContentTitle("下载中")
                        .setContentText("")
                        .setSmallIcon(android.R.drawable.sym_def_app_icon)
                        .build();
            startForeground(1001, notification);
            Log.d("bjy", "onCreate startForeground");
        }
    }

    //隐藏通知
    public static void cancelNotification() {
        NotificationManager notificationManager = ((NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE));
        if(notificationManager != null){
            notificationManager.cancel(1001);
        }
    }
}
