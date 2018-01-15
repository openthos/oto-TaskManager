package om.openthos.greenify;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;


public abstract class BaseActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData();
        initListener();
    }

    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();

    public abstract void initListener();

}
