package com.sys.location.service;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.amap.api.location.LocationProviderProxy;

import java.util.ArrayList;

import javax.xml.transform.Result;

public class PushService extends Service {
    public PushService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        checkService();
        return super.onStartCommand(intent, flags, START_STICKY);
    }

    public void check(){
        Intent _intent = new Intent(PushService.this, LocationService.class);
        _intent.setAction(LocationService.START_SERVICE);
        _intent.putExtra("flag", LocationProviderProxy.AMapNetwork);
        PendingIntent pendingIntent = PendingIntent.getService(this,0,_intent,PendingIntent.FLAG_CANCEL_CURRENT);


    }

    public void checkService(){
        new AsyncTask<Void,Void,Boolean>(){

            @Override
            protected Boolean doInBackground(Void... params) {
                boolean  isRunning = isServiceRunning(PushService.this, "com.sys.location.service.LocationService");
                if(isRunning){
                    try {
                        Thread.sleep(30*1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                return isRunning;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                if (aBoolean) {
                    Intent _intent = new Intent(PushService.this, LocationService.class);
                    _intent.setAction(LocationService.START_SERVICE);
                    _intent.putExtra("flag", LocationProviderProxy.AMapNetwork);
                    startService(_intent);
                } else {
                    checkService();
                }

            }
        };
    }

    // 检测服务是否正在运行
    private boolean isServiceRunning(Context context, String service_Name) {
        ActivityManager manager =
                (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (service_Name.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
