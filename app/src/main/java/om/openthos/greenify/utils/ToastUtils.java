package om.openthos.greenify.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    private static Toast mToast;

    public static void showToast(Context context, String content) {
        showToast(context, content, Toast.LENGTH_SHORT);
    }

    public static void showToast(Context context, String content, int duration) {
        if (mToast == null) {
            mToast = Toast.makeText(context, content, duration);
        } else {
            mToast.setText(content);
        }
        mToast.show();
    }
}
