package biz.dealnote.messenger.activity;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.listener.BackPressCallback;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;

public abstract class NoMainActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private final FragmentManager.OnBackStackChangedListener mBackStackListener = this::resolveToolbarNavigationIcon;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        setTheme(Settings.get().ui().getMainTheme());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_no_main);

        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        w.setStatusBarColor(CurrentTheme.getStatusBarColor(this));
        w.setNavigationBarColor(CurrentTheme.getNavigationBarColor(this));

        getSupportFragmentManager().addOnBackStackChangedListener(mBackStackListener);
    }

    @IdRes
    protected int getMainContainerViewId() {
        return R.id.fragment;
    }

    @Override
    public void setSupportActionBar(@Nullable Toolbar toolbar) {
        super.setSupportActionBar(toolbar);
        mToolbar = toolbar;
        resolveToolbarNavigationIcon();
    }

    private void resolveToolbarNavigationIcon() {
        if (Objects.isNull(mToolbar)) return;

        FragmentManager manager = getSupportFragmentManager();
        if (manager.getBackStackEntryCount() > 1) {
            Drawable tr = AppCompatResources.getDrawable(this, R.drawable.arrow_left);
            Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(this));
            mToolbar.setNavigationIcon(tr);
        } else {
            Drawable tr = AppCompatResources.getDrawable(this, R.drawable.close);
            Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(this));
            mToolbar.setNavigationIcon(tr);
        }

        mToolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        FragmentManager fm = getSupportFragmentManager();

        Fragment front = getSupportFragmentManager().findFragmentById(getMainContainerViewId());
        if (front instanceof BackPressCallback) {
            if (!(((BackPressCallback) front).onBackPressed())) {
                return;
            }
        }

        if (fm.getBackStackEntryCount() > 1) {
            super.onBackPressed();
        } else {
            supportFinishAfterTransition();
        }
    }

    @Override
    protected void onDestroy() {
        getSupportFragmentManager().removeOnBackStackChangedListener(mBackStackListener);
        super.onDestroy();
    }
}