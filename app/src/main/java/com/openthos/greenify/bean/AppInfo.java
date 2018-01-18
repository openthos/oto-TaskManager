package com.openthos.greenify.bean;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;

import com.openthos.greenify.app.Constants;

public class AppInfo {
    private String appName;
    private String packageName;
    private Drawable icon;
    private int pid;
    private float cpuUsage;
    private long memoryUsage;
    private String batteryUsage;
    private boolean isRun;
    private boolean isDormant;
    private boolean isNonDormant;

    public AppInfo() {
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

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public String getPid() {
        if (pid == 0){
            return "";
        }
        return String.valueOf(pid);
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getCpuUsage() {
        if (cpuUsage == 0){
            return "";
        }
        return cpuUsage + "%";
    }

    public void setCpuUsage(float cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public String getMemoryUsage() {
        if (memoryUsage == 0){
            return "";
        }
        return String.valueOf(memoryUsage);
    }

    public void setMemoryUsage(long memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public String getBatteryUsage() {
        if (batteryUsage == null){
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

    public Intent getIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }
}
