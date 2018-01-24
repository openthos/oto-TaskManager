package com.openthos.greenify;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openthos.greenify.bean.AppInfo;
import com.openthos.greenify.utils.DormantAppUtils;
import com.openthos.greenify.utils.NonDormantAppUtils;

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
        Map<String, String> dormantMaps = DormantAppUtils.getInstance(this).getAllAddedApp();
        Map<String, String> nonDormantMaps = NonDormantAppUtils.getInstance(this).getAllAddedApp();
        AppInfo appInfo;
        for (PackageInfo packageInfo : packageInfos) {
            if (isSystemApps(packageInfo)) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(manager).toString());
            appInfo.setPackageName(packageInfo.packageName);

            appInfo.setIcon(packageInfo.applicationInfo.loadIcon(manager));
            if (dormantMaps != null && dormantMaps.containsKey(appInfo.getPackageName())) {
                appInfo.setDormant(true);
            } else {
                appInfo.setDormant(false);
            }
            if (nonDormantMaps != null && nonDormantMaps.containsKey(appInfo.getPackageName())) {
                appInfo.setNonDormant(true);
            } else {
                appInfo.setNonDormant(false);
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
    public void addDormantList(String packageName, boolean isAdd) {
        AppInfo appInfo = mNotSystemApps.get(packageName);
        if (appInfo != null) {
            if (isAdd) {
                DormantAppUtils.getInstance(this).saveAddedApp(packageName, appInfo.getAppName());
                appInfo.setDormant(true);
            } else {
                DormantAppUtils.getInstance(this).removeAddApp(packageName);
                appInfo.setDormant(false);
            }
        }
    }

    /**
     * Store the package name as packageName or remove the saved SP
     *
     * @param packageName
     * @param isAdd
     */
    public void addNonDormantList(String packageName, boolean isAdd) {
        AppInfo appInfo = mNotSystemApps.get(packageName);
        if (appInfo != null) {
            if (isAdd) {
                NonDormantAppUtils.getInstance(this).saveAddedApp(packageName, appInfo.getAppName());
                appInfo.setNonDormant(true);
            } else {
                NonDormantAppUtils.getInstance(this).removeAddApp(packageName);
                appInfo.setNonDormant(false);
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
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = manager.getRunningAppProcesses();
        for (String packageName : mNotSystemApps.keySet()) {
            mNotSystemApps.get(packageName).setRun(false);
        }
        AppInfo appInfo;
        for (ActivityManager.RunningAppProcessInfo info : runningAppProcesses) {
            for (String pkgName : info.pkgList) {
                if (mNotSystemApps.containsKey(pkgName)) {
                    appInfo = mNotSystemApps.get(pkgName);
                    appInfo.setRun(true);
                    appInfo.addProcessName(info.processName);
                    appInfo.addPid(info.pid);
                    android.util.Log.i("ljh", info.processName + " pkgName " + pkgName);
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
