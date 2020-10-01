package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IAudiosView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Audio> audios);

    void notifyListChanged();

    void notifyItemRemoved(int index);

    void notifyDataAdded(int position, int count);

    void notifyItemChanged(int index);

    void displayRefreshing(boolean refresing);

    void updatePlaylists(List<AudioPlaylist> stories);
}