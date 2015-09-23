package com.sys.location.receiver;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.widget.Toast;

import com.amap.api.location.LocationProviderProxy;
import com.sys.location.helper.utils.L;
import com.sys.location.helper.utils.T;
import com.sys.location.service.LocationService;

public class BootBroadCast extends BroadcastReceiver {

    private final static String TAG = BootBroadCast.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        L.i(TAG, intent.getAction());
        T.makeText(context, "BootBroadCast" + intent.getAction(), Toast.LENGTH_LONG);
        // 启动完成
        Intent alarmIntent = new Intent(context, LocationReceiver.class);

        alarmIntent = new Intent(context,LocationService.class);
        alarmIntent.setAction(LocationService.START_SERVICE);
        alarmIntent.putExtra("flag", LocationProviderProxy.AMapNetwork);
//        alarmIntent.setAction("com.location.service.action");
//        PendingIntent sender = PendingIntent.getBroadcast(context, 0, alarmIntent, 0);
        PendingIntent sender = PendingIntent.getService(context,0,alarmIntent,PendingIntent.FLAG_UPDATE_CURRENT);
        long firstTime = SystemClock.elapsedRealtime();
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // 1分种一个周期，不停的发送广播
        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstTime, 1 * 30 * 1000, sender);


    }
}
