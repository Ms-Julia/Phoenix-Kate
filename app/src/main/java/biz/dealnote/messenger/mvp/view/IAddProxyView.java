package biz.dealnote.messenger.mvp.view;

import biz.dealnote.mvp.core.IMvpView;


public interface IAddProxyView extends IMvpView, IErrorView {
    void setAuthFieldsEnabled(boolean enabled);

    void setAuthChecked(boolean checked);

    void goBack();
}
