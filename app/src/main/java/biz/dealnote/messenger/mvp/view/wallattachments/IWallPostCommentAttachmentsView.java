package biz.dealnote.messenger.mvp.view.wallattachments;

import java.util.List;

import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IWallPostCommentAttachmentsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Post> posts);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void onSetLoadingStatus(int isLoad);

    void openPostEditor(int accountId, Post post);
}
