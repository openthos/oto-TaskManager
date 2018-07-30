package org.openthos.taskmanager.task;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import org.openthos.taskmanager.MainActivity;
import org.openthos.taskmanager.bean.AppInfo;
import org.openthos.taskmanager.listener.OnTaskCallBack;
import org.openthos.taskmanager.prevent.ui.util.LabelLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RetrieveInfoTask extends AsyncTask<Void, Integer, List<AppInfo>> {
    private LabelLoader labelLoader;
    private MainActivity mActivity;
    private Set<String> mPackageNames;
    private OnTaskCallBack mCallBack;

    public RetrieveInfoTask(Context context, Set<String> packageNames, OnTaskCallBack callBack) {
        mActivity = (MainActivity) context;
        mPackageNames = packageNames;
        mCallBack = callBack;
    }

    @Override
    protected void onPreExecute() {
        labelLoader = new LabelLoader(mActivity);
    }

    @Override
    protected List<AppInfo> doInBackground(Void... params) {
//        Map<String, Set<Long>> running = mActivity.getRunningProcesses();
        List<AppInfo> applications = new ArrayList<>();
        int i = 1;
        AppInfo appInfo;
        for (String packageName : mPackageNames) {
            publishProgress(++i);
            ApplicationInfo info;
            try {
                info = mActivity.getPackageManager().getApplicationInfo(packageName, 0);
            } catch (PackageManager.NameNotFoundException e) { // NOSONAR
                info = null;
            }
            if (info == null || !info.enabled) {
                continue;
            }
            String label = labelLoader.loadLabel(info);
            appInfo = new AppInfo();
            appInfo.setPackageName(packageName);
            appInfo.setAppName(label);
//            appInfo.setRunning(running.get(packageName));
            appInfo.setFlags(info.flags);
            try {
                Drawable icon = mActivity.getPackageManager().getApplicationIcon(packageName);
                appInfo.setIcon(icon);
            } catch (PackageManager.NameNotFoundException e) {
            }

            if (!appInfo.isSystem()) {
                applications.add(appInfo);
            }
        }
        return applications;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

    }

    @Override
    protected void onPostExecute(List<AppInfo> appInfos) {
        mCallBack.callBack(appInfos);
    }
}
