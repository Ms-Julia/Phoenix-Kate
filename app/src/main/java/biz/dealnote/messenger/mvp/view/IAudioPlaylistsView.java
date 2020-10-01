package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IAudioPlaylistsView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AudioPlaylist> pages);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);
}
