package ams.android.linkit.Tools;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by Aidin on 2/5/2015.
 */
public class GlobalApplication extends Application {

    private static String TAG = "linkit";
    private static String PROPERTY_BADGET_COUNT = "badget_count";
    private static String PROPERTY_USER_ID = "user_id";
    private static String PROPERTY_REG_ID = "registration_id";
    private static String PROPERTY_APP_VERSION = "appVersion";
    private int badgetCount;
    private String userId = "";
    private String registrationId = "";

    public GlobalApplication() {
        super();
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
        this.badgetCount = badgetCount;
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
        if (userId.length() > 0) {
            return userId;
        } else {
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
    }

    public void setUserId(String userId) {
        this.userId = userId;
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
        if (registrationId.length() > 0) {
            return registrationId;
        } else {
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
    }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
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
            Log.i(TAG, "Erase all settings on app version " + appVersion);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
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