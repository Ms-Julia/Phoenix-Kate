package biz.dealnote.messenger.mvp.view;

import androidx.annotation.StringRes;

import biz.dealnote.messenger.util.PhoenixToast;


public interface IToastView {
    void showToast(@StringRes int titleTes, boolean isLong, Object... params);

    PhoenixToast getPhoenixToast();
}
