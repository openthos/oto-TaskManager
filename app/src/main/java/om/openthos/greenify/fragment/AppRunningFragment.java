package om.openthos.greenify.fragment;

import android.view.View;

import om.openthos.greenify.BaseFragment;
import om.openthos.greenify.R;

public class AppRunningFragment extends BaseFragment {


    public AppRunningFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_app_running;
    }

    @Override
    public void initView(View view) {

    }

    @Override
    public void initData() {

    }

    @Override
    public void initListener() {

    }

    @Override
    public void refresh() {

    }

    @Override
    public String getFragmentName() {
        return getString(R.string.running);
    }
}