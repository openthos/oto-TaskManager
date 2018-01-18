package com.openthos.greenify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openthos.greenify.bean.AppInfo;
import com.openthos.greenify.utils.SleepAppUtils;


public abstract class BaseActivity extends FragmentActivity {
    public static int DELAY_TIME_REFRESH = 500;

    private static Map<String, AppInfo> mNotSystemApps;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData();
        initListener();
    }

    /**
     * A set of non - system applications required to initialize an entire application
     */
    public void initAppInfos() {
        if (mNotSystemApps == null) {
            mNotSystemApps = new HashMap<>();
        }
        PackageManager manager = getPackageManager();
        List<PackageInfo> packageInfos = manager.getInstalledPackages(0);
        Map<String, String> allAddedApp = SleepAppUtils.getInstance(this).getAllAddedApp();
        AppInfo appInfo = null;
        for (PackageInfo packageInfo : packageInfos) {
            if (isSystemApps(packageInfo)) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(manager).toString());
            appInfo.setPackageName(packageInfo.packageName);
            appInfo.setIcon(packageInfo.applicationInfo.loadIcon(manager));
            if (allAddedApp != null && allAddedApp.containsKey(appInfo.getPackageName())) {
                appInfo.setAdd(true);
            } else {
                appInfo.setAdd(false);
            }
            mNotSystemApps.put(appInfo.getPackageName(), appInfo);
        }
    }

    /**
     * Whether it is a system application
     *
     * @param packageInfo
     * @return
     */
    private boolean isSystemApps(PackageInfo packageInfo) {
        return !((packageInfo.applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0);
    }

    /**
     * Obtaining the application of the specified key from the data list
     *
     * @param packageName
     * @return
     */
    public AppInfo getAppInfoByPkgName(String packageName) {
        return mNotSystemApps.get(packageName);
    }

    /**
     * Store the package name as packageName or remove the saved SP
     *
     * @param packageName
     * @param isAdd
     */
    public void addSleepList(String packageName, boolean isAdd) {
        AppInfo appInfo = mNotSystemApps.get(packageName);
        if (appInfo != null) {
            if (isAdd) {
                SleepAppUtils.getInstance(this).saveAddedApp(packageName, appInfo.getAppName());
                appInfo.setAdd(true);
            } else {
                SleepAppUtils.getInstance(this).removeAddApp(packageName);
                appInfo.setAdd(false);
            }
        }
    }

    /**
     * Obtaining a list of application data for the entire application
     *
     * @return
     */
    public Map<String, AppInfo> getAppInfosMap() {
        initRunningAPP();
        return mNotSystemApps;
    }

    /**
     * Initializing non system application data
     */
    public void initRunningAPP() {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningServiceInfo> runningServices = manager.getRunningServices(100);
        for (String packageName : mNotSystemApps.keySet()) {
            AppInfo appInfo = mNotSystemApps.get(packageName);
            appInfo.setRun(false);
            for (ActivityManager.RunningServiceInfo info : runningServices) {
                if (packageName.equals(info.service.getPackageName())) {
                    appInfo.setRun(true);
                    appInfo.setPid(info.pid);
                }
            }
        }
    }

    /**
     * Kill the application process
     *
     * @param pkgName
     */
    public void forceStopAPK(String pkgName) {
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(sh.getOutputStream());
            final String Command = "am force-stop " + pkgName + "\n";
            os.writeBytes(Command);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void refresh() {
    }

    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();

    public abstract void initListener();

}
