package org.openthos.taskmanager.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.openthos.taskmanager.R;

public class ConfirmDialog extends Dialog implements View.OnClickListener {
    private static ConfirmDialog mConfirmDialog;
    private OnConfirmListener mConfirmListener;
    private TextView mTitle;
    private Button mCancel;
    private Button mConfirm;

    public static ConfirmDialog getInstance(Context context) {
        if (mConfirmDialog == null) {
            mConfirmDialog = new ConfirmDialog(context);
        }
        return mConfirmDialog;
    }

    private ConfirmDialog(Context context) {
        super(context, R.style.DialogStyle);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);
        initView();
        initData();
        initListener();
    }

    public void initView() {
        mTitle = (TextView) findViewById(R.id.title);
        mCancel = (Button) findViewById(R.id.cancel);
        mConfirm = (Button) findViewById(R.id.confirm);
    }

    public void initData() {

    }

    public void initListener() {
        mCancel.setOnClickListener(this);
        mConfirm.setOnClickListener(this);
    }

    public void showDialog(String title,
                           String cancel, String confirm, OnConfirmListener dialogListener) {
        setCanceledOnTouchOutside(true);
        show();
        mConfirmListener = dialogListener;
        mTitle.setText(title);
        mCancel.setText(cancel);
        mConfirm.setText(confirm);
    }

    public void showDialog(boolean outCancel, String title,
                           String cancel, String confirm, OnConfirmListener dialogListener) {
        showDialog(title, cancel, confirm, dialogListener);
        setCanceledOnTouchOutside(outCancel);
    }

    public void showDialog(String title, OnConfirmListener dialogListener) {
        setCanceledOnTouchOutside(true);
        show();
        mConfirmListener = dialogListener;
        mTitle.setText(title);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.cancel) {
            if (mConfirmListener != null) {
                mConfirmListener.cancel(mConfirmDialog);
            }

        } else if (i == R.id.confirm) {
            if (mConfirmListener != null) {
                mConfirmListener.confirm(mConfirmDialog);
            }

        }
    }

    public interface OnConfirmListener {
        void cancel(Dialog dialog);

        void confirm(Dialog dialog);
    }
}