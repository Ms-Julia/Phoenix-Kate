package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.IVideosInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.VideoAlbum;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IVideoAlbumsView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class VideoAlbumsPresenter extends AccountDependencyPresenter<IVideoAlbumsView> {

    private static final int COUNT_PER_LOAD = 40;

    private final int ownerId;
    private final String action;
    private final List<VideoAlbum> data;
    private final IVideosInteractor videosInteractor;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataReceived;
    private boolean netLoadingNow;
    private int netLoadingOffset;
    private boolean cacheNowLoading;

    public VideoAlbumsPresenter(int accountId, int ownerId, String action, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        videosInteractor = InteractorFactory.createVideosInteractor();
        this.ownerId = ownerId;
        this.action = action;
        data = new ArrayList<>();

        loadAllDataFromDb();
        requestActualData(0);
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().displayLoading(netLoadingNow);
        }
    }

    private void requestActualData(int offset) {
        netLoadingNow = true;
        netLoadingOffset = offset;

        resolveRefreshingView();

        int accountId = getAccountId();
        netDisposable.add(videosInteractor.getActualAlbums(accountId, ownerId, COUNT_PER_LOAD, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(albums -> onActualDataReceived(offset, albums), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onActualDataReceived(int offset, List<VideoAlbum> albums) {
        cacheDisposable.clear();
        cacheNowLoading = false;

        netLoadingNow = false;
        actualDataReceived = true;
        endOfContent = albums.isEmpty();

        resolveRefreshingView();

        if (offset == 0) {
            data.clear();
            data.addAll(albums);
            callView(IVideoAlbumsView::notifyDataSetChanged);
        } else {
            int startSize = data.size();
            data.addAll(albums);
            callView(view -> view.notifyDataAdded(startSize, albums.size()));
        }
    }

    private void loadAllDataFromDb() {
        int accountId = getAccountId();

        cacheDisposable.add(videosInteractor.getCachedAlbums(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void onCachedDataReceived(List<VideoAlbum> albums) {
        cacheNowLoading = false;
        data.clear();
        data.addAll(albums);

        callView(IVideoAlbumsView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    public void onGuiCreated(@NonNull IVideoAlbumsView view) {
        super.onGuiCreated(view);
        view.displayData(data);
    }

    private boolean canLoadMore() {
        return !endOfContent && actualDataReceived && !netLoadingNow && !cacheNowLoading && !data.isEmpty();
    }

    public void fireItemClick(VideoAlbum album) {
        getView().openAlbum(getAccountId(), ownerId, album.getId(), action, album.getTitle());
    }

    public void fireRefresh() {
        cacheDisposable.clear();
        cacheNowLoading = false;

        netDisposable.clear();

        requestActualData(0);
    }

    public void fireScrollToLast() {
        if (canLoadMore()) {
            requestActualData(data.size());
        }
    }
}
