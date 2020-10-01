package biz.dealnote.messenger.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.fragment.app.FragmentActivity;

import biz.dealnote.messenger.util.MainActivityTransforms;
import biz.dealnote.messenger.util.ViewUtils;

/**
 * Тот же MainActivity, предназначенный для шаринга контента
 * Отличие только в том, что этот активити может существовать в нескольких экземплярах
 */
public class SwipebleActivity extends MainActivity {

    public static void start(FragmentActivity context, Intent intent, int code) {
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
        context.startActivityForResult(intent, code);
    }

    public static void start(Context context, Intent intent) {
        intent.putExtra(MainActivity.EXTRA_NO_REQUIRE_PIN, true);
        context.startActivity(intent);
    }

    @Override
    protected @MainActivityTransforms
    int getMainActivityTransform() {
        return MainActivityTransforms.SWIPEBLE;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_SWIPE_TO_DISMISS);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
        super.onCreate(savedInstanceState);
        // потому, что в onBackPressed к этому числу будут прибавлять 2000 !!!! и выход за границы
        mLastBackPressedTime = Long.MAX_VALUE - DOUBLE_BACK_PRESSED_TIMEOUT;
    }

    @Override
    public void onDestroy() {
        ViewUtils.keyboardHide(this);
        super.onDestroy();
    }
}
