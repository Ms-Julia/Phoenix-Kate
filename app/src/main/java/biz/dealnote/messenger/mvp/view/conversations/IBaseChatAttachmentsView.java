package biz.dealnote.messenger.mvp.view.conversations;

import java.util.List;

import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IBaseChatAttachmentsView<T> extends IMvpView, IAccountDependencyView,
        IAttachmentsPlacesView, IErrorView {

    void displayAttachments(List<T> data);

    void notifyDataAdded(int position, int count);

    void notifyDatasetChanged();

    void showLoading(boolean loading);

    void setEmptyTextVisible(boolean visible);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);
}
