package biz.dealnote.messenger.mvp.presenter.wallattachments;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.serialize.Serializers;
import biz.dealnote.messenger.domain.IWallsRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.model.criteria.WallCriteria;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.wallattachments.IWallPhotosAttachmentsView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class WallPhotosAttachmentsPresenter extends PlaceSupportPresenter<IWallPhotosAttachmentsView> {

    private final ArrayList<Photo> mPhotos;
    private final IWallsRepository fInteractor;
    private final int owner_id;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private int loaded;
    private boolean actualDataReceived;
    private boolean endOfContent;
    private boolean actualDataLoading;

    public WallPhotosAttachmentsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        owner_id = ownerId;
        mPhotos = new ArrayList<>();
        fInteractor = Repository.INSTANCE.getWalls();
        loadActualData(0);
    }

    @Override
    public void onGuiCreated(@NonNull IWallPhotosAttachmentsView view) {
        super.onGuiCreated(view);
        view.displayData(mPhotos);
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        TmpSource source = new TmpSource(getInstanceId(), 0);

        fireTempDataUsage();

        actualDataDisposable.add(Stores.getInstance()
                .tempStore()
                .put(source.getOwnerId(), source.getSourceId(), mPhotos, Serializers.PHOTOS_SERIALIZER)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onPhotosSavedToTmpStore(position, source), Analytics::logUnexpectedError));
    }

    private void onPhotosSavedToTmpStore(int index, TmpSource source) {
        callView(view -> view.goToTempPhotosGallery(getAccountId(), source, index));
    }

    private void loadActualData(int offset) {
        actualDataLoading = true;

        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(fInteractor.getWallNoCache(accountId, owner_id, offset, 100, WallCriteria.MODE_ALL)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> onActualDataReceived(offset, data), this::onActualDataGetError));

    }

    private void onActualDataGetError(Throwable t) {
        actualDataLoading = false;
        showError(getView(), getCauseIfRuntime(t));

        resolveRefreshingView();
    }

    private void update(List<Post> data) {
        for (Post i : data) {
            if (i.hasAttachments() && !isEmpty(i.getAttachments().getPhotos()))
                mPhotos.addAll(i.getAttachments().getPhotos());
            if (i.hasCopyHierarchy())
                update(i.getCopyHierarchy());
        }
    }

    private void onActualDataReceived(int offset, List<Post> data) {

        actualDataLoading = false;
        endOfContent = data.isEmpty();
        actualDataReceived = true;
        if (endOfContent && isGuiResumed())
            getView().onSetLoadingStatus(2);

        if (offset == 0) {
            loaded = data.size();
            mPhotos.clear();
            update(data);
            resolveToolbar();
            callView(IWallPhotosAttachmentsView::notifyDataSetChanged);
        } else {
            int startSize = mPhotos.size();
            loaded += data.size();
            update(data);
            resolveToolbar();
            callView(view -> view.notifyDataAdded(startSize, mPhotos.size() - startSize));
        }

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
            if (!endOfContent)
                getView().onSetLoadingStatus(actualDataLoading ? 1 : 0);
        }
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_wall));
            getView().setToolbarSubtitle(getString(R.string.photos_count, safeCountOf(mPhotos)) + " " + getString(R.string.posts_analized, loaded));
        }
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }

    public boolean fireScrollToEnd() {
        if (!endOfContent && actualDataReceived && !actualDataLoading) {
            loadActualData(loaded);
            return false;
        }
        return true;
    }

    public void fireRefresh() {

        actualDataDisposable.clear();
        actualDataLoading = false;

        loadActualData(0);
    }
}
