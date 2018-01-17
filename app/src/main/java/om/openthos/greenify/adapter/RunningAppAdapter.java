package om.openthos.greenify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import om.openthos.greenify.R;
import om.openthos.greenify.bean.AppInfo;
import om.openthos.greenify.bean.DialogType;
import om.openthos.greenify.dialog.MenuDialog;

public class RunningAppAdapter extends BasicAdapter {
    private List<AppInfo> mDatas;
    private View mLastView;

    public RunningAppAdapter(Context context, List<AppInfo> datas) {
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
                    .inflate(R.layout.item_running_app, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = mDatas.get(position);
        holder.appIcon.setImageDrawable(appInfo.getIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.cpuUsage.setText(appInfo.getCpuUsage() + "%");
        holder.memoryUsage.setText(String.valueOf(appInfo.getMemoryUsage()));
        holder.batteryUsage.setText(appInfo.getBatteryUsage());
        holder.pid.setText(String.valueOf(appInfo.getPid()));
        holder.layout.setTag(position);
        return convertView;
    }

    @Override
    public void refreshList() {
        notifyDataSetChanged();
    }

    private class ViewHolder implements View.OnTouchListener, View.OnHoverListener {
        private LinearLayout layout;
        private ImageView appIcon;
        private TextView appName;
        private TextView cpuUsage;
        private TextView memoryUsage;
        private TextView batteryUsage;
        private TextView pid;

        public ViewHolder(View view) {
            layout = (LinearLayout) view.findViewById(R.id.layout);
            appIcon = (ImageView) view.findViewById(R.id.app_icon);
            appName = (TextView) view.findViewById(R.id.app_name);
            cpuUsage = (TextView) view.findViewById(R.id.cpu_usage);
            memoryUsage = (TextView) view.findViewById(R.id.memory_usage);
            batteryUsage = (TextView) view.findViewById(R.id.battery_usage);
            pid = (TextView) view.findViewById(R.id.pid);
            layout.setOnTouchListener(this);
            layout.setOnHoverListener(this);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (mLastView != null && mLastView != v) {
                mLastView.setSelected(false);
            }
            switch (event.getButtonState()) {
                case MotionEvent.BUTTON_PRIMARY:
                    v.setSelected(true);
                    break;
                case MotionEvent.BUTTON_SECONDARY:
                    v.setSelected(true);
                    new MenuDialog(mContext).show(DialogType.RUNNING_RIGHT,
                            mDatas.get((Integer) v.getTag()),
                            (int) event.getRawX(), (int) event.getRawY());
                    break;
            }
            return true;
        }

        @Override
        public boolean onHover(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_HOVER_ENTER:
                    v.setSelected(true);
                    break;
                case MotionEvent.ACTION_HOVER_EXIT:
                    v.setSelected(false);
                    break;
            }
            return false;
        }
    }
}
