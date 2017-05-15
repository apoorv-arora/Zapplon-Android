package com.application.zapplon;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Application;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.location.LocationManager;
import android.os.AsyncTask;

import com.application.zapplon.data.City;
import com.application.zapplon.db.AddressDBWrapper;
import com.application.zapplon.db.LocationDBWrapper;
import com.application.zapplon.services.CacheCleanerService;
import com.application.zapplon.services.ZHackService;
import com.application.zapplon.utils.CommonLib;
import com.application.zapplon.utils.LruCache;
import com.application.zapplon.utils.RequestWrapper;
import com.application.zapplon.utils.UploadManager;
import com.application.zapplon.utils.location.ZLocationListener;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

public class ZApplication extends Application {

    public LruCache<String, Bitmap> cache;
    public ZLocationListener zll = new ZLocationListener(this);
    public LocationManager locationManager = null;
    public String location = "";
    public String country = "";
    public double lat = 0;
    public double lon = 0;
    public boolean isNetworkProviderEnabled = false;
    public boolean isGpsProviderEnabled = false;
    public boolean firstLaunch = false;
    public int state = CommonLib.LOCATION_DETECTION_RUNNING;
    public ArrayList<City> cities = new ArrayList<City>();
    public City currentCity;

    private CheckLocationTimeoutAsync checkLocationTimeoutThread;

    public void onCreate() {
        super.onCreate();

        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        try {
            lat = Double.parseDouble(prefs.getString("lat1", "0"));
            lon = Double.parseDouble(prefs.getString("lon1", "0"));
        } catch (ClassCastException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        location = prefs.getString("location", "");

        // Managers initialize
        RequestWrapper.Initialize(getApplicationContext());
        UploadManager.setContext(getApplicationContext());

        if (prefs.getInt("version", 0) < CommonLib.VERSION) {

            // the logic in this block is used on Home.java, to determine
            // whether to show collection first run or not.
            if (prefs.getInt("version", 0) == 0) {
                prefs.edit().putBoolean("app_fresh_install", true).commit();
                prefs.edit().putBoolean("app_upgrade", false).commit();

            } else if (prefs.getInt("version", 0) > 0) {
                prefs.edit().putBoolean("app_upgrade", true).commit();
                prefs.edit().putBoolean("app_fresh_install", false).commit();
            }

            firstLaunch = true;
            Editor edit = prefs.edit();
            edit.putBoolean("LOCAL_ADDRESS_UPDATE", true);
            edit.putBoolean("firstLaunch", true);
            edit.putInt("version", CommonLib.VERSION);
            edit.commit();

            deleteDatabase("CACHE");
            deleteDatabase(CommonLib.ADDRESS_DB);
            deleteDatabase(CommonLib.LOCATION_DB);

            startCacheCleanerService();
            startHacking();

        } else {
            firstLaunch = prefs.getBoolean("firstLaunch", false);
        }

        try {
            if (!isMyServiceRunning(CacheCleanerService.class)) {
                boolean alarmUp = (PendingIntent.getService(this, 0, new Intent(this, CacheCleanerService.class), PendingIntent.FLAG_NO_CREATE) != null);

                if (!alarmUp)
                    startCacheCleanerService();
            }
            if (!isMyServiceRunning(ZHackService.class)) {
                boolean alarmUp = (PendingIntent.getService(this, 0, new Intent(this, ZHackService.class), PendingIntent.FLAG_NO_CREATE) != null);

                if (!alarmUp)
                    startHacking();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // DB Initialize
        AddressDBWrapper.Initialize(getApplicationContext());
        LocationDBWrapper.Initialize(getApplicationContext());
        cache = new LruCache<String, Bitmap>(30);

        new ThirdPartyInitAsync().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        getTracker(CommonLib.TrackerName.GLOBAL_TRACKER);
    }


    private class ThirdPartyInitAsync extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {

            try {
                // Crashlytics Initialize
                //Fabric.with(getApplicationContext(), new Crashlytics());

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void startCacheCleanerService() {

        Intent intent = new Intent(this, CacheCleanerService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 04);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
    }

    private void startHacking() {

        Intent intent = new Intent(this, ZHackService.class);
        PendingIntent pintent = PendingIntent.getService(this, 0, intent, 0);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 04);
        calendar.set(Calendar.MINUTE, 00);
        calendar.set(Calendar.SECOND, 00);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(AlarmManager.RTC, calendar.getTimeInMillis(), 24 * 60 * 60 * 1000, pintent);
    }

    public void setLocationString(String lstr) {
        location = lstr;
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("location", location);
        editor.commit();
    }

    public void setCountryString(String lstr) {
        country = lstr;
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("country", country);
        editor.commit();
    }

    public void setAddressString(String lstr) {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putString("address", lstr);
        editor.commit();
    }
    public String getAddressString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        String address= prefs.getString("address", "");
        return address;
    }

    public String getLocationString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        location = prefs.getString("location", "");
        return location;
    }

    public String getCountryString() {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        location = prefs.getString("country", "");
        return country;
    }

    public void interruptLocationTimeout() {
        // checkLocationTimeoutThread.interrupt();
        if (checkLocationTimeoutThread != null)
            checkLocationTimeoutThread.interrupt = false;
    }

    public void startLocationCheck() {

        int result = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());

        if (result == ConnectionResult.SUCCESS) {
            zll.getFusedLocation(this);
        } else {
            getAndroidLocation();
        }
    }

    public void getAndroidLocation() {

        CommonLib.ZLog("zll", "getAndroidLocation");

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);

        if (providers != null) {
            for (String providerName : providers) {
                if (providerName.equals(LocationManager.GPS_PROVIDER))
                    isGpsProviderEnabled = true;
                if (providerName.equals(LocationManager.NETWORK_PROVIDER))
                    isNetworkProviderEnabled = true;
            }
        }

        if (isNetworkProviderEnabled || isGpsProviderEnabled) {

            if (isGpsProviderEnabled)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 500.0f, zll);
            if (isNetworkProviderEnabled)
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 500.0f, zll);

