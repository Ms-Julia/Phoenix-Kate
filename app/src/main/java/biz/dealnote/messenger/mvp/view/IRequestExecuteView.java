package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IRequestExecuteView extends IMvpView, IErrorView, IProgressView, IAccountDependencyView, IToastView {
    void displayBody(String body);

    void hideKeyboard();

    void requestWriteExternalStoragePermission();
}