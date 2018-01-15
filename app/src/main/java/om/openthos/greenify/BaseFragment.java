package om.openthos.greenify;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Map;

import om.openthos.greenify.bean.AppInfo;

/**
 * Created by ljh on 18-1-11.
 */

public abstract class BaseFragment extends Fragment {

    public BaseFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(getLayoutId(), container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initView(view);
        initData();
        initListener();
    }

    public Map<String, AppInfo> getAppInfosMap() {
        return ((MainActivity) getActivity()).getAppInfosMap();
    }

    public abstract int getLayoutId();

    public abstract void initView(View view);

    public abstract void initData();

    public abstract void initListener();

    public abstract void refresh();

    public abstract String getFragmentName();
}
