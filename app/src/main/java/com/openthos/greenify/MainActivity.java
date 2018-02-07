package com.openthos.greenify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.openthos.greenify.adapter.AppLayoutAdapter;
import com.openthos.greenify.app.Constants;
import com.openthos.greenify.bean.AppInfo;
import com.openthos.greenify.bean.AppLayoutInfo;
import com.openthos.greenify.listener.OnCpuChangeListener;
import com.openthos.greenify.listener.OnListClickListener;
import com.openthos.greenify.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends BaseActivity implements OnListClickListener, View.OnClickListener {
    private Handler mHandler;
    private double mCpuMaxFreqGHz;
    private List<AppLayoutInfo> mDatas;
    private AppLayoutAdapter mAdapter;

    //Storage already dormant application
    private List<AppInfo> mHaveDormants;

    //Storage of non dormant applications
    private List<AppInfo> mWaitDormants;

    // //Storage of non need dormant applications
    private List<AppInfo> mNonNeedDormants;

    //Currently selected application package name
    private String mSelectPkgName;

    private ExecutorService mFixedThreadPool = Executors.newFixedThreadPool(3);

    private ScreenStatusReceiver mScreenStatusReceiver;
    private AppInstallReceiver mAppInstallReceiver;
    private BatteryChangeReceiver mBatteryChangeReceiver;
    private RefreshRunnable mRefreshRunnable;

    private ImageView mRefresh;
    private ListView mListView;
    private View mLastView;

    private TextView mCpuFrequence;
    private TextView mCpuUse;
    private TextView mBattertState;
    private TextView mBatteryCharge;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mListView = (ListView) findViewById(R.id.listview);
        mCpuFrequence = (TextView) findViewById(R.id.cpu_frquence);
        mCpuUse = (TextView) findViewById(R.id.cpu_use);
        mBattertState = (TextView) findViewById(R.id.battery_state);
        mBatteryCharge = (TextView) findViewById(R.id.battery_charge);
    }

    @Override
    public void initData() {
        mHandler = new Handler();
        mRefreshRunnable = new RefreshRunnable();
        registSreenStatusReceiver();
        registAppInstallReceiver();
        registBatteryReceiver();
        mCpuMaxFreqGHz = DeviceUtils.getCPUMaxFreqGHz();
        mCpuFrequence.setText(getString(R.string.cpu_frequence, mCpuMaxFreqGHz));
        mCpuUse.setText(getString(R.string.cpu_use, 0.0));
        initAppInfos();
        mDatas = new ArrayList<>();
        mAdapter = new AppLayoutAdapter(this, mDatas);
        mListView.setAdapter(mAdapter);

        mListView.addHeaderView(
                LayoutInflater.from(this).inflate(R.layout.main_list_header, null, false));
        mHaveDormants = new ArrayList<>();
        mNonNeedDormants = new ArrayList<>();
        mWaitDormants = new ArrayList<>();
        loadData();
        mAdapter.refreshList();
        refresh();
    }

    @Override
    public void initListener() {
        mRefresh.setOnClickListener(this);
        mAdapter.setOnListClickListener(this);
    }

    /**
     * Initialization of data
     */
    public void loadData() {
        mDatas.clear();
        mWaitDormants.clear();
        mNonNeedDormants.clear();
        mHaveDormants.clear();

        Map<String, AppInfo> appInfosMap = getAppInfosMap();
        for (String packageName : appInfosMap.keySet()) {
            AppInfo appInfo = getAppInfoByPkgName(packageName);
            switch (appInfo.getDormantState()) {
                case Constants.APP_HAVE_DORMANT:
                    mHaveDormants.add(appInfo);
                    break;
                case Constants.APP_NON_DORMANT:
                    mNonNeedDormants.add(appInfo);
                    break;
                case Constants.APP_WAIT_DORMANT:
                    mWaitDormants.add(appInfo);
                    break;
                case Constants.App_NON_DEAL:
                    if (appInfo.isRun()) {
                        mWaitDormants.add(appInfo);
                    }
                    break;
            }
        }
        if (mWaitDormants.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.wait_dormant), mWaitDormants));
        }
        if (mNonNeedDormants.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.non_dormant), mNonNeedDormants));
        }
        if (mHaveDormants.size() != 0) {
            mDatas.add(new AppLayoutInfo(getString(R.string.have_dormant), mHaveDormants));
        }
    }

    @Override
    public void refresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mFixedThreadPool.execute(mRefreshRunnable);
            }
        }, Constants.DELAY_TIME_REFRESH);
    }

    /**
     * Registered dormant broadcast
     */
    private void registSreenStatusReceiver() {
        mScreenStatusReceiver = new ScreenStatusReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenStatusReceiver, filter);
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

    private void registBatteryReceiver() {
        mBatteryChangeReceiver = new BatteryChangeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(mBatteryChangeReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        if (mScreenStatusReceiver != null) {
            unregisterReceiver(mScreenStatusReceiver);
            mScreenStatusReceiver = null;
        }
        if (mAppInstallReceiver != null) {
            unregisterReceiver(mAppInstallReceiver);
            mAppInstallReceiver = null;
        }

        if (mBatteryChangeReceiver != null) {
            unregisterReceiver(mBatteryChangeReceiver);
            mBatteryChangeReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onListClickListener(View view, String packageName) {
        switch (view.getId()) {
            case R.id.dormant:
                forceStopAPK(packageName);
                loadData();
                break;
            case R.id.img1:
                switch (getAppInfoByPkgName(packageName).getDormantState()) {
                    case Constants.APP_WAIT_DORMANT:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, false);
                        break;
                    case Constants.APP_HAVE_DORMANT:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, false);
                        break;
                    case Constants.APP_NON_DORMANT:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, false);
                        break;
                    case Constants.App_NON_DEAL:
                        addDormantList(packageName, true);
                        addNonDormantList(packageName, false);
                        break;
                }
                loadData();
                break;
            case R.id.img2:
                switch (getAppInfoByPkgName(packageName).getDormantState()) {
                    case Constants.APP_WAIT_DORMANT:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, true);
                        break;
                    case Constants.APP_HAVE_DORMANT:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, true);
                        break;
                    case Constants.APP_NON_DORMANT:
                        addDormantList(packageName, true);
                        addNonDormantList(packageName, false);
                        break;
                    case Constants.App_NON_DEAL:
                        addDormantList(packageName, false);
                        addNonDormantList(packageName, true);
                        break;
                }
                loadData();
                break;
        }
    }

    @Override
    public void onClick(View v) {
        loadData();
    }

    /**
     * Listening to a dormant broadcast
     * The application of auto dormancy into the dormancy list when dormancy
     * Automatically refresh the screen when you open the screen
     */
    private class ScreenStatusReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case Intent.ACTION_SCREEN_OFF:
                    Map<String, AppInfo> appInfosMap = getAppInfosMap();
                    for (String packageName : appInfosMap.keySet()) {
                        AppInfo appInfo = getAppInfoByPkgName(packageName);
                        if (appInfo.getDormantState() == Constants.APP_WAIT_DORMANT) {
                            forceStopAPK(packageName);
                        }
                    }
                    break;
                case Intent.ACTION_SCREEN_ON:
                    loadData();
                    break;
            }
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
            Map<String, AppInfo> appInfosMap = getAppInfosMap();
            String packageName = intent.getData().getSchemeSpecificPart();
            switch (intent.getAction()) {
                case Intent.ACTION_PACKAGE_ADDED:
                    AppInfo appInfo = getAppInfo(packageName);
                    if (appInfo != null) {
                        appInfosMap.put(packageName, appInfo);
                    }
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    appInfosMap.remove(packageName);
                    break;
                case Intent.ACTION_PACKAGE_REPLACED:
                    break;
            }
        }
    }

    private class RefreshRunnable implements Runnable {

        @Override
        public void run() {
            loadData();
            initCpuInfo(new OnCpuChangeListener() {
                @Override
                public void cpuUse(final double cpuUse) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mCpuUse.setText(getString(R.string.cpu_use, cpuUse));
                        }
                    });
                }

                @Override
                public void loadComplete() {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            mAdapter.refreshList();
                        }
                    });
                    refresh();
                }
            });
        }
    }

    private AppInfo getAppInfo(String packageName) {
        PackageManager manager = getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(packageName, 0);
            AppInfo appInfo = new AppInfo();
            appInfo.setAppName(info.loadLabel(manager).toString());
            appInfo.setIcon(info.loadIcon(manager));
            appInfo.setPackageName(packageName);
            return appInfo;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}