package com.openthos.greenify;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;

import com.openthos.greenify.adapter.AppLayoutAdapter;
import com.openthos.greenify.app.Constants;
import com.openthos.greenify.bean.AppInfo;
import com.openthos.greenify.bean.AppLayoutInfo;
import com.openthos.greenify.listener.OnListClickListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends BaseActivity implements OnListClickListener, View.OnClickListener {
    private Handler mHandler;
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

    private ScreenStatusReceiver mScreenStatusReceiver;

    private ImageView mRefresh;
    private ListView mListView;
    private View mLastView;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
        mRefresh = (ImageView) findViewById(R.id.refresh);
        mListView = (ListView) findViewById(R.id.listview);
    }

    @Override
    public void initData() {
        mHandler = new Handler();
        registSreenStatusReceiver();
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
        mAdapter.refreshList();
    }

    @Override
    public void refresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadData();
            }
        }, DELAY_TIME_REFRESH);
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

    @Override
    protected void onDestroy() {
        if (mScreenStatusReceiver != null) {
            unregisterReceiver(mScreenStatusReceiver);
            mScreenStatusReceiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onListClickListener(View view, String packageName) {
        switch (view.getId()) {
            case R.id.dormant:
                forceStopAPK(packageName);
                refresh();
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
                    refresh();
                    break;
            }
        }
    }
}
