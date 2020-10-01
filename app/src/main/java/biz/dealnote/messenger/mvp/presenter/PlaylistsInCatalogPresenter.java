package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.model.CatalogBlock;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IPlaylistsInCatalogView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class PlaylistsInCatalogPresenter extends AccountDependencyPresenter<IPlaylistsInCatalogView> {

    private final IAudioInteractor audioInteractor;
    private final ArrayList<AudioPlaylist> audios;
    private final String block_id;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private boolean actualReceived;
    private String next_from;
    private boolean loadingNow;
    private boolean endOfContent;

    public PlaylistsInCatalogPresenter(int accountId, String block_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
        audios = new ArrayList<>();
        this.block_id = block_id;
    }

    public void LoadAudiosTool() {
        fireRefresh();
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    public void requestList() {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.getCatalogBlockById(getAccountId(), block_id, next_from)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onListReceived, this::onListGetError));
    }

    private void onListReceived(CatalogBlock data) {
        if (data == null || Utils.isEmpty(data.getPlaylists())) {
            actualReceived = true;
            setLoadingNow(false);
            endOfContent = true;
            return;
        }
        if (Utils.isEmpty(next_from)) {
            audios.clear();
        }
        next_from = data.getNext_from();
        endOfContent = Utils.isEmpty(next_from);
        actualReceived = true;
        setLoadingNow(false);
        audios.addAll(data.getPlaylists());
        callView(IPlaylistsInCatalogView::notifyListChanged);
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        if (isGuiResumed()) {
            showError(getView(), Utils.getCauseIfRuntime(t));
        }
    }

    public void fireRefresh() {
        audioListDisposable.clear();
        next_from = null;
        requestList();
    }

    public void fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestList();
        }
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        audioListDisposable.add(audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getPhoenixToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    @Override
    public void onGuiCreated(@NonNull IPlaylistsInCatalogView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
    }

}
