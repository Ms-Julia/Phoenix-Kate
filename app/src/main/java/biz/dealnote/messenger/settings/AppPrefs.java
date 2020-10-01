package biz.dealnote.messenger.settings;

import android.content.Context;
import android.content.pm.PackageManager;

public class AppPrefs {

    public static boolean isCoubInstalled(Context context) {
        return isPackageIntalled(context, "com.coub.android");
    }

    public static boolean isYoutubeInstalled(Context context) {
        return isPackageIntalled(context, "com.google.android.youtube");
    }

    private static boolean isPackageIntalled(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(name, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException ignored) {
            return false;
        }
    }
}
