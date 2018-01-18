package com.openthos.greenify;

import android.app.FragmentManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.View;

import com.openthos.greenify.fragment.AppRunningFragment;
import com.openthos.greenify.fragment.AppSleepFragment;
import com.openthos.greenify.fragment.AppWholeFragment;

public class MainActivity extends BaseActivity {
    private Handler mHandler;
    private FragmentManager mManager;
    private BaseFragment mRunningFragment;
    private BaseFragment mSleepFragment;
    private BaseFragment mWholeFragment;
    private BaseFragment mCurrentFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView() {
    }

    @Override
    public void initData() {
        mHandler = new Handler();
        mManager = getFragmentManager();
        mRunningFragment = new AppRunningFragment();
        mSleepFragment = new AppSleepFragment();
        mWholeFragment = new AppWholeFragment();

        initAppInfos();
        mManager.beginTransaction()
                .add(R.id.container, mSleepFragment).hide(mSleepFragment)
                .add(R.id.container, mWholeFragment).hide(mWholeFragment)
                .add(R.id.container, mRunningFragment)
                .commit();
        mCurrentFragment = mRunningFragment;
    }

    @Override
    public void initListener() {

    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.running:
                showFragment(mRunningFragment);
                break;
            case R.id.sleep:
                showFragment(mSleepFragment);
                break;
            case R.id.whole:
                showFragment(mWholeFragment);
                break;
        }
    }

    private void showFragment(BaseFragment fragment) {
        if (mCurrentFragment == null) {
            mManager.beginTransaction().show(fragment).commit();
        } else if (mCurrentFragment != fragment) {
            mManager.beginTransaction().hide(mCurrentFragment).show(fragment).commit();
        }
        mManager.executePendingTransactions();
        mCurrentFragment = fragment;
        mCurrentFragment.refresh();
    }

    @Override
    public void refresh() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mCurrentFragment != null) {
                    mCurrentFragment.refresh();
                }
            }
        }, DELAY_TIME_REFRESH);
    }
}
