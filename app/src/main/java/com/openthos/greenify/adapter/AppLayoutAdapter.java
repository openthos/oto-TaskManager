package com.openthos.greenify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.openthos.greenify.R;
import com.openthos.greenify.bean.AppLayoutInfo;
import com.openthos.greenify.listener.OnListClickListener;
import com.openthos.greenify.view.CustomListView;

import java.util.List;

public class AppLayoutAdapter extends BasicAdapter {
    private List<AppLayoutInfo> mDatas;
    private OnListClickListener mOnListClickListener;

    public AppLayoutAdapter(Context context, List<AppLayoutInfo> datas) {
        super(context);
        mDatas = datas;
    }

    @Override
    public int getCount() {
        return mDatas != null ? mDatas.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mDatas != null ? mDatas.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext)
                    .inflate(R.layout.app_layout_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppLayoutInfo appLayoutInfo = mDatas.get(position);
        holder.type.setText(appLayoutInfo.getType());
        AppListAdapter adapter = new AppListAdapter(mContext, appLayoutInfo.getAppInfos());
        holder.listView.setAdapter(adapter);
        adapter.refreshList();
        adapter.setOnListClickListener(mOnListClickListener);
        return convertView;
    }

    public void setOnListClickListener(OnListClickListener onListClickListener) {
        mOnListClickListener = onListClickListener;
    }

    @Override
    public void refreshList() {
        notifyDataSetChanged();
    }

    private class ViewHolder {
        private TextView type;
        private CustomListView listView;

        public ViewHolder(View view) {
            type = (TextView) view.findViewById(R.id.type);
            listView = (CustomListView) view.findViewById(R.id.listview);
        }
    }
}
