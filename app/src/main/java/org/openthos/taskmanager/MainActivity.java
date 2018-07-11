package org.openthos.taskmanager;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.wenming.library.processutil.ProcessManager;
import com.wenming.library.processutil.models.AndroidAppProcess;

import org.openthos.taskmanager.adapter.PreventLayoutAdapter;
import org.openthos.taskmanager.app.Constants;
import org.openthos.taskmanager.bean.AppInfo;
import org.openthos.taskmanager.bean.AppLayoutInfo;
import org.openthos.taskmanager.listener.OnCpuChangeListener;
import org.openthos.taskmanager.listener.OnListClickListener;
import org.openthos.taskmanager.listener.OnTaskCallBack;
import org.openthos.taskmanager.prevent.common.PackageUtils;
import org.openthos.taskmanager.prevent.ui.util.PreventUtils;
import org.openthos.taskmanager.task.RetrieveInfoTask;
import org.openthos.taskmanager.utils.DeviceUtils;
import org.openthos.taskmanager.utils.NonDormantAppUtils;
import org.openthos.taskmanager.utils.PreventAppUtils;
import org.openthos.taskmanager.view.HoverTextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements OnListClickListener, View.OnClickListener, View.OnHoverListener {
    private PreventLayoutAdapter mAdapter;
    private Set<String> prevNames = null;
    private int headerIconWidth;
    private static final int HEADER_ICON_WIDTH = 48;
    private static Map<String, Position> positions = new HashMap<String, Position>();
    private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(3);

    private boolean scrolling;
    private static boolean appNotification;
    private ImageView mRefresh;
    private ImageView mClean;
    private TextView mCpuFrequence;
    private TextView mCpuUse;
    private TextView mBattertState;
    private TextView mBatteryCharge;
    private ListView mListView;
    private HoverTextView mHoverText;
    private FrameLayout mMainLayout;

    private Handler mHandler;
    private Handler mCircleHandler;
    private BatteryChangeReceiver mBatteryChangeReceiver;
    private AppInstallReceiver mAppInstallReceiver;
    private double mCpuMaxFreqGHz;
    private RefreshRunnable mRefreshRunnable;

    private List<AppLayoutInfo> mDatas;
    private List<AppInfo> mForwardDatas;
    private List<AppInfo> mNonNeedDormants;
    private List<AppInfo> mBackgroundDatas;
    private Map<String, AppInfo> mAllDatasMap;
    private double mTotalCpuUsed;
    private Map<String, Double> mCpuMap;
    private boolean mIsLoadFinished;


    @Override
    public int getLayoutId() {
        return R.layout.fragment_prevent;
    }

    @Override
    public void initView() {
        mMainLayout = (FrameLayout) findViewById(R.id.main_layout);
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mClean = (ImageView) findViewById(R.id.clean);
        mCpuFrequence = (TextView) findViewById(R.id.cpu_frquence);
        mCpuUse = (TextView) findViewById(R.id.cpu_use);
        mBattertState = (TextView) findViewById(R.id.battery_state);
        mBatteryCharge = (TextView) findViewById(R.id.battery_charge);
        mListView = (ListView) findViewById(R.id.listview);
        mHoverText = (HoverTextView) findViewById(R.id.hove_text);
    }

    @Override
    public void initData() {
        mDatas = new ArrayList<>();
        mForwardDatas = new ArrayList<>();
        mBackgroundDatas = new ArrayList<>();
        mNonNeedDormants = new ArrayList<>();
        mAllDatasMap = new HashMap<>();
        mCpuMap = new HashMap<>();

        mIsLoadFinished = true;

        mHoverText.setParentView(mMainLayout);
        mAdapter = new PreventLayoutAdapter(this);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(
                LayoutInflater.from(this).inflate(R.layout.prevent_list_header, null, false));

        mHandler = new Handler();
        mCircleHandler = new Handler();
        mRefreshRunnable = new RefreshRunnable();
        registBatteryReceiver();
        registAppInstallReceiver();
        mCpuMaxFreqGHz = DeviceUtils.getCurCpuFreq();
        mCpuFrequence.setText(getString(R.string.cpu_frequence, mCpuMaxFreqGHz));
        mCpuUse.setText(getString(R.string.cpu_use, 0.0));
        initAllDatas();
    }

    @Override
    public void initListener() {
        mRefresh.setOnClickListener(this);
        mClean.setOnClickListener(this);
        mAdapter.setOnListClickListener(this);

        mRefresh.setOnHoverListener(this);
        mClean.setOnHoverListener(this);
        mAdapter.setOnHoverListener(this);

        mListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                scrolling = scrollState != SCROLL_STATE_IDLE;
            }

            @Override
            public void onScroll(AbsListView view,
                                 int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
        });
    }

    public HoverTextView getHoverText() {
        return getHoverText();
    }

    private void initAllDatas() {
        new RetrieveInfoTask(this, getPreventPkgNames(), new OnTaskCallBack() {
            @Override
            public void callBack(List<AppInfo> appInfos) {
                mAllDatasMap.clear();
                for (AppInfo appInfo : appInfos) {
                    mAllDatasMap.put(appInfo.getPackageName(), appInfo);
                }
                initAllDataState();
                refreshAllDataState();
                loadData();
                mCircleHandler.removeCallbacksAndMessages(null);
                mCircleHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        notifyDataSetChanged();
                        mCircleHandler.postDelayed(this, Constants.DELAY_TIME_REFRESH);
                    }
                }, Constants.DELAY_TIME_REFRESH);
            }
        }).execute();
    }

    private void initAllDataState() {
        Map<String, Boolean> preventPackages = getPreventPackages();
        Map<String, String> nonDormantMaps = NonDormantAppUtils.getInstance(this).getAllAddedApp();
        for (String packageName : mAllDatasMap.keySet()) {
            AppInfo appInfo = mAllDatasMap.get(packageName);
            if (nonDormantMaps != null && nonDormantMaps.containsKey(appInfo.getPackageName())) {
                appInfo.setNonDormant(true);
            } else {
                appInfo.setNonDormant(false);
            }

            if (preventPackages != null && preventPackages.containsKey(appInfo.getPackageName())) {
                appInfo.setAutoPrevent(preventPackages.get(packageName));
            } else {
                appInfo.setAutoPrevent(false);
            }
        }
    }

    private Map<String, Boolean> getPreventPackages() {
        return PreventAppUtils.getInstance(this).getAllAddedApp();
    }

    private void refreshAllDataState() {
        for (String packageName : mAllDatasMap.keySet()) {
            AppInfo appInfo = mAllDatasMap.get(packageName);
            appInfo.setMemoryUsage(0);
            appInfo.setRunDescribe(getString(R.string.not_running));
        }
        ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<AndroidAppProcess> listInfo = ProcessManager.getRunningAppProcesses();
        for (AndroidAppProcess info : listInfo) {
            AppInfo appInfo = mAllDatasMap.get(info.getPackageName());
            if (appInfo != null) {
                int[] mempid = new int[]{info.pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(mempid);
                if (memoryInfo == null) {
                    return;
                }
                double memSize = memoryInfo[0].getTotalPss() / 1024;
                appInfo.setMemoryUsage(memSize);
                appInfo.setRunDescribe(getString(info.foreground
                        ? R.string.importance_foreground : R.string.importance_background));
            }
        }
    }

    public void loadData() {
        if (!mIsLoadFinished) {
            return;
        }
        mIsLoadFinished = false;
        mDatas.clear();
        mForwardDatas.clear();
        mNonNeedDormants.clear();
        mBackgroundDatas.clear();
        for (String packageName : mAllDatasMap.keySet()) {
            AppInfo appInfo = mAllDatasMap.get(packageName);
            switch (appInfo.getRunState(this)) {
                case Constants.FORWARD_APP:
                    mForwardDatas.add(appInfo);
                    break;
                case Constants.BACKGROUND_APP:
                    mBackgroundDatas.add(appInfo);
                    break;
                case Constants.NO_DORMANT_APP:
                    mNonNeedDormants.add(appInfo);
                    break;
                case Constants.NOT_RUN:
//                    mBackgroundDatas.add(appInfo);
                    break;
            }
        }

        if (mForwardDatas.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.forward_app), mForwardDatas));
        }
        if (mNonNeedDormants.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.non_dormant), mNonNeedDormants));
        }
        if (mBackgroundDatas.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.background_app), mBackgroundDatas));
        }
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                mCpuUse.setText(getString(R.string.cpu_use, mTotalCpuUsed));
                mCpuFrequence.setText(getString(R.string.cpu_frequence, mCpuMaxFreqGHz));
                mAdapter.refreshList(mDatas);
                mIsLoadFinished = true;
            }
        });
    }

    private void registBatteryReceiver() {
        mBatteryChangeReceiver = new BatteryChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryChangeReceiver, filter);
    }

    private void registAppInstallReceiver() {
        mAppInstallReceiver = new AppInstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        registerReceiver(mAppInstallReceiver, filter);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mBatteryChangeReceiver != null) {
            unregisterReceiver(mBatteryChangeReceiver);
            mBatteryChangeReceiver = null;
        }
        if (mAppInstallReceiver != null) {
            unregisterReceiver(mAppInstallReceiver);
            mAppInstallReceiver = null;
        }
        super.onDestroy();
    }

    protected Set<String> getPreventPkgNames() {
        Set<String> names = new HashSet<>();
        PackageManager pm = getPackageManager();
        for (PackageInfo pkgInfo : pm.getInstalledPackages(0)) {
            ApplicationInfo appInfo = pkgInfo.applicationInfo;
            if (PackageUtils.canPrevent(pm, appInfo)) {
                names.add(appInfo.packageName);
            }
        }
        return names;
    }

    public void notifyDataSetChanged() {
        if (mIsLoadFinished) {
            mFixedThreadPool.execute(mRefreshRunnable);
        }
    }

    @Override
    public void onListClickListener(View view, String packageName) {
        AppInfo appInfo = mAllDatasMap.get(packageName);
        switch (view.getId()) {
            case R.id.layout:
//                mListView.showContextMenuForChild(view);
                break;
            case R.id.dormant:
                forceStopAPK(packageName);
                break;
            case R.id.add_or_remove:
                if (appInfo != null) {
                    addNonDormantList(appInfo.getPackageName(), !appInfo.isNonDormant());
                }
                loadData();
                break;
            case R.id.prevent:
                if (appInfo != null) {
                    appInfo.setAutoPrevent(!appInfo.isAutoPrevent());
                    changePrevent(packageName, appInfo.isAutoPrevent());
                }
                loadData();
                break;
        }
    }

    private void changePrevent(String packageName, boolean autoPrevent) {
        PreventUtils.updatePreventPkg(this, new String[]{packageName}, autoPrevent);
        PreventAppUtils.getInstance(this).saveAddedApp(packageName, autoPrevent);
    }

    /**
     * Store the package name as packageName or remove the saved SP
     *
     * @param packageName
     * @param isAdd
     */
    public void addNonDormantList(String packageName, boolean isAdd) {
        AppInfo appInfo = mAllDatasMap.get(packageName);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                initAllDatas();
                break;
            case R.id.clean:
                for (String packageName : mAllDatasMap.keySet()) {
                    AppInfo appInfo = mAllDatasMap.get(packageName);
                    if (appInfo.getRunState(this) == Constants.FORWARD_APP
                            || appInfo.getRunState(this) == Constants.BACKGROUND_APP) {
                        forceStopAPK(packageName);
                    }
                }
                break;
        }
    }

    @Override
    public boolean onHover(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_HOVER_ENTER:
                if (v.getId() == R.id.layout) {
                    v.setBackgroundColor(getResources().getColor(R.color.theme));
                } else {
                    mHoverText.show(v, getHoverText(v));
                }
                break;
            case MotionEvent.ACTION_HOVER_EXIT:
                if (v.getId() == R.id.layout) {
                    v.setBackgroundColor(getResources().getColor(R.color.transparent));
                } else {
                    mHoverText.dismiss();
                }
                break;
        }

        return false;
    }

    private String getHoverText(View view) {
        AppInfo appInfo = null;
        if (view.getTag() != null) {
            appInfo = (AppInfo) view.getTag();
        }
        switch (view.getId()) {
            case R.id.refresh:
                return getString(R.string.refresh);
            case R.id.clean:
                return getString(R.string.stop_running_application);
            case R.id.dormant:
                return getString(R.string.dormant);
            case R.id.add_or_remove:
                if (appInfo.isNonDormant()) {
                    return getString(R.string.non_dormant);
                } else {
                    return getString(R.string.add_non_dormant_list);
                }
            case R.id.prevent:
                if (appInfo.isAutoPrevent()) {
                    return getString(R.string.auto_prevent);
                } else {
                    return getString(R.string.remove);
                }
        }
        return "";
    }

    private static class Position {
        int pos;
        int top;

        public Position(int pos, int top) {
            this.pos = pos;
            this.top = top;
        }
    }

    private class BatteryChangeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            int level = extras.getInt(BatteryManager.EXTRA_LEVEL, 0);
            int status = extras.getInt(BatteryManager.EXTRA_STATUS);
            mBatteryCharge.setVisibility(View.VISIBLE);
            switch (status) {
                case BatteryManager.BATTERY_STATUS_CHARGING:
                    mBattertState.setText(
                            getString(R.string.battery_state, getString(R.string.charging)));
                    mBatteryCharge.setText(getString(R.string.battery_charge, level));
                    break;
                case BatteryManager.BATTERY_STATUS_DISCHARGING:
                    mBattertState.setText(
                            getString(R.string.battery_state, getString(R.string.discharging)));
                    mBatteryCharge.setText(getString(R.string.battery_charge, level));
                    break;
                case BatteryManager.BATTERY_STATUS_FULL:
                    mBattertState.setText(
                            getString(R.string.battery_state, getString(R.string.battery_full)));
                    mBatteryCharge.setText(getString(R.string.battery_charge, level));
                    break;
                default:
                    mBattertState.setText(
                            getString(R.string.battery_state, getString(R.string.battery_none)));
                    mBatteryCharge.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private class AppInstallReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String packageName = intent.getData().getSchemeSpecificPart();
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            initAllDatas();
                        }
                    }, 500);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    mAllDatasMap.remove(packageName);
                    PreventAppUtils.getInstance(MainActivity.this).removeAddApp(packageName);
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    break;
            }
        }
    }

    private class RefreshRunnable implements Runnable {

        @Override
        public void run() {
            initCpuInfo(new OnCpuChangeListener() {
                @Override
                public void cpuUse(double cpuUse) {
                    mTotalCpuUsed = cpuUse;
                }

                @Override
                public void loadComplete() {
                    for (String packageName : mAllDatasMap.keySet()) {
                        AppInfo appInfo = mAllDatasMap.get(packageName);
                        if (mCpuMap.containsKey(packageName)) {
                            appInfo.setCpuUsage(mCpuMap.get(packageName));
                        } else {
                            appInfo.clearCpuUsage();
                        }
                    }
                    mCpuMaxFreqGHz = DeviceUtils.getCurCpuFreq();
                    refreshAllDataState();
                    loadData();
                }
            });
        }
    }

    /**
     * init cpu info
     */
    private void initCpuInfo(OnCpuChangeListener onCpuChangeListener) {
        Process process;
        BufferedReader reader;
        try {
            process = Runtime.getRuntime().exec("/system/bin/top -n 1");
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            boolean isIgnore = true;
            mCpuMap.clear();
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.contains("User") && line.contains("System")) {
                    isIgnore = true;
                    String replace = line.replace("User", "").replace("System", "")
                            .replace("IOW", "").replace("IRQ", "").replace("%", "").replace(" ", "");
                    String[] split = replace.split(",");
                    mTotalCpuUsed = 0.0;
                    for (String s : split) {
                        mTotalCpuUsed += Integer.parseInt(s);
                    }
                    onCpuChangeListener.cpuUse(mTotalCpuUsed);
                } else if (line.contains("PID")) {
                    isIgnore = false;
                } else if (!isIgnore) {
                    String[] split = line.replace("%", "").split(Constants.ONE_OR_MORE_SPACE);
                    double cpuUsed = Double.parseDouble(split[2]);
                    if (split.length == 10 && cpuUsed > 0) {
                        mCpuMap.put(split[9], cpuUsed);
                    } else {
                        break;
                    }
                }
            }
            onCpuChangeListener.loadComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}