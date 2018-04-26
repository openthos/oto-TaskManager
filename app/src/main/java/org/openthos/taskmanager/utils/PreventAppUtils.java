package org.openthos.taskmanager.utils;

import android.content.Context;
import android.content.SharedPreferences;

import org.openthos.taskmanager.app.Constants;

import java.util.Map;

public class PreventAppUtils {
    private static PreventAppUtils instance;
    private SharedPreferences mPreferences;

    public static PreventAppUtils getInstance(Context context) {
        if (instance == null) {
            instance = new PreventAppUtils(context);
        }
        return instance;
    }

    private PreventAppUtils(Context context) {
        mPreferences = context.getSharedPreferences(
                Constants.SP_PREVENT_APP, Context.MODE_PRIVATE);
    }

    /**
     * Add app to an automatic prevent list
     *
     * @param packageName
     */
    public void saveAddedApp(String packageName, String appName) {
        mPreferences.edit().putString(packageName, appName).commit();
    }

    /**
     * Get the application stored in the automatic prevent list
     *
     * @return
     */
    public Map<String, String> getAllAddedApp() {
        return (Map<String, String>) mPreferences.getAll();
    }

    /**
     * Remove the application added to the auto prevent list
     *
     * @param packageName
     */
    public void removeAddApp(String packageName) {
        mPreferences.edit().remove(packageName).commit();
    }
}
