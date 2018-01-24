package com.openthos.greenify.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.openthos.greenify.R;
import com.openthos.greenify.app.Constants;
import com.openthos.greenify.bean.AppInfo;
import com.openthos.greenify.listener.OnListClickListener;
import com.openthos.greenify.utils.ToolUtils;

import java.util.List;

public class AppListAdapter extends BasicAdapter {
    private List<AppInfo> mDatas;
    private OnListClickListener mOnListClickListener;

    public AppListAdapter(Context context, List<AppInfo> datas) {
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.app_list_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = mDatas.get(position);
        holder.icon.setImageDrawable(appInfo.getIcon());
        holder.name.setText(appInfo.getAppName());
        holder.cpu.setText(appInfo.getCpuUsage());
        holder.memory.setText(ToolUtils.transformFileSize(appInfo.getMemoryUsage(mContext)));
        holder.battery.setText(appInfo.getBatteryUsage());

        switch (appInfo.getDormantState()) {
            case Constants.APP_WAIT_DORMANT:
                holder.dormant.setVisibility(View.VISIBLE);
                holder.img1.setBackground(getDrawable(R.mipmap.o_remove));
                holder.img2.setBackground(getDrawable(R.mipmap.o_protect));
                break;
            case Constants.APP_HAVE_DORMANT:
                holder.dormant.setVisibility(View.GONE);
                holder.img1.setBackground(getDrawable(R.mipmap.o_remove));
                holder.img2.setBackground(getDrawable(R.mipmap.o_protect));
                break;
            case Constants.APP_NON_DORMANT:
                holder.dormant.setVisibility(View.GONE);
                holder.img1.setBackground(getDrawable(R.mipmap.o_remove));
                holder.img2.setBackground(getDrawable(R.mipmap.o_dormant));
                break;
            case Constants.App_NON_DEAL:
                holder.dormant.setVisibility(View.VISIBLE);
                holder.img1.setBackground(getDrawable(R.mipmap.o_dormant));
                holder.img2.setBackground(getDrawable(R.mipmap.o_protect));
                break;
        }
        holder.img1.setTag(appInfo.getPackageName());
        holder.img2.setTag(appInfo.getPackageName());
        holder.dormant.setTag(appInfo.getPackageName());
        holder.layout.setTag(appInfo.getPackageName());
        return convertView;
    }

    public Drawable getDrawable(int resId) {
        return mContext.getResources().getDrawable(resId);
    }

    public void setOnListClickListener(OnListClickListener onListClickListener) {
        mOnListClickListener = onListClickListener;
    }

    @Override
    public void refreshList() {
        notifyDataSetChanged();
    }

    private class ViewHolder implements View.OnClickListener, View.OnHoverListener {
        private LinearLayout layout;
        private ImageView icon;
        private TextView name;
        private TextView cpu;
        private TextView memory;
        private TextView battery;
        private ImageView img1;
        private ImageView img2;
        private ImageView dormant;
        ;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            icon = (ImageView) view.findViewById(R.id.app_icon);
            name = (TextView) view.findViewById(R.id.app_name);
            cpu = (TextView) view.findViewById(R.id.cpu_usage);
            memory = (TextView) view.findViewById(R.id.memory_usage);
            battery = (TextView) view.findViewById(R.id.battery_usage);
            img1 = (ImageView) view.findViewById(R.id.img1);
            img2 = (ImageView) view.findViewById(R.id.img2);
            dormant = (ImageView) view.findViewById(R.id.dormant);
            layout.setOnClickListener(this);
            img1.setOnClickListener(this);
            img2.setOnClickListener(this);
            dormant.setOnClickListener(this);
            layout.setOnHoverListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mOnListClickListener != null) {
                mOnListClickListener.onListClickListener(view, (String) view.getTag());
            }
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.hint_color));
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    break;
            }
            return false;
        }
    }
}
