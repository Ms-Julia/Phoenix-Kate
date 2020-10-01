package biz.dealnote.messenger.fragment.base;

import android.app.AlertDialog;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.IProgressView;
import biz.dealnote.messenger.mvp.view.IToastView;
import biz.dealnote.messenger.mvp.view.IToolbarView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.spots.SpotsDialog;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.compat.AbsMvpBottomSheetDialogFragment;
import biz.dealnote.mvp.core.AbsPresenter;
import biz.dealnote.mvp.core.IMvpView;

import static biz.dealnote.messenger.util.Objects.nonNull;

public abstract class BaseMvpBottomSheetDialogFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpBottomSheetDialogFragment<P, V> implements IMvpView, IAccountDependencyView, IProgressView, IErrorView, IToastView, IToolbarView {

    public static final String EXTRA_HIDE_TOOLBAR = "extra_hide_toolbar";
    private AlertDialog mLoadingProgressDialog;

    protected static void safelySetCheched(CompoundButton button, boolean checked) {
        if (nonNull(button)) {
            button.setChecked(checked);
        }
    }

    protected static void safelySetText(TextView target, String text) {
        if (nonNull(target)) {
            target.setText(text);
        }
    }

    protected static void safelySetText(TextView target, @StringRes int text) {
        if (nonNull(target)) {
            target.setText(text);
        }
    }

    protected static void safelySetVisibleOrGone(View target, boolean visible) {
        if (nonNull(target)) {
            target.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    protected boolean hasHideToolbarExtra() {
        return nonNull(getArguments()) && getArguments().getBoolean(EXTRA_HIDE_TOOLBAR);
    }

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if (isAdded()) {
            Toast.makeText(requireActivity(), getString(titleTes, params), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), text);
        }
    }

    @Override
    public PhoenixToast getPhoenixToast() {
        if (isAdded()) {
            return PhoenixToast.CreatePhoenixToast(requireActivity());
        }
        return PhoenixToast.CreatePhoenixToast(null);
    }

    @Override
    public void showError(@StringRes int titleTes, Object... params) {
        if (isAdded()) {
            showError(getString(titleTes, params));
        }
    }

    @Override
    public void showThrowable(Throwable throwable) {
        if (isAdded()) {
            Snackbar.make(requireView(), ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info, v -> {
                        StringBuilder Text = new StringBuilder();
                        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                            Text.append("    ");
                            Text.append(stackTraceElement);
                            Text.append("\r\n");
                        }
                        MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(requireActivity());
                        dlgAlert.setIcon(R.drawable.crash_ic_error);
                        dlgAlert.setMessage(Text);
                        dlgAlert.setTitle(R.string.more_info);

                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    }).setActionTextColor(Color.WHITE).show();
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActivityUtils.setToolbarSubtitle(this, subtitle);
    }

    @Override
    public void setToolbarTitle(String title) {
        ActivityUtils.setToolbarTitle(this, title);
    }

    @Override
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    protected void styleSwipeRefreshLayoutWithCurrentTheme(@NonNull SwipeRefreshLayout swipeRefreshLayout, boolean needToolbarOffset) {
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), swipeRefreshLayout, needToolbarOffset);
    }

    @Override
    public void displayProgressDialog(@StringRes int title, @StringRes int message, boolean cancelable) {
        dismissProgressDialog();

        mLoadingProgressDialog = new SpotsDialog.Builder().setContext(requireActivity()).setMessage(getString(title) + ": " + getString(message)).setCancelable(cancelable).build();
        mLoadingProgressDialog.show();
    }

    @Override
    public void dismissProgressDialog() {
        if (nonNull(mLoadingProgressDialog)) {
            if (mLoadingProgressDialog.isShowing()) {
                mLoadingProgressDialog.cancel();
            }
        }
    }
}
