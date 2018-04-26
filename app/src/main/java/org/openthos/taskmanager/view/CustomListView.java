package org.openthos.taskmanager.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ListView;

import org.openthos.taskmanager.app.Constants;

public class CustomListView extends ListView {
    private boolean isDisableScroll = true;

    public CustomListView(Context context) {
        super(context);
    }

    public CustomListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CustomListView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (isDisableScroll) {
            int height = MeasureSpec.makeMeasureSpec(Integer.MAX_VALUE & Constants.HEIGHT_MASK,
                    MeasureSpec.AT_MOST);
            super.onMeasure(widthMeasureSpec, height);
        } else {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
    }

    public void setIsDisableScroll(boolean isDisableScroll) {
        this.isDisableScroll = isDisableScroll;
    }
}
