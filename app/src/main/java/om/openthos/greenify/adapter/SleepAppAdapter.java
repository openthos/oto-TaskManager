package om.openthos.greenify.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import om.openthos.greenify.MainActivity;
import om.openthos.greenify.R;
import om.openthos.greenify.bean.AppInfo;

public class SleepAppAdapter extends BasicAdapter {
    private List<AppInfo> mDatas;

    public SleepAppAdapter(Context context, List<AppInfo> datas) {
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
                    .inflate(R.layout.item_sleep_app, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AppInfo appInfo = mDatas.get(position);
        holder.appIcon.setImageDrawable(appInfo.getIcon());
        holder.appName.setText(appInfo.getAppName());
        holder.state.setText(appInfo.isRun() ? R.string.running : R.string.stop_run);
        holder.removeListText.setVisibility(View.GONE);
        holder.removeListIcon.setImageResource(R.mipmap.decrease);
        holder.removeListIcon.setTag(position);
        return convertView;
    }

    @Override
    public void refreshList() {
        notifyDataSetChanged();
    }

    private class ViewHolder implements View.OnClickListener {
        private TextView appName;
        private TextView state;
        private TextView removeListText;
        private ImageView removeListIcon;
        private ImageView appIcon;

        public ViewHolder(View view) {
            appName = (TextView) view.findViewById(R.id.app_name);
            state = (TextView) view.findViewById(R.id.state);
            removeListText = (TextView) view.findViewById(R.id.remove_sleep_text);
            removeListIcon = (ImageView) view.findViewById(R.id.remove_sleep_icon);
            appIcon = (ImageView) view.findViewById(R.id.app_icon);
            removeListIcon.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int position = (int) v.getTag();
            AppInfo appInfo = mDatas.get(position);
            ((MainActivity) mContext).addSleepList(appInfo.getPackageName(), false);
            mDatas.remove(position);
            refreshList();
        }
    }
}
