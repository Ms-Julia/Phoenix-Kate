package biz.dealnote.messenger.mvp.view.wallattachments;

import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.mvp.view.IAttachmentsPlacesView;
import biz.dealnote.messenger.mvp.view.IErrorView;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IWallPhotosAttachmentsView extends IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    void displayData(List<Photo> photos);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void setToolbarTitle(String title);

    void setToolbarSubtitle(String subtitle);

    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);

    void onSetLoadingStatus(int isLoad);
}
