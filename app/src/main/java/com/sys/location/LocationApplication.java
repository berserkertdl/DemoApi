package com.sys.location;


import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;

import com.sys.location.helper.utils.L;
import com.sys.location.helper.utils.T;

/**
 */
public class LocationApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        boolean isDebug = isApkDebugable(getApplicationContext());
        T.isDebug = isDebug;
        L.i("isDebug", isDebug + "");
    }


    public static boolean isApkDebugable(Context context) {
        try {
            PackageInfo pkginfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 1);
            if (pkginfo != null ) {
                ApplicationInfo info= pkginfo.applicationInfo;
                return (info.flags&ApplicationInfo.FLAG_DEBUGGABLE)!=0;
            }
        } catch (Exception e) {

        }
        return false;
    }

}
