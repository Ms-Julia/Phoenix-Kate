package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IPlaylistsInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<AudioPlaylist> audios);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