            if (checkLocationTimeoutThread != null) {
                checkLocationTimeoutThread.interrupt = false;
            }

            checkLocationTimeoutThread = new CheckLocationTimeoutAsync();
            checkLocationTimeoutThread.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        } else {
            zll.locationNotEnabled();
        }
    }

    private class CheckLocationTimeoutAsync extends AsyncTask<Void, Void, Void> {
        boolean interrupt = true;

        @Override
        protected Void doInBackground(Void... params) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void arg) {
            if (interrupt) {
                zll.interruptProcess();
            }
        }
    }

    public boolean isLocationAvailable() {
        return (isNetworkProviderEnabled || isGpsProviderEnabled);
    }

    @Override
    public void onLowMemory() {
        cache.clear();
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        int userId = prefs.getInt("uid", 0);
        super.onLowMemory();
    }

    public void onTrimLevel(int i) {
        cache.clear();
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        int userId = prefs.getInt("uid", 0);
        super.onTrimMemory(i);
    }

    // GA
    private HashMap<CommonLib.TrackerName, Tracker> mTrackers = new HashMap<CommonLib.TrackerName, Tracker>();

    public synchronized Tracker getTracker(CommonLib.TrackerName trackerId) {

        if (!mTrackers.containsKey(trackerId)) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = trackerId == CommonLib.TrackerName.APPLICATION_TRACKER ? analytics.newTracker("UA-71808244-1") : analytics.newTracker(R.xml.global_tracker);
            // Enable Display Features.
            t.enableAdvertisingIdCollection(true);
            mTrackers.put(trackerId, t);
        }
        return mTrackers.get(trackerId);
    }
    public void logout()
    {
        SharedPreferences prefs = getSharedPreferences("application_settings", 0);
        Editor editor = prefs.edit();
        editor.putInt("uid", 0);
        editor.putString("thumbUrl", "");
        editor.putString("access_token", "");
        editor.remove("username");
        editor.remove("description");
        editor.remove("verified");
        editor.remove("phone");
        editor.remove("profile_pic");
        editor.remove("HSLogin");
        editor.remove("INSTITUTION_NAME");
        editor.remove("STUDENT_ID");
        editor.putBoolean("facebook_post_permission", false);
        editor.putBoolean("post_to_facebook_flag", false);
        editor.putBoolean("facebook_connect_flag", false);
        editor.putBoolean("twitter_status", false);
        editor.remove("ola_access_token");
        editor.remove("uber_access_token");
        editor.remove("ola_cab_session_id");
        editor.remove("uber_cab_session_id");
        editor.remove("appConfig_title");
        editor.remove("appConfig_description");
        editor.remove("appConfig_imageUrl");
        editor.remove("appConfig_footer");
        editor.remove("appConfig_dialog");
        editor.remove("appConfig_hasChanged");
        editor.remove("appConfig_hasChanged_new");
        editor.remove("appConfig_finishonTouchOutside");
        editor.remove("appConfig_showAlways");
        editor.commit();
    }

}
