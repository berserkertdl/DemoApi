package com.sys.location.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.widget.Toast;

import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.sys.location.helper.utils.L;
import com.sys.location.helper.utils.NetUtils;
import com.sys.location.helper.utils.T;
import com.sys.location.service.LocationService;

public class LocationReceiver extends BroadcastReceiver {

    private final static String TAG = LocationReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        T.makeText(context, action, Toast.LENGTH_LONG);
        L.i("receiver_action", action);
        LocationManager locationManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        boolean isOpenGPS = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isConnected = NetUtils.isNetworkConnected(context);
        Intent _intent = null;
        T.makeText(context, "GPS : " + isOpenGPS, Toast.LENGTH_SHORT);
        T.makeText(context, "network : " + isConnected, Toast.LENGTH_SHORT);
        if (isOpenGPS && isConnected) {
            _intent = new Intent(context,LocationService.class);
            _intent.setAction(LocationService.START_SERVICE);
            _intent.putExtra("flag", LocationProviderProxy.AMapNetwork);
        } else if (!isOpenGPS && isConnected) {
            _intent = new Intent(context,LocationService.class);
            _intent.setAction(LocationService.START_SERVICE);
            _intent.putExtra("flag", LocationManagerProxy.NETWORK_PROVIDER);
        } else if (!isOpenGPS && !isConnected) {
            _intent = new Intent(context,LocationService.class);
            _intent.setAction(LocationService.STOP_SERVICE);
        } else if (isOpenGPS && !isConnected) {
            _intent = new Intent(context,LocationService.class);
            _intent.setAction(LocationService.START_SERVICE);
            _intent.putExtra("flag", LocationManagerProxy.GPS_PROVIDER);
        }
        _intent.putExtra("isConnected", isConnected);
        context.startService(_intent);
    }

}
