/*************************************************************************************************
 * Android QB Rating App
 * Jason Barkes - http://jbarkes.blogspot.com
 * November 11, 2013
 *************************************************************************************************/
package barkes.util;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.widget.Toast;

import barkes.football.qbcalc.BuildConfig;

public class AppSettings {

    /** Determine if the app is running in debug mode */
    public static boolean inDebugMode(Context context) {
        boolean inDebug = false;

        PackageManager packageManager = context.getPackageManager();
        String packageName = context.getPackageName();
        ApplicationInfo appInfo = null;

        try {
            appInfo = packageManager.getApplicationInfo(packageName, 0);
        } catch (PackageManager.NameNotFoundException e) {
            if (BuildConfig.DEBUG) {
                Toast.makeText(context, "Could not find package " + packageName, Toast.LENGTH_SHORT).show();
            }
        }
        if(appInfo!=null) {
            if( (appInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0) {
                inDebug = true;
            }
        }

        if(BuildConfig.DEBUG) {
            inDebug = true;
        }

        return inDebug;
    }
}
