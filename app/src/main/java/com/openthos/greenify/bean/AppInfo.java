package com.openthos.greenify.bean;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.openthos.greenify.app.Constants;

import java.util.ArrayList;
import java.util.List;

public class AppInfo {
    private ActivityManager mManager;
    private String appName;
    private String packageName;
    private List<String> processNames;
    private Drawable icon;
    private List<Integer> pids;
    private double cpuUsage;
    private long memoryUsage;
    private String batteryUsage;
    private boolean isRun;
    private boolean isDormant;
    private boolean isNonDormant;

    public AppInfo() {
        pids = new ArrayList<>();
        processNames = new ArrayList<>();
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public List<String> getProcessNames() {
        return processNames;
    }

    public void addProcessName(String processName) {
        if (!processNames.contains(processName)) {
            processNames.add(processName);
        }
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public List<Integer> getPids() {
        return pids;
    }

    public void addPid(int pid) {
        if (!pids.contains(pid)) {
            pids.add(pid);
        }
    }

    public String getCpuUsage() {
        return cpuUsage + "%";
    }

    public void addCpuUsage(double cpuUsage) {
        this.cpuUsage += cpuUsage;
    }

    public long getMemoryUsage(Context context) {
        memoryUsage = 0;
        if (pids.size() != 0) {
            if (mManager == null) {
                mManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            }
            for (Integer pid : pids) {
                memoryUsage +=
                        mManager.getProcessMemoryInfo(new int[]{pid})[0].getTotalPrivateDirty();
            }
        }
        return memoryUsage * Constants.KB;
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage += memoryUsage;
    }

    public String getBatteryUsage() {
        if (batteryUsage == null) {
            return "";
        }
        return batteryUsage;
    }

    public void setBatteryUsage(String batteryUsage) {
        this.batteryUsage = batteryUsage;
    }

    public boolean isRun() {
        return isRun;
    }

    public void setRun(boolean run) {
        isRun = run;
        if (!run) {
            setStop();
        }
    }

    public boolean isDormant() {
        return isDormant;
    }

    public void setDormant(boolean dormant) {
        isDormant = dormant;
    }

    public boolean isNonDormant() {
        return isNonDormant;
    }

    public void setNonDormant(boolean nonDormant) {
        isNonDormant = nonDormant;
    }

    public int getDormantState() {
        if (isDormant()) {
            return isRun() ? Constants.APP_WAIT_DORMANT : Constants.APP_HAVE_DORMANT;
        }
        if (isNonDormant()) {
            return Constants.APP_NON_DORMANT;
        }
        return Constants.App_NON_DEAL;
    }

    public void setStop() {
        processNames.clear();
        pids.clear();
        cpuUsage = 0;
    }

    public Intent getIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
