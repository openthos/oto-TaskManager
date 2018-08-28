package org.openthos.taskmanager.prevent.ui;

import android.app.ActivityManager;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Debug;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.TypedValue;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.wenming.library.processutil.ProcessManager;
import com.wenming.library.processutil.models.AndroidAppProcess;

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

import org.openthos.taskmanager.BaseFragment;
import org.openthos.taskmanager.R;
import org.openthos.taskmanager.adapter.PreventAdapter;
import org.openthos.taskmanager.adapter.PreventLayoutAdapter;
import org.openthos.taskmanager.app.Constants;
import org.openthos.taskmanager.bean.AppInfo;
import org.openthos.taskmanager.bean.AppLayoutInfo;
import org.openthos.taskmanager.listener.OnCpuChangeListener;
import org.openthos.taskmanager.listener.OnListClickListener;
import org.openthos.taskmanager.listener.OnTaskCallBack;
import org.openthos.taskmanager.prevent.common.PackageUtils;
import org.openthos.taskmanager.prevent.ui.PreventActivity;
import org.openthos.taskmanager.prevent.ui.util.StatusUtils;
import org.openthos.taskmanager.prevent.ui.util.UILog;
import org.openthos.taskmanager.task.RetrieveInfoTask;
import org.openthos.taskmanager.utils.DeviceUtils;
import org.openthos.taskmanager.utils.NonDormantAppUtils;
import org.openthos.taskmanager.view.HoverTextView;

