package org.openthos.taskmanager.adapter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.openthos.taskmanager.R;
import org.openthos.taskmanager.app.Constants;
import org.openthos.taskmanager.bean.AppInfo;
import org.openthos.taskmanager.listener.OnListClickListener;
import org.openthos.taskmanager.utils.ToolUtils;

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
        holder.cpu.setText(appInfo.isRun() ? appInfo.getCpuUsage() : "");
        holder.memory.setText(appInfo.getMemoryUsage());

        switch (appInfo.getDormantState()) {
            case Constants.APP_NON_DORMANT:
                holder.add.setText(mContext.getString(R.string.non_dormant));
                break;
            case Constants.App_NON_DEAL:
                holder.add.setText(mContext.getString(R.string.wait_dormant));
                break;
        }
        holder.prevent.setText(appInfo.isAutoPrevent()
                ? mContext.getString(R.string.prevent)
                : mContext.getString(R.string.remove));
        holder.dormant.setTag(appInfo.getPackageName());
        holder.add.setTag(appInfo.getPackageName());
        holder.prevent.setTag(appInfo.getPackageName());
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
        private TextView add;
        private TextView dormant;
        private TextView prevent;
        ;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            icon = (ImageView) view.findViewById(R.id.app_icon);
            name = (TextView) view.findViewById(R.id.app_name);
            cpu = (TextView) view.findViewById(R.id.cpu_usage);
            memory = (TextView) view.findViewById(R.id.memory_usage);
            battery = (TextView) view.findViewById(R.id.battery_usage);
            add = (TextView) view.findViewById(R.id.add_or_remove);
            dormant = (TextView) view.findViewById(R.id.dormant);
            prevent = (TextView) view.findViewById(R.id.prevent);
            layout.setOnClickListener(this);
            add.setOnClickListener(this);
            dormant.setOnClickListener(this);
            prevent.setOnClickListener(this);
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
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.theme));
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.white));
                    break;
            }
            return false;
        }
    }
}
