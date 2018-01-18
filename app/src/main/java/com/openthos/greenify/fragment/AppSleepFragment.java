package com.openthos.greenify.fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.openthos.greenify.BaseFragment;
import com.openthos.greenify.R;
import com.openthos.greenify.adapter.SleepAppAdapter;
import com.openthos.greenify.bean.AppInfo;

public class AppSleepFragment extends BaseFragment implements AdapterView.OnItemClickListener {
    private ListView mListView;
    private SleepAppAdapter mAdapter;
    private List<AppInfo> mDatas;
    private View mLastView;

    public AppSleepFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_app_sleep;
    }

    @Override
    public void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listview);
    }

    @Override
    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new SleepAppAdapter(getActivity(), mDatas);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(
                LayoutInflater.from(getActivity()).inflate(R.layout.item_sleep_app, null, false));
        refresh();
    }

    @Override
    public void initListener() {
        mListView.setOnItemClickListener(this);
    }

    @Override
    public void refresh() {
        mDatas.clear();
        Map<String, AppInfo> appInfoMap = getAppInfosMap();
        for (String packageName : appInfoMap.keySet()) {
            AppInfo appInfo = appInfoMap.get(packageName);
            if (appInfo.isAdd()) {
                mDatas.add(appInfo);
            }
        }
        mAdapter.refreshList();
    }

    @Override
    public String getFragmentName() {
        return getString(R.string.sleep_list);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (position == 0) {
            return;
        }
        view.setSelected(true);
        if (mLastView != null && mLastView != view) {
            mLastView.setSelected(false);
        }
        mLastView = view;
    }
}