package org.openthos.taskmanager.adapter;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
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
import org.openthos.taskmanager.piebridge.prevent.common.PackageUtils;
import org.openthos.taskmanager.piebridge.prevent.ui.PreventActivity;
import org.openthos.taskmanager.piebridge.prevent.ui.util.StatusUtils;
import org.openthos.taskmanager.utils.ToolUtils;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

public class AppAdapter extends BasicAdapter {
    private List<AppInfo> mDatas;
    private PreventActivity mActivity;
    private OnListClickListener mOnListClickListener;
    private final PackageManager mPm;

    public AppAdapter(Context context, List<AppInfo> datas) {
        super(context);
        mDatas = datas;
        mActivity = (PreventActivity) context;
        mPm = mActivity.getPackageManager();
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
            convertView = LayoutInflater.from(mContext).inflate(R.layout.prevent_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = mDatas.get(position);
        holder.nameView.setText(appInfo.getAppName());
        holder.cpuView.setText(appInfo.getCpuUsage());
        holder.memoryView.setText(appInfo.getMemoryUsage());

        holder.prevent.setImageResource(appInfo.isAutoPrevent()
                ? R.mipmap.ic_menu_block
                : R.mipmap.ic_menu_prevent);

        holder.addView.setImageResource(appInfo.isNonDormant()
                ? R.mipmap.o_protect
                : R.mipmap.add_black);
        holder.dormantView.setTag(appInfo.getPackageName());
        holder.addView.setTag(appInfo.getPackageName());
        holder.prevent.setTag(appInfo.getPackageName());
        holder.layout.setTag(appInfo.getPackageName());

        if (appInfo.isSystem()) {
            convertView.setBackgroundColor(mActivity.getDangerousColor());
        } else {
            convertView.setBackgroundColor(mActivity.getTransparentColor());
        }
        holder.canUninstall = ((appInfo.getFlags() & ApplicationInfo.FLAG_SYSTEM) == 0)
                || ((appInfo.getFlags() & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0);
        if (appInfo.getIcon() == null) {
            holder.iconView.setImageDrawable(mPm.getDefaultActivityIcon());
        } else {
            holder.iconView.setImageDrawable(appInfo.getIcon());
        }
        holder.summaryView.setText(appInfo.getRunDescribe());

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

        Collections.sort(mDatas, new Comparator<AppInfo>() {
            @Override
            public int compare(AppInfo o1, AppInfo o2) {
                return o1.toString().compareTo(o2.toString());
            }

            @Override
            public boolean equals(Object obj) {
                return super.equals(obj);
            }

            @Override
            public int hashCode() {
                return toString().hashCode();
            }
        });
        notifyDataSetChanged();
    }

    public class ViewHolder implements View.OnClickListener, View.OnHoverListener {
        private LinearLayout layout;
        private ImageView iconView;
        private TextView cpuView;
        private TextView memoryView;
        private ImageView addView;
        private ImageView dormantView;
        private ImageView prevent;

        public TextView nameView;
        public TextView summaryView;
        public TextView loadingView;
        public ImageView preventView;

        //        public String packageName;
        public Drawable icon;
        public Set<Long> running;
        public boolean canUninstall;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            iconView = (ImageView) view.findViewById(R.id.icon);
            nameView = (TextView) view.findViewById(R.id.name);
            summaryView = (TextView) view.findViewById(R.id.summary);
            loadingView = (TextView) view.findViewById(R.id.loading);
            preventView = (ImageView) view.findViewById(R.id.item_prevent);
            cpuView = (TextView) view.findViewById(R.id.cpu_usage);
            memoryView = (TextView) view.findViewById(R.id.memory_usage);
            addView = (ImageView) view.findViewById(R.id.add_or_remove);
            dormantView = (ImageView) view.findViewById(R.id.dormant);
            prevent = (ImageView) view.findViewById(R.id.prevent);

            layout.setOnClickListener(this);
            addView.setOnClickListener(this);
            dormantView.setOnClickListener(this);
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
                    v.setBackgroundColor(mContext.getResources().getColor(R.color.transparent));
                    break;
            }
            return false;
        }
    }
}
