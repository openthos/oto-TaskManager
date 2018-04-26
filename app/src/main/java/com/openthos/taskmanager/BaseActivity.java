package com.openthos.taskmanager;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.openthos.taskmanager.bean.AppInfo;
import com.openthos.taskmanager.listener.OnCpuChangeListener;
import com.openthos.taskmanager.piebridge.prevent.ui.util.PreventUtils;
import com.openthos.taskmanager.utils.DormantAppUtils;
import com.openthos.taskmanager.utils.NonDormantAppUtils;
import com.openthos.taskmanager.utils.PreventAppUtils;

public abstract class BaseActivity extends FragmentActivity {

    private static Map<String, AppInfo> mNotSystemApps;
    private static String ONE_OR_MORE_SPACE = "\\s+";

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
        Map<String, String> nonDormantMaps = NonDormantAppUtils.getInstance(this).getAllAddedApp();
        Map<String, String> preventMaps = PreventAppUtils.getInstance(this).getAllAddedApp();
        AppInfo appInfo;
        for (PackageInfo packageInfo : packageInfos) {
            if (isSystemApps(packageInfo)) {
                continue;
            }
            appInfo = new AppInfo();
            appInfo.setAppName(packageInfo.applicationInfo.loadLabel(manager).toString());
            appInfo.setPackageName(packageInfo.packageName);

            appInfo.setIcon(packageInfo.applicationInfo.loadIcon(manager));
            if (nonDormantMaps != null && nonDormantMaps.containsKey(appInfo.getPackageName())) {
                appInfo.setNonDormant(true);
            } else {
                appInfo.setNonDormant(false);
            }

            if (preventMaps != null && preventMaps.containsKey(appInfo.getPackageName())) {
                appInfo.setAutoPrevent(true);
            } else {
                appInfo.setAutoPrevent(false);
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
     * Store the package name as packageName or remove the saved SP
     *
     * @param packageName
     * @param isAdd
     */
    public void addPreventList(String packageName, boolean isAdd) {
        AppInfo appInfo = mNotSystemApps.get(packageName);
        if (appInfo != null) {
            if (isAdd) {
                PreventAppUtils.getInstance(this).saveAddedApp(packageName, appInfo.getAppName());
                appInfo.setAutoPrevent(true);
                PreventUtils.updatePreventPkg(this, new String[]{packageName}, true);
            } else {
                PreventAppUtils.getInstance(this).removeAddApp(packageName);
                appInfo.setAutoPrevent(false);
                PreventUtils.updatePreventPkg(this, new String[]{packageName}, false);
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
     * init cpu info
     *
     * @param cpuChangeListener
     */
    public void initCpuInfo(OnCpuChangeListener cpuChangeListener) {
        Process process;
        BufferedReader reader;
        try {
            process = Runtime.getRuntime().exec("/system/bin/top -n 1");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isIgnore = true;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("User") && line.contains("System")) {
                    isIgnore = true;
                    String replace = line.replace("User", "").replace("System", "")
                            .replace("IOW", "").replace("IRQ", "").replace("%", "").replace(" ", "");
                    String[] split = replace.split(",");
                    double totalCpuUsed = 0.0;
                    for (String s : split) {
                        totalCpuUsed += Integer.parseInt(s);
                    }
                    cpuChangeListener.cpuUse(totalCpuUsed);
                } else if (line.contains("PID")) {
                    isIgnore = false;
                } else if (!isIgnore) {
                    String[] split = line.replace("%", "").split(ONE_OR_MORE_SPACE);
                    double cpuUsed = Double.parseDouble(split[2]);
                    if (split.length == 10 && cpuUsed > 0) {
                        if (cpuUsed > 0) {
                            for (String packageName : mNotSystemApps.keySet()) {
                                AppInfo appInfo = mNotSystemApps.get(packageName);
                                for (int pid : appInfo.getPids()) {
                                    if (split[0].equals(String.valueOf(pid))) {
                                        appInfo.addCpuUsage(cpuUsed);
                                        break;
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            }
            cpuChangeListener.loadComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
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