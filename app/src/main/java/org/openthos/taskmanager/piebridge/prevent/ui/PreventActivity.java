package org.openthos.taskmanager.piebridge.prevent.ui;

import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentUtils;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openthos.taskmanager.R;
import org.openthos.taskmanager.piebridge.prevent.common.PackageUtils;
import org.openthos.taskmanager.piebridge.prevent.common.PreventIntent;
import org.openthos.taskmanager.piebridge.prevent.ui.util.PreventListUtils;
import org.openthos.taskmanager.piebridge.prevent.ui.util.PreventUtils;
import org.openthos.taskmanager.piebridge.prevent.ui.util.UILog;

public class PreventActivity extends FragmentActivity implements
        ViewPager.OnPageChangeListener, View.OnClickListener {

    private static final int PREVENT_LIST = 1;
    private static final int APPLICATIONS = 0;

    private ViewPager mPager;
    private String[] mPageTitles;
    private List<Set<String>> mPageSelections;

    private static Map<String, Boolean> mPreventPackages = null;
    private static Map<String, Set<Long>> running = new HashMap<>();

    private View main;
    private ProgressDialog dialog;
    private Integer dangerousColor = null;
    private Integer transparentColor = null;
    private BroadcastReceiver receiver;
    private Handler mHandler;
    private Handler mainHandler;
    private final Object preventLock = new Object();
    private boolean initialized;
    private boolean paused;
    private int code;
    private String name;

    public int getDangerousColor() {
        if (dangerousColor == null) {
            dangerousColor = getThemedColor(R.attr.color_dangerous);
        }
        return dangerousColor;
    }

    public int getTransparentColor() {
        if (transparentColor == null) {
            transparentColor = getResourceColor(android.R.color.transparent);
        }
        return transparentColor;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*try {
            Class<?> clazz = Class.forName("de.robv.android.xposed.XposedBridge", false,
                    ClassLoader.getSystemClassLoader());
            Field field = clazz.getDeclaredField("disableHooks");
            field.setAccessible(true);
            field.set(null, true);
            if (BuildConfig.DONATE && XposedUtils.canDisableXposed()) {
                XposedUtils.disableXposed(clazz);
            } else {
                field.set(null, false);
            }
        } catch (Throwable t) { // NOSONAR
        }*/
        //ThemeUtils.setTheme(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prevent_main);
        //ThemeUtils.fixSmartBar(this);

        mPager = (ViewPager) findViewById(R.id.pager);
        main = findViewById(R.id.main);
        findViewById(R.id.mismatch).setOnClickListener(this);
        receiver = new HookReceiver();

        /**
         * 所有程序 / 阻止列表
         * */
//        mPageTitles = new String[]{getString(R.string.applications), getString(R.string.prevent_list)};
        mPageTitles = new String[]{getString(R.string.applications)};
        mPageSelections = new ArrayList<Set<String>>();
        mPageSelections.add(new HashSet<String>());
        mPageSelections.add(new HashSet<String>());
        mPager.addOnPageChangeListener(this);
        mPager.setAdapter(new ScreenSlidePagerAdapter(getSupportFragmentManager()));

        HandlerThread thread = new HandlerThread("PreventUI");
        thread.start();
        mHandler = new Handler(thread.getLooper());
        mainHandler = new Handler(getMainLooper());
        initialize();

    }

    private void initialize() {
        initialized = true;
        showProcessDialog(R.string.retrieving);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!paused) {
                    retrievePrevents();
                }
            }
        }, 0x100);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        if (initialized) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!paused) {
                        retrievePrevents();
                    }
                }
            }, 0x400);
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mPreventPackages == null && !paused) {
                        showRetrieving();
                    }
                }
            }, 0x500);
        }
    }

    private void showRetrieving() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                synchronized (preventLock) {
                    if (mPreventPackages == null) {
                        showProcessDialog(R.string.retrieving);//正在获取数据，请稍候. . .
                    }
                }
            }
        });
    }

    public void retrievePrevents() {
        PackageUtils.clearInputMethodPackages();
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setAction(PreventIntent.ACTION_GET_PACKAGES);//已经被阻止的
        intent.setData(Uri.fromParts(PreventIntent.SCHEME, getPackageName(), null));
        Log.i("PreventActivity::", "retrievePrevents()" + getPackageName());
        UILog.i("sending get prevent packages broadcast");
        sendOrderedBroadcast(intent, PreventIntent.PERMISSION_SYSTEM, receiver, mHandler,
                0, null, null);
    }

    public void retrieveRunning() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setAction(PreventIntent.ACTION_GET_PROCESSES);//正在运行
        intent.setData(Uri.fromParts(PreventIntent.SCHEME, getPackageName(), null));
        Log.i("PreventActivity::", "retrieveRunning()" + getPackageName());
        UILog.i("sending get processes broadcast");
        sendOrderedBroadcast(intent, PreventIntent.PERMISSION_SYSTEM, receiver, mHandler, 0, null, null);
        if (name == null) {
            retrieveInfo();
        }
    }

    private void retrieveInfo() {
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY | Intent.FLAG_RECEIVER_FOREGROUND);
        intent.setAction(PreventIntent.ACTION_GET_INFO);
        intent.setData(Uri.fromParts(PreventIntent.SCHEME, getPackageName(), null));
        Log.i("PreventActivity::", "retrieveInfo()" + getPackageName());
        UILog.i("sending get info broadcast");
        sendOrderedBroadcast(intent, PreventIntent.PERMISSION_SYSTEM, receiver, mHandler, 0, null, null);
    }

    public Map<String, Set<Long>> getRunningProcesses() {
        return running;
    }

    @Override
    public void onPageScrollStateChanged(int position) {
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        refresh(position, false);
    }

    /**
     * 获取所有 selected
     */
    public Set<String> getSelection() {
        return mPageSelections.get(mPager.getCurrentItem());
    }

    /**
     * Smaster
     * --> PreventUtils.updatePreventPkg()
     * --> 广播 --> PreventIntent.ACTION_UPDATE_PREVENT
     * --> SystemReceiver中接收后进行json存储
     * --> handlePackages()方法可以读取到；
     */
    public void changePrevent(String packageName, boolean prevent) {
        PreventUtils.updatePreventPkg(this, new String[]{packageName}, prevent);
        if (prevent) {
            /**
             * 第二个参数判断是否正在运行；
             * */
            mPreventPackages.put(packageName, !running.containsKey(packageName));
        } else {
            mPreventPackages.remove(packageName);
        }
        savePackages();
    }

    private void savePackages() {
        //refreshIfNeeded();
        int position = mPager.getCurrentItem();
        int size = mPager.getAdapter().getCount();
        for (int item = 0; item < size; ++item) {
            if (item == position) {
                refresh(item, false);
            } else {
                refresh(item, true);
            }
        }
    }

    /**
     * 获取已经阻止的应用
     * Smaster
     */
    public Map<String, Boolean> getPreventPackages() {
        if (mPreventPackages == null) {
            return new HashMap<>();
        } else {
            return mPreventPackages;
        }
    }

    /**
     * 需要参数: mPreventPackages
     */
    private class HookReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (PreventIntent.ACTION_GET_PROCESSES.equals(action)) {
                handleGetProcesses();
                showFragments();
            } else if (PreventIntent.ACTION_GET_PACKAGES.equals(action)) {
                handleGetPackages();
            } /*else if (Intent.ACTION_PACKAGE_RESTARTED.equals(action)) { //restarted
                String packageName = PackageUtils.getPackageName(intent);
                if (running != null) {
                    running.remove(packageName);
                }
                if (mPreventPackages != null && Boolean.FALSE.equals(mPreventPackages.get(packageName))) {
                    mPreventPackages.put(packageName, true);
                }
                updateTimeIfNeeded(packageName);
            }*/ else if (PreventIntent.ACTION_GET_INFO.equals(action)) {
                handleGetInfo(context);
            }
        }

        private void showFragments() {
            if (dialog != null && dialog.isShowing()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (refresh(true)) {
                            dialog.dismiss();
                        } else {
                            showViewPager();
                            retrieveRunning();
                        }
                    }
                });
            }
        }

        private void handleGetInfo(Context context) {
            String info = getResultData();
            if (TextUtils.isEmpty(info)) {
                return;
            }
            try {
                /**
                 * 通过json进行的存储
                 * Smaster
                 * */
                JSONObject json = new JSONObject(info);
                name = json.optString("name");
                code = json.optInt("code");
                PreventUtils.updateConfiguration(context, json);
                //showRebootIfNeeded();
            } catch (JSONException e) {
                UILog.d("cannot get version from " + info, e);
            }
        }

        private void handleGetProcesses() {
            UILog.i("received get processes broadcast");
            String result = getResultData();
            if (result != null) {
                handleProcesses(result);
            }
        }

        private void handleProcesses(String result) {
            try {
                JSONObject json = new JSONObject(result);
                Map<String, Set<Long>> processes = new HashMap<>();
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    String value = json.optString(key);
                    if (value != null) {
                        processes.put(key, convertImportance(value));
                    }
                }
                running.clear();
                running.putAll(processes);
                Log.i("ljh","running " + running.size());
                notifyDataSetChanged();
            } catch (JSONException e) {
                UILog.e("cannot convert to json", e);
            }
        }

        /**
         * 将存储再Json中的读取--> mPreventPackages集合中；
         */
        private void handlePackages(String result) {
            try {
                JSONObject json = new JSONObject(result);
                Map<String, Boolean> prevents = new HashMap<>();
                Iterator<String> it = json.keys();
                while (it.hasNext()) {
                    String key = it.next();
                    prevents.put(key, json.optBoolean(key));
                }
                Log.i("ljh","prevent "+prevents.size());
                /**
                 * json 串
                 * result{"com.microsoft.office.excel":true,"com.microsoft.office.powerpoint":true}
                 * */
                Log.i("PreventActivity::", "handlePackages::" + result);
                synchronized (preventLock) {
                    if (mPreventPackages == null) {
                        mPreventPackages = new HashMap<>();
                    } else {
                        mPreventPackages.clear();
                    }
                    mPreventPackages.putAll(prevents);
                }
                boolean synced = PreventListUtils.getInstance().syncIfNeeded(PreventActivity.this,
                        prevents.keySet());
                if (synced) {
                    retrievePrevents();
                }
            } catch (JSONException e) {
                UILog.e("cannot convert to json: " + result, e);
            }
        }

        private boolean handleGetPackages() {
            final String result = getResultData();
            Log.i("PreventActivity::", "---> handleGetPackages-->result" + result);
            if (result != null) {
                handlePackages(result);
                if (mPreventPackages != null) {
                    showViewPager();
                    retrieveRunning();
                    return true;
                }
            }
            return false;
        }

        private Set<Long> convertImportance(String value) {
            Set<Long> importance = new LinkedHashSet<Long>();
            for (String s : value.split(",")) {
                if (!TextUtils.isEmpty(s)) {
                    try {
                        importance.add(Long.parseLong(s));
                    } catch (NumberFormatException e) {
                        UILog.d("cannot format " + s, e);
                    }
                }
            }
            return importance;
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.mismatch && code > 0) {
            PreventUtils.confirmReboot(this);
        }
    }

    @SuppressWarnings("deprecation")
    public int getResourceColor(int colorId) {
        return getResources().getColor(colorId);
    }

    public int getThemed(int resId) {
        TypedValue tv = new TypedValue();
        getTheme().resolveAttribute(resId, tv, true);
        return tv.resourceId;
    }

    public int getThemedColor(int resId) {
        return getResourceColor(getThemed(resId));
    }

    private void showProcessDialog(int resId) {
        if (paused) {
            return;
        }
        if (dialog == null) {
            dialog = new ProgressDialog(this);
        }
        dialog.setTitle(R.string.app_name);
        dialog.setIcon(R.mipmap.ic_launcher);
        dialog.setCancelable(false);
        dialog.setMessage(getString(resId));
        dialog.show();
    }

    private boolean refresh(int position, boolean force) {
        String tag = getTag(position);
        int currentItem = mPager.getCurrentItem();
        PreventFragment fragment = (PreventFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            fragment.saveListPosition();
            fragment.refresh(force);
            if (position == currentItem) {
//                fragment.startTaskIfNeeded();
            }
            return true;
        } else {
            UILog.e("fragment is null in " + position);
            return false;
        }
    }

    private void updateTimeIfNeeded(String packageName) {
        int position = mPager.getCurrentItem();
        String tag = getTag(position);
        final PreventFragment fragment = (PreventFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            fragment.updateTimeIfNeeded(packageName);
        }
    }

    private void notifyDataSetChanged() {
        int position = mPager.getCurrentItem();
        String tag = getTag(position);
        final PreventFragment fragment = (PreventFragment) getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fragment.notifyDataSetChanged();
                }
            });
        }
    }

    private void showViewPager() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                main.setVisibility(View.VISIBLE);
            }
        });
    }

    private boolean refresh(boolean force) {
        boolean showed = false;
        int size = mPager.getAdapter().getCount();
        for (int item = 0; item < size; ++item) {
            if (refresh(item, force)) {
                showed = true;
            }
        }
        return showed;
    }

    private static String getTag(int position) {
        return "fragment-" + position;
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment;
            switch (position) {
                case APPLICATIONS:
                    fragment = new PreventFragment();
                    break;
                default:
                    return null;
            }
            FragmentUtils.setTag(fragment, getTag(position));
            return fragment;
        }

        @Override
        public int getCount() {
            return mPageTitles.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mPageTitles[position];
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(Intent.ACTION_PACKAGE_RESTARTED);
        filter.addDataScheme("package");
        registerReceiver(receiver, filter);
        paused = false;
        mainHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!paused) {
                    updateTimeIfNeeded(null);
                    retrieveRunning();
                    mainHandler.postDelayed(this, 1000);
                }
            }
        }, 1000);
    }

    @Override
    protected void onPause() {
        unregisterReceiver(receiver);
        super.onPause();
        paused = true;
    }

    @Override
    public void onStop() {
        mPreventPackages = null;
        super.onStop();
    }
}
