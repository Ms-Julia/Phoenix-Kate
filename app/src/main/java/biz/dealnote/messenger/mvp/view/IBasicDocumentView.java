package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;

import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IBasicDocumentView extends IMvpView, IAccountDependencyView, IToastView, IErrorView {

    void shareDocument(int accountId, @NonNull Document document);

    void requestWriteExternalStoragePermission();

}
