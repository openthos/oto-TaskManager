package org.openthos.taskmanager.bean;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;
import org.openthos.taskmanager.R;
import org.openthos.taskmanager.app.Constants;
import org.openthos.taskmanager.prevent.common.PackageUtils;
import org.openthos.taskmanager.prevent.ui.util.StatusUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AppInfo {
    private ActivityManager mManager;
    private String appName;
    private String packageName;
    private List<String> processNames;
    private Drawable icon;
    private List<Integer> pids;
    private double cpuUsage;
    private double memoryUsage;

    private int flags;
    private Set<Long> running;
    private String runDescribe;

    private boolean isRun;
    private boolean isDormant;
    private boolean isNonDormant;
    private boolean isAutoPrevent;

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

    public void setPids(List<Integer> pids) {
        this.pids = pids;
    }

    public String getCpuUsage() {
        return cpuUsage + "%";
    }

    public void addCpuUsage(double cpuUsage) {
        this.cpuUsage += cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = cpuUsage;
    }

    public void clearCpuUsage() {
        cpuUsage = 0;
    }

    public String getMemoryUsage() {
        return memoryUsage + "MB";
    }

    public void setMemoryUsage(double memoryUsage) {
        this.memoryUsage = memoryUsage;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }

    public Set<Long> getRunning() {
        return running;
    }

    public void setRunning(Set<Long> running) {
        this.running = running;
    }

    public String getRunDescribe() {
        return runDescribe;
    }

    public void setRunDescribe(String runDescribe) {
        this.runDescribe = runDescribe;
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

    public boolean isAutoPrevent() {
        return isAutoPrevent;
    }

    public void setAutoPrevent(boolean autoPrevent) {
        isAutoPrevent = autoPrevent;
    }

    public Intent getIntent(Context context) {
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        return intent;
    }

    public boolean isSystem() {
        return PackageUtils.isSystemPackage(this.flags);
    }

    @Override
    public String toString() {
        return (running == null ? "1" : "0") + (isSystem() ? "1" : "0") + "/" + appName + "/" + packageName;
    }

    public String getDescribeState(Context context) {
        return StatusUtils.formatRunning(context, running).toString();
    }

    public int getRunState(Context context) {
        if (runDescribe != null) {
            if (isNonDormant()) {
                return Constants.NO_DORMANT_APP;
            } else if (runDescribe.contains(context.getResources().getString(R.string.importance_foreground))) {
                return Constants.FORWARD_APP;
            } else if (runDescribe.contains(context.getResources().getString(R.string.not_running))) {
                return Constants.NOT_RUN;
            } else {
                return Constants.BACKGROUND_APP;
            }
        }
        return Constants.NOT_RUN;
    }

    public String toJason() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("{").append("packageName:").append(packageName).append(",");
        if (running != null) {
            buffer.append("importances:\"");
            for (Long importance : running) {
                buffer.append(importance).append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append("\",");
        }

        if (pids != null) {
            buffer.append("pids:\"");
            for (int pid : pids) {
                buffer.append(pid).append(",");
            }
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append("\"");
        }
        buffer.append("}");
        return buffer.toString();
    }

    public AppInfo parseJson(String json) {
        try {
            AppInfo appInfo = new AppInfo();
            JSONObject obj = new JSONObject(json);
            appInfo.setPackageName(obj.getString("packageName"));
            appInfo.setRunning(parseSet(obj.optString("importances:")));
            appInfo.setPids(parseList(obj.optString("pids")));
            return appInfo;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Set<Long> parseSet(String strings) {
        try {
            if (!TextUtils.isEmpty(strings)) {
                Set<Long> set = new HashSet<>();
                String[] split = strings.split(",");
                for (String s : split) {
                    set.add(Long.parseLong(s.trim()));
                }
                return set;
            }
        } catch (Exception e) {
        }
        return null;
    }

    private List<Integer> parseList(String strings) {
        try {
            if (!TextUtils.isEmpty(strings)) {
                List<Integer> list = new ArrayList<>();
                String[] split = strings.split(",");
                for (String s : split) {
                    list.add(Integer.parseInt(s.trim()));
                }
                return list;
            }
        } catch (Exception e) {
        }
        return null;
    }
}
