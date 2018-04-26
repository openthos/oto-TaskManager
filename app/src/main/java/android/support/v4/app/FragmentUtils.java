package android.support.v4.app;

public class FragmentUtils {

    private FragmentUtils() {

    }

    public static void setTag(Fragment fragment, String tag) {
        if (fragment != null) {
            fragment.mTag = tag;
        }
    }
}
