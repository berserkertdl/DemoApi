package com.sys.location.service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.sys.location.helper.utils.L;
import com.sys.location.helper.utils.T;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LocationService extends Service {

    private static final String TAG = LocationService.class.getSimpleName();

    public static final String START_SERVICE = "start_location_service";

    public static final String STOP_SERVICE = "stop_location_service";

    private LocationManagerProxy mLocationManagerProxy;

    public static final String GPSLOCATION_BROADCAST_ACTION = "com.location.apis.gpslocationdemo.broadcast";


    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        super.onCreate();
        sHA1(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        String flag = intent.getStringExtra("flag");
        Log.i(TAG, "onStartCommand");
        if (START_SERVICE.equals(action)) {
            Log.i("locationService ", "开启定位服务");
            T.makeText(this, "开启定位服务！", Toast.LENGTH_SHORT);
            if (mLocationManagerProxy != null) {
                mLocationManagerProxy.destroy();
                mLocationManagerProxy = null;
            }
            mLocationManagerProxy = LocationManagerProxy.getInstance(this);
            mLocationManagerProxy.requestLocationData(flag, 30 * 1000, 0, new AMapLocationListener() {
                @Override
                public void onLocationChanged(AMapLocation aMapLocation) {
                    if(aMapLocation==null){
                        L.e(TAG,"aMapLocation is null");
                        return;
                    }
                    int errorCode = aMapLocation.getAMapException().getErrorCode();
                    L.i(TAG,"onLocationChanged errorCode : " + errorCode);

                    if(errorCode == 0){
                        TelephonyManager TelephonyMgr = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
                        String szImei = TelephonyMgr.getDeviceId();                 // imei
                        // 定位成功回调信息，设置相关消息
                        String latitude = String.valueOf(aMapLocation.getLatitude());   //纬度
                        String longitude = String.valueOf(aMapLocation.getLongitude()); // 经度
                        String accuracy = String.valueOf(aMapLocation.getAccuracy());   //精确度
                        String provider = aMapLocation.getProvider();                   //定位方式信息
                        SimpleDateFormat df = new SimpleDateFormat(
                                "yyyy-MM-dd HH:mm:ss");
                        Date date = new Date(aMapLocation.getTime());
                        String time = df.format(date);                              //时间

                        JSONArray jsonArray = new JSONArray();
                        JSONObject jsonObject = new JSONObject();
                        try {
                            jsonObject.put("latitude", latitude);
                            jsonObject.put("longitude", longitude);
                            jsonObject.put("accuracy", accuracy);
                            jsonObject.put("provider", provider);
                            jsonObject.put("imei", szImei);
                            jsonObject.put("time", time);
                            jsonArray.put(jsonObject);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String info = jsonArray.toString();
                        send("http://121.43.224.29:8080/PlaceServer/locations.do", info);
                        Log.i("info", info);
                        T.makeText(LocationService.this, info, Toast.LENGTH_SHORT);
                    }
                }

                @Override
                public void onLocationChanged(Location location) {
                    L.i(TAG,"onLocationChanged");
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    L.i(TAG,"onStatusChanged provider:" + provider + " \t status : " + status);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    L.i(TAG,"onProviderEnabled provider:" + provider );
                }

                @Override
                public void onProviderDisabled(String provider) {
                    L.i(TAG,"onProviderDisabled provider:" + provider );
                }
            });

        } else if (STOP_SERVICE.equals(action)) {
            T.makeText(this, "停止定位服务！", Toast.LENGTH_SHORT);
            L.i(TAG, "startId : " + startId);
            Log.i(TAG, "停止定位服务");
            stopSelf();
        }
        return super.onStartCommand(intent, START_REDELIVER_INTENT, startId);
    }

    public static String sHA1(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), PackageManager.GET_SIGNATURES);

            byte[] cert = info.signatures[0].toByteArray();

            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] publicKey = md.digest(cert);
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < publicKey.length; i++) {
                String appendString = Integer.toHexString(0xFF & publicKey[i])
                        .toUpperCase(Locale.US);
                if (appendString.length() == 1)
                    hexString.append("0");
                hexString.append(appendString);
                hexString.append(":");
            }
            return hexString.toString();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy");
        if(mLocationManagerProxy!=null){
            mLocationManagerProxy.destroy();
            mLocationManagerProxy = null;
        }
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            Bundle bundle = null;
            switch (what) {
                case 0:
                    break;
                case 1:
                    bundle = msg.getData();
                    String data = bundle.getString("result");
                    L.i(TAG, "message : " + data);
                    break;
            }
        }
    };


    private void send(final String path, final String args) {
        new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL(path);
                    HttpURLConnection httpConnect = (HttpURLConnection) url.openConnection();
                    httpConnect.setDoInput(true); //设置输入采用字符流
                    httpConnect.setDoOutput(true); //设置输出采用字符流
                    httpConnect.setRequestMethod("POST");
                    httpConnect.setUseCaches(false);//设置缓存
                    httpConnect.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    httpConnect.setRequestProperty("Charset", "UTF-8");

                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(httpConnect.getOutputStream()));
                    writer.write("location_info="+args);
                    writer.flush();
                    writer.close();

                    BufferedReader reader = new BufferedReader(new InputStreamReader(httpConnect.getInputStream()));
                    String readLine = null;
                    StringBuffer result = new StringBuffer();
                    while ((readLine = reader.readLine()) != null) {
                        result.append(readLine);
                    }
                    reader.close();
                    L.i(TAG, "result : " + result.toString());
                    httpConnect.disconnect();
                    Message msg = new Message();
                    msg.what = 1;
                    Bundle bundle = new Bundle();
                    bundle.putString("result", result.toString());
                    msg.setData(bundle);
                    handler.sendMessage(msg);

                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }
}
