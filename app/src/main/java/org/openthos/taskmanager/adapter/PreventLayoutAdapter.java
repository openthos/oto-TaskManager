package org.openthos.taskmanager.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.openthos.taskmanager.R;
import org.openthos.taskmanager.bean.AppLayoutInfo;
import org.openthos.taskmanager.listener.OnListClickListener;
import org.openthos.taskmanager.view.CustomListView;

import java.util.ArrayList;
import java.util.List;

public class PreventLayoutAdapter extends BasicAdapter {
    private List<AppLayoutInfo> mDatas;
    private OnListClickListener mOnListClickListener;
    private View.OnHoverListener mOnHoverListener;
    private boolean mIsRefresh;

    public PreventLayoutAdapter(Context context) {
        super(context);
        mDatas = new ArrayList<>();
        mIsRefresh = true;
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
                    .inflate(R.layout.item_prevent_list_layout, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppLayoutInfo appLayoutInfo = mDatas.get(position);
        holder.type.setText(appLayoutInfo.getType());
        PreventAdapter adapter = new PreventAdapter(mContext);
        holder.listView.setAdapter(adapter);
        adapter.refreshList(appLayoutInfo.getAppInfos());
        adapter.setOnListClickListener(mOnListClickListener);
        adapter.setOnHoverListener(mOnHoverListener);
        return convertView;
    }

    public void setOnListClickListener(OnListClickListener onListClickListener) {
        mOnListClickListener = onListClickListener;
    }

    public void setOnHoverListener(View.OnHoverListener onHoverListener) {
        mOnHoverListener = onHoverListener;
    }

    public void refreshList(List<AppLayoutInfo> datas) {
        if (mIsRefresh) {
            mIsRefresh = false;
            mDatas.clear();
            if (datas != null) {
                mDatas.addAll(datas);
            }
            notifyDataSetChanged();
            mIsRefresh = true;
        }
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
