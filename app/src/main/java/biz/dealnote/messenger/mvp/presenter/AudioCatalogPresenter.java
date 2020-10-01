package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.AudioCatalog;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IAudioCatalogView;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;


public class AudioCatalogPresenter extends AccountDependencyPresenter<IAudioCatalogView> {

    private final List<AudioCatalog> pages;

    private final IAudioInteractor fInteractor;
    private final String artist_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataLoading;

    public AudioCatalogPresenter(int accountId, String artist_id, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        pages = new ArrayList<>();
        this.artist_id = artist_id;
        fInteractor = InteractorFactory.createAudioInteractor();
    }

    public void LoadAudiosTool() {
        loadActualData();
    }

    @Override
    public void onGuiCreated(@NonNull IAudioCatalogView view) {
        super.onGuiCreated(view);
        view.displayData(pages);
    }

    private void loadActualData() {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getCatalog(accountId, artist_id)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onActualDataReceived, this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void onActualDataReceived(List<AudioCatalog> data) {

        actualDataLoading = false;

        pages.clear();
        pages.addAll(data);
        callView(IAudioCatalogView::notifyDataSetChanged);

        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(actualDataLoading);
        }
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getPhoenixToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData();
    }
}
