package biz.dealnote.messenger;

import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

import biz.dealnote.messenger.db.column.GroupColumns;
import biz.dealnote.messenger.db.column.UserColumns;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Utils;

public class Constants {
    public static final String API_VERSION = "5.124";
    public static final int DATABASE_VERSION = 202;

    public static final String AUTH_VERSION = BuildConfig.AUTH_VERSION;
    public static final int VERSION_APK = BuildConfig.VERSION_CODE;
    public static final String APK_ID = BuildConfig.APPLICATION_ID;

    public static final boolean IS_HAS_LOGIN_WEB = false;

    public static final String FILE_PROVIDER_AUTHORITY = BuildConfig.APPLICATION_ID + ".fileprovider";

    //public static final String DEVICE_COUNTRY_CODE = Injection.provideApplicationContext().getResources().getConfiguration().locale.getCountry().toLowerCase();
    public static final String DEVICE_COUNTRY_CODE = "ru";

    public static final String KATE_APP_VERSION_CODE = "477";
    public static final String KATE_APP_VERSION_NAME = "65.1 lite";

    public static final String KATE_USER_AGENT = String.format(Locale.US, "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", KATE_APP_VERSION_NAME, KATE_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, Build.SUPPORTED_ABIS[0], Utils.getDeviceName(), DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());
    public static final String KATE_USER_AGENT_FAKE = String.format(Locale.US, "KateMobileAndroid/%s-%s (Android %s; SDK %d; %s; %s; %s; %s)", KATE_APP_VERSION_NAME, KATE_APP_VERSION_CODE, Build.VERSION.RELEASE, Build.VERSION.SDK_INT, BuildConfig.FAKE_ABI, BuildConfig.FAKE_DEVICE, DEVICE_COUNTRY_CODE, SCREEN_RESOLUTION());

    public static final int API_ID = BuildConfig.VK_API_APP_ID;
    public static final String SECRET = BuildConfig.VK_CLIENT_SECRET;
    public static final String MAIN_OWNER_FIELDS = UserColumns.API_FIELDS + "," + GroupColumns.API_FIELDS;
    public static final String SERVICE_TOKEN = BuildConfig.SERVICE_TOKEN;
    public static final String PHOTOS_PATH = "DCIM/Phoenix";
    public static final int PIN_DIGITS_COUNT = 4;
    public static final String PICASSO_TAG = "picasso_tag";
    public static final boolean IS_DEBUG = BuildConfig.DEBUG;

    private static String SCREEN_RESOLUTION() {
        DisplayMetrics metrics = Resources.getSystem().getDisplayMetrics();
        if (metrics == null) {
            return "1920x1080";
        }
        return metrics.heightPixels + "x" + metrics.widthPixels;
    }

    @NotNull
    private static String getTypedUserAgent(@Account_Types int type) {
        switch (type) {
            case Account_Types.BY_TYPE:
            case Account_Types.KATE:
                return KATE_USER_AGENT;
            case Account_Types.KATE_HIDDEN:
                return KATE_USER_AGENT_FAKE;
        }
        return KATE_USER_AGENT;
    }

    @NotNull
    public static String USER_AGENT(@Account_Types int type) {
        if (type != Account_Types.BY_TYPE) {
            return getTypedUserAgent(type);
        }
        int account_id = Settings.get().accounts().getCurrent();
        if (account_id == ISettings.IAccountsSettings.INVALID_ID) {
            return KATE_USER_AGENT;
        }
        return getTypedUserAgent(Settings.get().accounts().getType(account_id));
    }
}
