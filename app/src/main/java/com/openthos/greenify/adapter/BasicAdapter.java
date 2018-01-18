package com.openthos.greenify.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

public abstract class BasicAdapter extends BaseAdapter {
    public Context mContext;

    public BasicAdapter(Context context) {
        mContext = context;
    }

    public abstract void refreshList();

}