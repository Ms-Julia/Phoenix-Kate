package biz.dealnote.messenger.settings;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;

import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.squareup.picasso3.Transformation;

import java.io.File;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.picasso.transforms.ElipseTransformation;
import biz.dealnote.messenger.picasso.transforms.RoundTransformation;
import biz.dealnote.messenger.util.Utils;

public class CurrentTheme {

    private static final String KEY_CHAT_BACKGROUND = "chat_background";

    private static File getDrawerBackgroundFile(Context context, boolean light) {
        return new File(context.getFilesDir(), light ? "chat_light.jpg" : "chat_dark.jpg");
    }

    private static Drawable getStatic(Activity activity) {
        if (Settings.get().other().isCustom_chat_color()) {

            return new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                    new int[]{Settings.get().other().getColorChat(), Settings.get().other().getSecondColorChat()});
        }
        int color = getColorFromAttrs(activity, R.attr.messages_background_color, Color.WHITE);
        return new ColorDrawable(color);
    }

    public static Drawable getChatBackground(Activity activity) {
        boolean dark = Settings.get().ui().isDarkModeEnabled(activity);
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
        String page = preferences.getString(KEY_CHAT_BACKGROUND, "1");
        assert page != null;
        Drawable ret;
        switch (page) {
            case "1":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_cookies);
                break;
            case "2":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_lines);
                break;
            case "3":
                ret = getDrawableFromAttribute(activity, R.attr.chat_background_runes);
                break;
            case "4":
                File bitmap = getDrawerBackgroundFile(activity, !dark);
                if (bitmap.exists()) {
                    Drawable d = Drawable.createFromPath(bitmap.getAbsolutePath());
                    if (Settings.get().other().isCustom_chat_color())
                        Utils.setColorFilter(d, Settings.get().other().getColorChat());
                    return d;
                }
                return getStatic(activity);
            default: //"0
                return getStatic(activity);
        }
        if (Settings.get().other().isCustom_chat_color()) {
            Drawable r1 = ret.mutate();
            Utils.setColorFilter(r1, Settings.get().other().getColorChat());
            return r1;
        }
        return ret;
    }

    public static Transformation createTransformationForAvatar(Context context) {
        int style = Settings.get()
                .ui()
                .getAvatarStyle();

        switch (style) {
            case AvatarStyle.OVAL:
                return new ElipseTransformation();
            case AvatarStyle.CIRCLE:
            default:
                return new RoundTransformation();
        }
    }

    public static int getColorPrimary(Context context) {
        return getColorFromAttrs(androidx.appcompat.R.attr.colorPrimary, context, "#000000");
    }

    public static int getColorToast(Context context) {
        return getColorFromAttrs(R.attr.toast_background, context, "#FFFFFF");
    }

    public static int getColorOnPrimary(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnPrimary, context, "#000000");
    }

    public static int getColorSurface(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorSurface, context, "#000000");
    }

    public static int getColorOnSurface(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnSurface, context, "#000000");
    }

    public static int getColorBackground(Context context) {
        return getColorFromAttrs(android.R.attr.colorBackground, context, "#000000");
    }

    public static int getColorOnBackground(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorOnBackground, context, "#000000");
    }

    public static int getStatusBarColor(Context context) {
        return getColorFromAttrs(android.R.attr.statusBarColor, context, "#000000");
    }

    public static int getNavigationBarColor(Context context) {
        return getColorFromAttrs(android.R.attr.navigationBarColor, context, "#000000");
    }

    public static int getColorSecondary(Context context) {
        return getColorFromAttrs(com.google.android.material.R.attr.colorSecondary, context, "#000000");
    }

    public static int getStatusBarNonColored(Context context) {
        return getColorFromAttrs(R.attr.statusbarNonColoredColor, context, "#000000");
    }

    public static int getMessageUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.message_unread_color, context, "#ffffff");
    }

    public static int getMessageBackgroundSquare(Context context) {
        return getColorFromAttrs(R.attr.message_bubble_color, context, "#000000");
    }

    public static int getPrimaryTextColorCode(Context context) {
        return getColorFromSystemAttrs(android.R.attr.textColorPrimary, context);
    }

    public static int getSecondaryTextColorCode(Context context) {
        return getColorFromSystemAttrs(android.R.attr.textColorSecondary, context);
    }

    public static int getDialogsUnreadColor(Context context) {
        return getColorFromAttrs(R.attr.dialogs_unread_color, context, "#20b0b0b0");
    }

    public static int getMy_messages_bubble_color(Context context) {
        return getColorFromAttrs(R.attr.my_messages_bubble_color, context, "#20b0b0b0");
    }

    public static int getColorFromAttrs(int resId, Context context, String defaultColor) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(resId, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return a.data;
        } else {
            return Color.parseColor(defaultColor);
        }
    }

    public static int getColorFromAttrs(Context context, int resId, int defaultColor) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(resId, a, true);
        if (a.type >= TypedValue.TYPE_FIRST_COLOR_INT && a.type <= TypedValue.TYPE_LAST_COLOR_INT) {
            return a.data;
        } else {
            return defaultColor;
        }
    }

    public static int getColorFromSystemAttrs(int resId, Context context) {
        TypedValue a = new TypedValue();
        context.getTheme().resolveAttribute(resId, a, true);
        TypedArray arr = context.obtainStyledAttributes(a.data, new int[]{resId});
        int result = arr.getColor(0, -1);
        arr.recycle();
        return result;
    }

    public static int getResIdFromAttribute(Activity activity, int attr) {
        if (attr == 0) {
            return 0;
        }

        TypedValue typedvalueattr = new TypedValue();
        activity.getTheme().resolveAttribute(attr, typedvalueattr, true);
        return typedvalueattr.resourceId;
    }

    public static Drawable getDrawableFromAttribute(Activity activity, int attr) {
        int resId = getResIdFromAttribute(activity, attr);
        return ContextCompat.getDrawable(activity, resId);
    }

    private static String intToHexColor(int color) {
        return String.format("#%06X", (0xFFFFFF & color));
    }


}
