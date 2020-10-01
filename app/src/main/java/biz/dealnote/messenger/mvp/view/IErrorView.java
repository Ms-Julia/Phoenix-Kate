package biz.dealnote.messenger.mvp.view;

import androidx.annotation.StringRes;

import biz.dealnote.messenger.util.PhoenixToast;


public interface IErrorView {
    void showError(String errorText);

    void showThrowable(Throwable throwable);

    void showError(@StringRes int titleTes, Object... params);

    PhoenixToast getPhoenixToast();
}
