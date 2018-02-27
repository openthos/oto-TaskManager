package com.openthos.taskmanager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.openthos.taskmanager.MainActivity;
import com.openthos.taskmanager.R;
import com.openthos.taskmanager.adapter.MenuAdapter;
import com.openthos.taskmanager.bean.DialogType;

public class BaseDialog extends Dialog{
    private static BaseDialog mBaseDialog;
    private MainActivity mActivity;
    private ListView mMenuList;
    private MenuAdapter mAdapter;
    private List<String> mDatas;

    public static BaseDialog getInstance(Context context) {
        if (mBaseDialog == null) {
            mBaseDialog = new BaseDialog(context);
        }
        return mBaseDialog;
    }

    public BaseDialog(@NonNull Context context) {
        super(context, R.style.MenuDialogStyle);
        mActivity = (MainActivity) context;
        create();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu_dialog);
        initView();
        initData();
        initListener();
    }

    private void initView() {
        mMenuList = (ListView) findViewById(R.id.menu_list);
    }

    private void initData() {
        mDatas = new ArrayList<>();
        mAdapter = new MenuAdapter(getContext(), mDatas);
        mMenuList.setAdapter(mAdapter);
    }

    private void initListener() {
    }

    public void show(DialogType type, int x, int y) {
        refreshList(type);

        Window dialogWindow = getWindow();
        dialogWindow.setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        dialogWindow.setGravity(Gravity.LEFT | Gravity.TOP);
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.format = PixelFormat.TRANSPARENT;
        lp.dimAmount = 0f;
        lp.x = x;
        lp.y = y;
        dialogWindow.setAttributes(lp);
        show();
    }

    private void refreshList(DialogType dialogType) {
        mDatas.clear();
        String[] sArr = null;
        switch (dialogType) {
            case RUNNING_RIGHT:
                sArr = getContext().getResources().getStringArray(R.array.running_right);
                break;
        }
        mDatas.addAll(Arrays.asList(sArr));
        mAdapter.notifyDataSetChanged();
        int maxWidth = 0;
        int height = 0;
        for (int i = 0; i < mAdapter.getCount(); i++) {
            View views = mAdapter.getView(i, null, null);
            views.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            maxWidth = Math.max(views.getMeasuredWidth(), maxWidth);
            height = height + views.getMeasuredHeight();
        }

        mMenuList.setLayoutParams(new LinearLayout.LayoutParams(maxWidth, height));
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }
}
