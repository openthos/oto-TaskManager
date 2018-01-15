package om.openthos.greenify.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Map;

public class SleepAppUtils {
    private static final String SP_ADDED_APP = "added_app";
    private static SleepAppUtils instance;
    private SharedPreferences mPreferences;

    public static SleepAppUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SleepAppUtils(context);
        }
        return instance;
    }

    private SleepAppUtils(Context context) {
        mPreferences = context.getSharedPreferences(SP_ADDED_APP, Context.MODE_PRIVATE);
    }

    /**
     * Add app to an automatic dormancy list
     *
     * @param packageName
     */
    public void saveAddedApp(String packageName, String appName) {
        mPreferences.edit().putString(packageName, appName).commit();
    }

    /**
     * Get the application stored in the automatic dormancy list
     *
     * @return
     */
    public Map<String, String> getAllAddedApp() {
        return (Map<String, String>) mPreferences.getAll();
    }

    /**
     * Remove the application added to the auto dormancy list
     *
     * @param packageName
     */
    public void removeAddApp(String packageName) {
        mPreferences.edit().remove(packageName).commit();
    }
}
