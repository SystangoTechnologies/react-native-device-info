package com.learnium.RNDeviceInfo;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings.Secure;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.imagepicker.RNPermissionManager;


import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.annotation.Nullable;

public class RNDeviceModule extends ReactContextBaseJavaModule {

  ReactApplicationContext reactContext;
    Activity activity;

  public RNDeviceModule(ReactApplicationContext reactContext, Activity mActivity) {
    super(reactContext);
    this.reactContext = reactContext;
      this.activity = mActivity;
  }

  @Override
  public String getName() {
    return "RNDeviceInfo";
  }

  private String getCurrentLanguage() {
      Locale current = getReactApplicationContext().getResources().getConfiguration().locale;
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
          return current.toLanguageTag();
      } else {
          StringBuilder builder = new StringBuilder();
          builder.append(current.getLanguage());
          if (current.getCountry() != null) {
              builder.append("-");
              builder.append(current.getCountry());
          }
          return builder.toString();
      }
  }

  private String getCurrentCountry() {
    Locale current = getReactApplicationContext().getResources().getConfiguration().locale;
    return current.getCountry();
  }

  @Override
  public @Nullable Map<String, Object> getConstants() {

    HashMap<String, Object> constants = new HashMap<String, Object>();

    PackageManager packageManager = this.reactContext.getPackageManager();
    String packageName = this.reactContext.getPackageName();

    constants.put("appVersion", "not available");
    constants.put("buildVersion", "not available");
    constants.put("buildNumber", 0);

    try {
      PackageInfo info = packageManager.getPackageInfo(packageName, 0);
      constants.put("appVersion", info.versionName);
      constants.put("buildNumber", info.versionCode);
    } catch (PackageManager.NameNotFoundException e) {
      e.printStackTrace();
    }

    String deviceName = "Unknown";

    try {
      BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
      deviceName = myDevice.getName();
    } catch(Exception e) {
      e.printStackTrace();
    }

    constants.put("deviceName", deviceName);
    constants.put("systemName", "Android");
    constants.put("systemVersion", Build.VERSION.RELEASE);
    constants.put("model", Build.MODEL);
    constants.put("deviceId", Build.BOARD);
    constants.put("deviceLocale", this.getCurrentLanguage());
    constants.put("deviceCountry", this.getCurrentCountry());
    constants.put("uniqueId", Secure.getString(this.reactContext.getContentResolver(), Secure.ANDROID_ID));
    constants.put("systemManufacturer", Build.MANUFACTURER);
    constants.put("bundleId", packageName);
    constants.put("userAgent", System.getProperty("http.agent"));
    return constants;
  }

  @ReactMethod
  public void  getCurrentScreenSize(final Callback callback){

    DisplayMetrics metrics = reactContext.getResources().getDisplayMetrics();;
    WindowManager wm = (WindowManager) reactContext.getSystemService(Context.WINDOW_SERVICE);
    Display display = wm.getDefaultDisplay();
    Point size = new Point();
    display.getSize(size);
    int width = (int)(size.x/metrics.density);
    int height = (int)(size.y/metrics.density);
    callback.invoke(null, String.valueOf(width), String.valueOf(height));
  }
   @ReactMethod
    public void takeScreenShot(final Callback callback){
       if (!RNPermissionManager.isAlertWindowPermissionGranted(activity)) {
           ActivityCompat.requestPermissions(activity,
                   new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                   225);
           callback.invoke(null,null);

       }
       else{
           View view = activity.getWindow().getDecorView();
           view.setDrawingCacheEnabled(true);
           view.buildDrawingCache();
           Bitmap b1 = view.getDrawingCache();
           Rect frame = new Rect();
           activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
           int statusBarHeight = frame.top;
           int width = activity.getWindowManager().getDefaultDisplay().getWidth();
           int height = activity.getWindowManager().getDefaultDisplay().getHeight();

           Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height - statusBarHeight);
           view.destroyDrawingCache();

           String imagePath=saveToSDcard(b);
           Log.e("imagePath",imagePath);

           callback.invoke(null,imagePath);
       }
   }

    private String saveToSDcard(Bitmap pictureBitmap) {
        String filepath="";
        try {
            String path = Environment.getExternalStorageDirectory().toString();
            OutputStream fOut = null;
            Integer counter = 0;
            File file = new File(path, "ScreenaShot" + counter + ".jpg"); // the File to save , append increasing numeric counter to prevent files from getting overwritten.
            fOut = new FileOutputStream(file);
            pictureBitmap.compress(Bitmap.CompressFormat.JPEG, 85, fOut); // saving the Bitmap to a file compressed as a JPEG with 85% compression rate
            fOut.flush(); // Not really required
            fOut.close(); // do not forget to close the stream
            MediaStore.Images.Media.insertImage(activity.getContentResolver(), file.getAbsolutePath(), file.getName(), file.getName());
             filepath= file.getPath();

        } catch (Exception e) {
           Log.e("Log",e.getStackTrace().toString());
        }
        return filepath;
    }
}
