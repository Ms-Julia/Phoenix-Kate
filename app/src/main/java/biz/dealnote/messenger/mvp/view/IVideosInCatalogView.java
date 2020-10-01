package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IVideosInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Video> videos);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
