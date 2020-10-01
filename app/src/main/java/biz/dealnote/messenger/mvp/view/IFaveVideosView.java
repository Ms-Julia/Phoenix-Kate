package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IFaveVideosView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Video> videos);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void goToPreview(int accountId, Video video);
}
