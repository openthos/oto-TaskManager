package org.openthos.taskmanager.task;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import org.openthos.taskmanager.R;
import org.openthos.taskmanager.bean.AppInfo;
import org.openthos.taskmanager.listener.OnTaskCallBack;
import org.openthos.taskmanager.piebridge.prevent.ui.PreventActivity;
import org.openthos.taskmanager.piebridge.prevent.ui.util.LabelLoader;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RetrieveInfoTask extends AsyncTask<Void, Integer, List<AppInfo>> {
    private ProgressDialog dialog;
    private LabelLoader labelLoader;
    private PreventActivity mActivity;
    private Set<String> mPackageNames;
    private OnTaskCallBack mCallBack;

    public RetrieveInfoTask(Context context, Set<String> packageNames, OnTaskCallBack callBack) {
        mActivity = (PreventActivity) context;
        mPackageNames = packageNames;
        mCallBack = callBack;
    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(mActivity);
        dialog.setTitle(R.string.app_name);
        dialog.setIcon(R.mipmap.icon);
        dialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        dialog.setCancelable(false);
        dialog.setMax(mPackageNames.size());
        dialog.show();
        labelLoader = new LabelLoader(mActivity);
    }

    @Override
    protected List<AppInfo> doInBackground(Void... params) {
        Map<String, Set<Long>> running = mActivity.getRunningProcesses();
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
            appInfo.setRunning(running.get(packageName));
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
        if (dialog != null) {
            dialog.setProgress(progress[0]);
        }
    }

    @Override
    protected void onPostExecute(List<AppInfo> appInfos) {
        mCallBack.callBack(appInfos);
        if (dialog != null) {
            dialog.dismiss();
            dialog = null;
        }
    }

    public ProgressDialog getDialog() {
        return dialog;
    }
}
