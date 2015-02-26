package ams.android.linkit.Tools;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import ams.android.linkit.R;

/**
 * Created by Aidin on 2/5/2015.
 */
public class GlobalApplication extends Application {

    private static String TAG = "linkit";
    private static String PROPERTY_BADGET_COUNT = "badget_count";
    private static String PROPERTY_USER_ID = "user_id";
    private static String PROPERTY_REG_ID = "registration_id";
    private static String PROPERTY_APP_VERSION = "appVersion";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = (trackerId == TrackerName.APP_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : (trackerId == TrackerName.GLOBAL_TRACKER) ? analytics.newTracker(R.xml.global_tracker)
                    : analytics.newTracker(R.xml.global_tracker);
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }

    public int getBadgeCount() {
        final SharedPreferences prefs = getGCMPreferences();
        int badgetCountSaved = prefs.getInt(PROPERTY_BADGET_COUNT, 0);
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return 0;
        }
        return badgetCountSaved;
    }

    public void setBadgetCount(int badgetCount) {
        try {
            final SharedPreferences prefs = getGCMPreferences();
            int appVersion = getAppVersion();
            Log.i(TAG, "Saving badgetCount on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PROPERTY_BADGET_COUNT, badgetCount);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        } catch (Exception e) {
        }
    }

    public String getUserId() {
        final SharedPreferences prefs = getGCMPreferences();
        String userIdSaved = prefs.getString(PROPERTY_USER_ID, "");
        if (userIdSaved.isEmpty()) {
            Log.i(TAG, "User not found.");
            return "";
        }
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return userIdSaved;
    }

    public void setUserId(String userId) {
        try {
            final SharedPreferences prefs = getGCMPreferences();
            int appVersion = getAppVersion();
            Log.i(TAG, "Saving userId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_USER_ID, userId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();

        } catch (Exception e) {
        }
    }

    public String getRegistrationId() {
        final SharedPreferences prefs = getGCMPreferences();
        String registrationIdSaved = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationIdSaved.isEmpty()) {
            Log.i(TAG, "Registration not found.");
            return "";
        }

        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion();
        if (registeredVersion != currentVersion) {
            Log.i(TAG, "App version changed.");
            return "";
        }
        return registrationIdSaved;
    }

    public void setRegistrationId(String registrationId) {
        try {
            final SharedPreferences prefs = getGCMPreferences();
            int appVersion = getAppVersion();
            Log.i(TAG, "Saving regId on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PROPERTY_REG_ID, registrationId);
            editor.putInt(PROPERTY_APP_VERSION, appVersion);
            editor.commit();
        } catch (Exception e) {
        }
    }

    public void clearAllSettings() {
        try {
            final SharedPreferences prefs = getGCMPreferences();
            int appVersion = getAppVersion();
            Log.i(TAG, "Erase UserID settings on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove(PROPERTY_USER_ID);
            editor.commit();
        } catch (Exception e) {
        }
    }

    private SharedPreferences getGCMPreferences() {
        return getSharedPreferences("linkit",
                Context.MODE_PRIVATE);
    }

    private int getAppVersion() {
        try {
            PackageInfo packageInfo = this.getPackageManager()
                    .getPackageInfo(this.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }
}