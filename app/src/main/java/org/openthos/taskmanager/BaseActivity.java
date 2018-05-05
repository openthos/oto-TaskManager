package org.openthos.taskmanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import java.io.DataOutputStream;
import java.io.IOException;

public abstract class BaseActivity extends FragmentActivity {

    private static String ONE_OR_MORE_SPACE = "\\s+";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        initView();
        initData();
        initListener();
    }

    /**
     * Kill the application process
     *
     * @param pkgName
     */
    public void forceStopAPK(String pkgName) {
        Process sh = null;
        DataOutputStream os = null;
        try {
            sh = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(sh.getOutputStream());
            final String Command = "am force-stop " + pkgName + "\n";
            os.writeBytes(Command);
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();

    public abstract void initListener();

}