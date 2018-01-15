package om.openthos.greenify.fragment;

import android.view.View;

import om.openthos.greenify.BaseFragment;
import om.openthos.greenify.R;

public class AppSleepFragment extends BaseFragment {


    public AppSleepFragment() {
    }

    @Override
    public int getLayoutId() {
        return R.layout.fragment_app_sleep;
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
        return getString(R.string.sleep_list);
    }
}