public class PreventFragment extends BaseFragment
        implements OnListClickListener, View.OnClickListener, View.OnHoverListener {

    private PreventLayoutAdapter mAdapter;
    private PreventActivity mActivity;
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


    public PreventFragment() {
    }

    @Override
    public void onDestroyView() {
        saveListPosition();
        super.onDestroyView();
        mActivity = null;
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_prevent;
    }

    @Override
    public void initView(View view) {
        mMainLayout = (FrameLayout) view.findViewById(R.id.main_layout);
        mRefresh = (ImageView) view.findViewById(R.id.refresh);
        mClean = (ImageView) view.findViewById(R.id.clean);
        mCpuFrequence = (TextView) view.findViewById(R.id.cpu_frquence);
        mCpuUse = (TextView) view.findViewById(R.id.cpu_use);
        mBattertState = (TextView) view.findViewById(R.id.battery_state);
        mBatteryCharge = (TextView) view.findViewById(R.id.battery_charge);
        mListView = (ListView) view.findViewById(R.id.listview);
        mHoverText = (HoverTextView) view.findViewById(R.id.hove_text);
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
        mActivity = (PreventActivity) getActivity();
        appNotification = PreferenceManager.getDefaultSharedPreferences(mActivity).
                getBoolean("app_notification", Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);

        mHoverText.setParentView(mMainLayout);
        mAdapter = new PreventLayoutAdapter(mActivity);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(
                LayoutInflater.from(mActivity).inflate(R.layout.prevent_list_header, null, false));

        mHandler = new Handler();
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
        new RetrieveInfoTask(mActivity, getPreventPkgNames(mActivity), new OnTaskCallBack() {
            @Override
            public void callBack(List<AppInfo> appInfos) {
                mAllDatasMap.clear();
                for (AppInfo appInfo : appInfos) {
                    mAllDatasMap.put(appInfo.getPackageName(), appInfo);
                    initAllDataState();
                    loadData();
                }
            }
        }).execute();
    }

    private void initAllDataState() {
        Map<String, Boolean> preventPackages = mActivity.getPreventPackages();
        Map<String, String> nonDormantMaps = NonDormantAppUtils.getInstance(mActivity).getAllAddedApp();
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

    private void refreshAllDataState() {
        Map<String, Set<Long>> running = mActivity.getRunningProcesses();
        for (String packageName : mAllDatasMap.keySet()) {
            AppInfo appInfo = mAllDatasMap.get(packageName);
            Set<Long> importances = running.get(packageName);
            if (running != null && running.containsKey(packageName)) {
                appInfo.setRunning(importances);
            } else {
                appInfo.setRunning(null);
            }
            appInfo.setRunDescribe(StatusUtils.formatRunning(mActivity, importances).toString());
            appInfo.setMemoryUsage(0);
        }
        getMemorySize();
    }

    private void getMemorySize() {
        ActivityManager am = (ActivityManager) mActivity.getSystemService(Context.ACTIVITY_SERVICE);
        List<AndroidAppProcess> listInfo = ProcessManager.getRunningAppProcesses();
        for (AndroidAppProcess info : listInfo) {
            AppInfo appInfo = mAllDatasMap.get(info.name);
            if (appInfo != null) {
                int[] mempid = new int[]{info.pid};
                Debug.MemoryInfo[] memoryInfo = am.getProcessMemoryInfo(mempid);
                double memSize = memoryInfo[0].getTotalPss() / 1024;
                appInfo.setMemoryUsage(memSize);
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
            switch (appInfo.getRunState(mActivity)) {
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
        mActivity.registerReceiver(mBatteryChangeReceiver, filter);
    }

    private void registAppInstallReceiver() {
        mAppInstallReceiver = new AppInstallReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REPLACED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addDataScheme("package");
        mActivity.registerReceiver(mAppInstallReceiver, filter);
    }

    @Override
    public void onPause() {
        saveListPosition();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (mBatteryChangeReceiver != null) {
            mActivity.unregisterReceiver(mBatteryChangeReceiver);
            mBatteryChangeReceiver = null;
        }
        if (mAppInstallReceiver != null) {
            mActivity.unregisterReceiver(mAppInstallReceiver);
            mAppInstallReceiver = null;
        }
        super.onDestroy();
    }

    private int getHeaderIconWidth() {
        if (headerIconWidth == 0) {
            headerIconWidth = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, HEADER_ICON_WIDTH,
                    getResources().getDisplayMetrics());
        }
        return headerIconWidth;
    }

    private boolean canCreateContextMenu(ContextMenu menu, ContextMenuInfo menuInfo) {
        return mActivity != null && menu != null && menuInfo != null;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        if (!canCreateContextMenu(menu, menuInfo)) {
            return;
        }
//        menu.clear();
//        PreventAdapter.ViewHolder holder = (PreventAdapter.ViewHolder) ((AdapterContextMenuInfo) menuInfo).targetView.getTag();
//        menu.setHeaderTitle(holder.nameView.getText());
//        if (holder.icon != null) {
//            setHeaderIcon(menu, holder.icon);
//        }
//        menu.add(Menu.NONE, R.string.app_info, Menu.NONE, R.string.app_info);
//        if (canPreventAll()) {
//            updatePreventMenu(menu, holder.packageName);
//        }
//        if (getMainIntent(holder.packageName) != null) {
//            menu.add(Menu.NONE, R.string.open, Menu.NONE, R.string.open);
//        }
//        if (holder.canUninstall) {
//            menu.add(Menu.NONE, R.string.uninstall, Menu.NONE, R.string.uninstall);
//        }
//        if (appNotification) {
//            menu.add(Menu.NONE, R.string.app_notifications, Menu.NONE, R.string.app_notifications);
//        }
    }

    private boolean canPreventAll() {
        return true;
    }

    private void updatePreventMenu(Menu menu, String packageName) {
        if (mActivity.getPreventPackages().containsKey(packageName)) {
            menu.add(Menu.NONE, R.string.remove, Menu.NONE, R.string.remove);
        } else {
            menu.add(Menu.NONE, R.string.prevent, Menu.NONE, R.string.prevent);
        }
    }

    private void setHeaderIcon(ContextMenu menu, Drawable icon) {
        int width = getHeaderIconWidth();
        if (icon.getMinimumWidth() <= width) {
            menu.setHeaderIcon(icon);
        } else if (icon instanceof BitmapDrawable) {
            Bitmap bitmap = Bitmap.createScaledBitmap(((BitmapDrawable) icon).getBitmap(), width, width, false);
            menu.setHeaderIcon(new BitmapDrawable(getResources(), bitmap));
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
//        if (mActivity == null || item == null) {
//            return false;
//        }
//        PreventAdapter.ViewHolder holder = (PreventAdapter.ViewHolder) ((AdapterContextMenuInfo) item.getMenuInfo()).targetView.getTag();
//        return onContextItemSelected(holder, holder.packageName, item.getItemId());
        return false;
    }

    private boolean onContextItemSelected(PreventAdapter.ViewHolder holder, String packageName, int id) {
        switch (id) {
            case R.string.app_info:
                startActivity(id, packageName);
                break;
            case R.string.uninstall:
                startActivity(id, packageName);
                break;
            case R.string.app_notifications:
                startNotification(packageName);
                break;
            case R.string.remove:
                Toast.makeText(getActivity(), "item remove" + packageName, Toast.LENGTH_SHORT).show();
                updatePrevent(id, holder, packageName);
                break;
            case R.string.prevent:
                Toast.makeText(getActivity(), "item prevent" + packageName, Toast.LENGTH_SHORT).show();
                updatePrevent(id, holder, packageName);
                break;
            case R.string.open:
                startPackage(packageName);
                break;
        }
        return true;
    }

    private boolean updatePrevent(int id, PreventAdapter.ViewHolder holder, String packageName) {
        switch (id) {
            case R.string.prevent:
                holder.preventView.setVisibility(View.VISIBLE);
                holder.preventView.setImageResource(StatusUtils.getDrawable(holder.running, false));
                mActivity.changePrevent(packageName, true);
                break;
            case R.string.remove:
                holder.preventView.setVisibility(View.GONE);
                mActivity.changePrevent(packageName, false);
                break;
        }
        return true;
    }

    private boolean startNotification(String packageName) {
        ApplicationInfo info;
        try {
            info = mActivity.getPackageManager().getApplicationInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            UILog.d("cannot find package " + packageName, e);
            return false;
        }
        int uid = info.uid;
        Intent intent = new Intent("android.settings.APP_NOTIFICATION_SETTINGS")
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra("app_package", packageName)
                .putExtra("app_uid", uid);
        try {
            mActivity.startActivity(intent);
            return true;
        } catch (ActivityNotFoundException e) {
            appNotification = false;
            PreferenceManager.getDefaultSharedPreferences(mActivity).edit().putBoolean("app_notification", false).apply();
            UILog.d("cannot start notification for " + packageName, e);
            return false;
        }
    }

    private boolean startActivity(int id, String packageName) {
        String action;
        if (id == R.string.app_info) {
            action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
        } else if (id == R.string.uninstall) {
            action = Intent.ACTION_DELETE;
        } else {
            return false;
        }
        mActivity.startActivity(new Intent(action, Uri.fromParts("package", packageName, null)));
        return true;
    }

    private boolean startPackage(String packageName) {
        Intent intent = getMainIntent(packageName);
        if (intent != null) {
            mActivity.startActivity(intent);
        }
        return true;
    }

    private Intent getMainIntent(String packageName) {
        return mActivity.getPackageManager().getLaunchIntentForPackage(packageName);
    }

    public void saveListPosition() {
        if (mAdapter != null) {
            int position = mListView.getFirstVisiblePosition();
            View v = mListView.getChildAt(0);
            int top = (v == null) ? 0 : v.getTop();
            setListPosition(new Position(position, top));
        }
    }

    private void setListPosition(Position position) {
        positions.put(getClass().getName(), position);
    }

    private Position getListPosition() {
        return positions.get(getClass().getName());
    }

    public void updateTimeIfNeeded(String packageName) {
//        if (scrolling || mAdapter == null) {
//            return;
//        }
//        int size = mAdapter.getCount();
//        for (int i = 0; i < size; ++i) {
//            View view = mListView.getChildAt(i);
//            if (view == null || view.getTag() == null || view.getVisibility() != View.VISIBLE) {
//                continue;
//            }
//            PreventAdapter.ViewHolder holder = (PreventAdapter.ViewHolder) view.getTag();
//            if (PackageUtils.equals(packageName, holder.packageName)) {
//                holder.updatePreventView(mActivity);
//                holder.running = mActivity.getRunningProcesses().get(packageName);
//                holder.summaryView.setText(StatusUtils.formatRunning(mActivity, holder.running));
//            } else if (holder.running != null) {
//                holder.summaryView.setText(StatusUtils.formatRunning(mActivity, holder.running));
//            }
//        }
    }

    protected Set<String> getPreventPkgNames(PreventActivity activity) {
        Set<String> names = new HashSet<>();
        PackageManager pm = activity.getPackageManager();
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
                mListView.showContextMenuForChild(view);
                break;
            case R.id.dormant:
                forceStopAPK(packageName);
                mActivity.retrieveRunning();
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
                    mActivity.changePrevent(packageName, appInfo.isAutoPrevent());
                }
                loadData();
                break;
        }
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
                NonDormantAppUtils.getInstance(getActivity()).saveAddedApp(packageName, appInfo.getAppName());
                appInfo.setNonDormant(true);
            } else {
                NonDormantAppUtils.getInstance(getActivity()).removeAddApp(packageName);
                appInfo.setNonDormant(false);
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.refresh:
                mActivity.retrieveRunning();
                break;
            case R.id.clean:
                for (String packageName : mAllDatasMap.keySet()) {
                    AppInfo appInfo = mAllDatasMap.get(packageName);
                    if (appInfo.getRunState(mActivity) == Constants.FORWARD_APP) {
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
            boolean isPlug = extras.getInt(BatteryManager.EXTRA_PLUGGED, 0) != 0;
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
                    if (!isPlug) {
                        mBattertState.setText(
                                getString(R.string.battery_state, getString(R.string.battery_none)));
                        mBatteryCharge.setVisibility(View.GONE);
                    } else {
                        mBattertState.setText(getString(R.string.battery_state, getString(R.string.battery_full)));
                        mBatteryCharge.setText(getString(R.string.battery_charge, level));
                    }
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
                    },500);
                    break;
                case Intent.ACTION_PACKAGE_REMOVED:
                    mAllDatasMap.remove(packageName);
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