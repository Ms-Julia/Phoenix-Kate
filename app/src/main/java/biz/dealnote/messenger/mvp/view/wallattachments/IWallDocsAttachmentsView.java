package biz.dealnote.messenger.mvp.view.wallattachments;

import java.util.List;

import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IWallDocsAttachmentsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Document> documents);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void onSetLoadingStatus(int isLoad);
}
