package org.openthos.taskmanager.view;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HoverTextView extends TextView {
    private int[] mParentPosition;
    private View mParentView;


    public HoverTextView(Context context) {
        this(context, null);
    }

    public HoverTextView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HoverTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mParentPosition = new int[2];
    }

    public void setParentView(View view) {
        mParentView = view;
    }

    public void show(View view, String text) {
        dismiss();
        setText(text);

        int[] location = new int[2];
        view.getLocationInWindow(location);
        mParentView.getLocationInWindow(mParentPosition);
        mParentView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(getLayoutParams());
        params.leftMargin = location[0] + view.getMeasuredWidth() / 2 - getMeasuredWidth() / 2 - mParentPosition[0];
        params.topMargin = location[1] + view.getMeasuredHeight() - mParentPosition[1];
//        if (params.leftMargin < 0) {
//            params.leftMargin = 0;
//        } else if (params.leftMargin > mParentView.getMeasuredWidth() - getMeasuredWidth()) {
//            params.leftMargin = mParentView.getMeasuredWidth() - getMeasuredWidth();
//        }

        Log.i("ljh", "---------------------------------");
        Log.i("ljh", "location " + location[0] + " " + location[1]);
        Log.i("ljh", "mParentPosition " + mParentPosition[0] + " " + mParentPosition[1]);
        Log.i("ljh", "view " + view.getMeasuredWidth() + " " + view.getMeasuredHeight());
        Log.i("ljh", "mParentView " + mParentView.getMeasuredWidth() + " " + mParentView.getMeasuredHeight());
        Log.i("ljh", "leftMargin " + params.leftMargin);
        Log.i("ljh", "this " + getMeasuredWidth() + " " + getMeasuredHeight());

        params.gravity = Gravity.LEFT | Gravity.TOP;
        setLayoutParams(params);
        setVisibility(VISIBLE);
    }

    public void dismiss() {
        if (isVisibility()) {
            setVisibility(GONE);
        }
    }

    public boolean isVisibility() {
        return getVisibility() == VISIBLE;
    }
}