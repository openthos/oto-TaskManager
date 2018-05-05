package org.openthos.taskmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BaseFragment extends Fragment {

    public BaseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
    }

    /**
     * Kill the application process
     *
     * @param pkgName
     */
    public void forceStopAPK(String pkgName) {
        ((BaseActivity) getActivity()).forceStopAPK(pkgName);
    }

    public abstract int getLayoutId();

    public abstract void initView(View view);

    public abstract void initData();

    public abstract void initListener();

}
