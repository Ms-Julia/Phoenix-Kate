package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.domain.IFaveInteractor;
import biz.dealnote.messenger.domain.IWallsRepository;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.IFavePostsView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;


public class FavePostsPresenter extends PlaceSupportPresenter<IFavePostsView> {

    private static final int COUNT = 50;
    private final List<Post> posts;
    private final IFaveInteractor faveInteractor;
    private final IWallsRepository wallInteractor;
    private final CompositeDisposable cacheCompositeDisposable = new CompositeDisposable();
    private boolean requestNow;
    private boolean actualInfoReceived;
    private int nextOffset;
    private boolean endOfContent;

    public FavePostsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        posts = new ArrayList<>();
        faveInteractor = InteractorFactory.createFaveInteractor();
        wallInteractor = Repository.INSTANCE.getWalls();

        appendDisposable(wallInteractor.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdate));

        loadCachedData();
    }

    public void LoadTool() {
        requestActual(0);
    }

    private void onPostUpdate(PostUpdate update) {
        // likes only
        if (isNull(update.getLikeUpdate())) {
            return;
        }

        Pair<Integer, Post> info = Utils.findInfoByPredicate(posts, post -> post.getVkid() == update.getPostId() && post.getOwnerId() == update.getOwnerId());

        if (nonNull(info)) {
            Post post = info.getSecond();

            if (getAccountId() == update.getAccountId()) {
                post.setUserLikes(update.getLikeUpdate().isLiked());
            }

            post.setLikesCount(update.getLikeUpdate().getCount());
            callView(view -> view.notifyItemChanged(info.getFirst()));
        }
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().showRefreshing(requestNow);
        }
    }

    @Override
    public void onGuiCreated(@NonNull IFavePostsView view) {
        super.onGuiCreated(view);
        view.displayData(posts);
    }

    private void requestActual(int offset) {
        setRequestNow(true);
        int accountId = getAccountId();
        int newOffset = offset + COUNT;
        appendDisposable(faveInteractor.getPosts(accountId, COUNT, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(posts -> onActualDataReceived(offset, newOffset, posts), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable throwable) {
        setRequestNow(false);
        showError(getView(), throwable);
    }

    private void onActualDataReceived(int offset, int newOffset, List<Post> data) {
        setRequestNow(false);

        nextOffset = newOffset;
        endOfContent = data.isEmpty();
        actualInfoReceived = true;

        if (offset == 0) {
            posts.clear();
            posts.addAll(data);
            callView(IFavePostsView::notifyDataSetChanged);
        } else {
            int sizeBefore = posts.size();
            posts.addAll(data);
            callView(view -> view.notifyDataAdded(sizeBefore, data.size()));
        }
    }

    private void loadCachedData() {
        int accountId = getAccountId();

        cacheCompositeDisposable.add(faveInteractor.getCachedPosts(accountId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, Analytics::logUnexpectedError));
    }

    private void onCachedDataReceived(List<Post> posts) {
        this.posts.clear();
        this.posts.addAll(posts);
        callView(IFavePostsView::notifyDataSetChanged);
    }

    @Override
    public void onDestroyed() {
        cacheCompositeDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRefresh() {
        if (!requestNow) {
            requestActual(0);
        }
    }

    public void fireScrollToEnd() {
        if (!posts.isEmpty() && actualInfoReceived && !requestNow && !endOfContent) {
            requestActual(nextOffset);
        }
    }

    public void fireLikeClick(Post post) {
        int accountId = getAccountId();
        appendDisposable(wallInteractor.like(accountId, post.getOwnerId(), post.getVkid(), !post.isUserLikes())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(RxUtils.ignore(), this::onLikeError));
    }

    public void firePostDelete(int index, Post post) {
        appendDisposable(faveInteractor.removePost(getAccountId(), post.getOwnerId(), post.getVkid())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(videos -> {
                    posts.remove(index);
                    callView(IFavePostsView::notifyDataSetChanged);
                }, this::onActualDataGetError));
    }

    private void onLikeError(Throwable t) {
        showError(getView(), t);
    }
}