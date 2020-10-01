package biz.dealnote.messenger.fragment.base;

import android.graphics.Color;
import android.widget.Toast;

import androidx.annotation.StringRes;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.IToastView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.compat.AbsMvpDialogFragment;
import biz.dealnote.mvp.core.AbsPresenter;
import biz.dealnote.mvp.core.IMvpView;

public abstract class BaseMvpDialogFragment<P extends AbsPresenter<V>, V extends IMvpView>
        extends AbsMvpDialogFragment<P, V> implements IMvpView, IAccountDependencyView, IErrorView, IToastView {

    @Override
    public void showToast(@StringRes int titleTes, boolean isLong, Object... params) {
        if (isAdded()) {
            Toast.makeText(requireActivity(), getString(titleTes), isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showError(String text) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), text);
        }
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
    public void displayAccountNotSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public void displayAccountSupported() {
        // TODO: 18.12.2017
    }

    @Override
    public PhoenixToast getPhoenixToast() {
        if (isAdded()) {
            return PhoenixToast.CreatePhoenixToast(requireActivity());
        }
        return PhoenixToast.CreatePhoenixToast(null);
    }
}
