package com.openthos.taskmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.openthos.taskmanager.app.Constants;

import java.util.Map;

public class DormantAppUtils {
    private static DormantAppUtils instance;
    private SharedPreferences mPreferences;

    public static DormantAppUtils getInstance(Context context) {
        if (instance == null) {
            instance = new DormantAppUtils(context);
        }
        return instance;
    }

    private DormantAppUtils(Context context) {
        mPreferences = context.getSharedPreferences(Constants.SP_DORMANT_APP, Context.MODE_PRIVATE);
    }

    /**
     * Add app to an automatic dormancy prevent_list
     *
     * @param packageName
     */
    public void saveAddedApp(String packageName, String appName) {
        mPreferences.edit().putString(packageName, appName).commit();
    }

    /**
     * Get the application stored in the automatic dormancy prevent_list
     *
     * @return
     */
    public Map<String, String> getAllAddedApp() {
        return (Map<String, String>) mPreferences.getAll();
    }

    /**
     * Remove the application added to the auto dormancy prevent_list
     *
     * @param packageName
     */
    public void removeAddApp(String packageName) {
        mPreferences.edit().remove(packageName).commit();
    }
}
