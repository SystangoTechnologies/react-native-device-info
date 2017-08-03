package com.imagepicker;

/**
 * Created by stplmacmini6 on 03/10/16.
 */

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.util.Log;

/**
 * Created by ssaxena on 25/12/15.
 */
public class RNPermissionManager {

    public static boolean isAlertWindowPermissionGranted(Activity activity) {
        boolean flag = true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && (ContextCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {
            flag = false;
        }
        return flag;
    }

    public static boolean isMarshmellowOrHigher() {
        boolean flag = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flag = true;
        }
        return flag;
    }

    public static boolean checkPermission(String permission, Context context) {
        if (ContextCompat.checkSelfPermission(context, permission)
                == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        return false;
    }
}
