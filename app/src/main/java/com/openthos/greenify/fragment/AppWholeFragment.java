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
import com.openthos.greenify.adapter.WholeAppAdapter;
import com.openthos.greenify.bean.AppInfo;

public class AppWholeFragment extends BaseFragment implements AdapterView.OnItemClickListener {

    private ListView mListView;
    private WholeAppAdapter mAdapter;
    private List<AppInfo> mDatas;
    private View mLastView;

    public AppWholeFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_app_whole;
    }

    @Override
    public void initView(View view) {
        mListView = (ListView) view.findViewById(R.id.listview);
    }

    @Override
    public void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new WholeAppAdapter(getActivity(), mDatas);
        mListView.setAdapter(mAdapter);
        mListView.addHeaderView(
                LayoutInflater.from(getActivity()).inflate(R.layout.item_whole_app, null, false));
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
            mDatas.add(appInfoMap.get(packageName));
        }
        mAdapter.refreshList();
    }

    @Override
    public String getFragmentName() {
        return getString(R.string.all_app);
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