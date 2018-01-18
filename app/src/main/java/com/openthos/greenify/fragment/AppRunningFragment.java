package com.openthos.greenify.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.openthos.greenify.BaseFragment;
import com.openthos.greenify.R;
import com.openthos.greenify.adapter.RunningAppAdapter;
import com.openthos.greenify.bean.AppInfo;

public class AppRunningFragment extends BaseFragment {
    private ListView mListView;
    private RunningAppAdapter mAdapter;
    private List<AppInfo> mDatas;


    public AppRunningFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_app_running;
    }

    @Override
    public void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listview);
    }

    @Override
    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new RunningAppAdapter(getActivity(), mDatas);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(
                LayoutInflater.from(getActivity()).inflate(R.layout.item_running_app, null, false));
        refresh();
    }

    @Override
    public void initListener() {
    }

    @Override
    public void refresh() {
        mDatas.clear();
        Map<String, AppInfo> appInfoMap = getAppInfosMap();
        for (String packageName : appInfoMap.keySet()) {
            AppInfo appInfo = appInfoMap.get(packageName);
            if (appInfo.isRun()) {
                mDatas.add(appInfo);
            }
        }
        mAdapter.refreshList();
    }

    @Override
    public String getFragmentName() {
        return getString(R.string.running);
    }
}