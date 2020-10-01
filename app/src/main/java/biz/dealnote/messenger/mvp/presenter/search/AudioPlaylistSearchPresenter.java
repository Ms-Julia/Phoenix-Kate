package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.criteria.AudioPlaylistSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.view.search.IAudioPlaylistSearchView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class AudioPlaylistSearchPresenter extends AbsSearchPresenter<IAudioPlaylistSearchView, AudioPlaylistSearchCriteria, AudioPlaylist, IntNextFrom> {

    private final IAudioInteractor audioInteractor;

    public AudioPlaylistSearchPresenter(int accountId, @Nullable AudioPlaylistSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    void onSeacrhError(Throwable throwable) {
        super.onSeacrhError(throwable);
        if (isGuiResumed()) {
            showError(getView(), Utils.getCauseIfRuntime(throwable));
        }
    }

    @Override
    Single<Pair<List<AudioPlaylist>, IntNextFrom>> doSearch(int accountId, AudioPlaylistSearchCriteria criteria, IntNextFrom startFrom) {
        IntNextFrom nextFrom = new IntNextFrom(startFrom.getOffset() + 50);
        return audioInteractor.searchPlaylists(accountId, criteria, startFrom.getOffset())
                .map(audio -> Pair.Companion.create(audio, nextFrom));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        appendDisposable(audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getPhoenixToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    @Override
    boolean canSearch(AudioPlaylistSearchCriteria criteria) {
        return true;
    }

    @Override
    AudioPlaylistSearchCriteria instantiateEmptyCriteria() {
        return new AudioPlaylistSearchCriteria("");
    }
}
