package biz.dealnote.messenger.mvp.presenter;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.model.PostUpdate;
import biz.dealnote.messenger.domain.IFeedInteractor;
import biz.dealnote.messenger.domain.IWallsRepository;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.FeedList;
import biz.dealnote.messenger.model.FeedSource;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.News;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Post;
import biz.dealnote.messenger.mvp.presenter.base.PlaceSupportPresenter;
import biz.dealnote.messenger.mvp.view.IFeedView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.DisposableHolder;
import biz.dealnote.messenger.util.InputTextDialog;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.RxUtils.applySingleIOToMainSchedulers;
import static biz.dealnote.messenger.util.RxUtils.ignore;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class FeedPresenter extends PlaceSupportPresenter<IFeedView> {

    private final IFeedInteractor feedInteractor;
    private final IWallsRepository walls;
    private final List<News> mFeed;
    private final List<FeedSource> mFeedSources;
    private final DisposableHolder<Void> loadingHolder = new DisposableHolder<>();
    private final DisposableHolder<Void> cacheLoadingHolder = new DisposableHolder<>();
    private String mNextFrom;
    private String mSourceIds;
    private boolean loadingNow;
    private String loadingNowNextFrom;
    private boolean cacheLoadingNow;
    private String mTmpFeedScrollOnGuiReady;

    public FeedPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        walls = Repository.INSTANCE.getWalls();

        appendDisposable(walls.observeMinorChanges()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onPostUpdateEvent));

        feedInteractor = InteractorFactory.createFeedInteractor();
        mFeed = new ArrayList<>();
        mFeedSources = new ArrayList<>(createDefaultFeedSources());

        refreshFeedSourcesSelection();

        restoreNextFromAndFeedSources();

        refreshFeedSources();

        String scrollState = Settings.get()
                .other()
                .restoreFeedScrollState(accountId);

        loadCachedFeed(scrollState);
    }

    private static List<FeedSource> createDefaultFeedSources() {
        List<FeedSource> data = new ArrayList<>(8);
        data.add(new FeedSource(null, R.string.news_feed, false));
        data.add(new FeedSource("updates", R.string.updates, false));
        data.add(new FeedSource("friends", R.string.friends, false));
        data.add(new FeedSource("groups", R.string.groups, false));
        data.add(new FeedSource("pages", R.string.pages, false));
        data.add(new FeedSource("following", R.string.subscriptions, false));
        data.add(new FeedSource("recommendation", R.string.recommendation, false));
        data.add(new FeedSource("likes", R.string.likes_posts, false));
        return data;
    }

    private void refreshFeedSources() {
        int accountId = getAccountId();

        appendDisposable(feedInteractor.getCachedFeedLists(accountId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe(lists -> {
                    onFeedListsUpdated(lists);
                    requestActualFeedLists();
                }, ignored -> requestActualFeedLists()));
    }

    private void requestActualFeedLists() {
        int accountId = getAccountId();
        appendDisposable(feedInteractor.getActualFeedLists(accountId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe(this::onFeedListsUpdated, ignore()));
    }

    private void onPostUpdateEvent(PostUpdate update) {
        if (nonNull(update.getLikeUpdate())) {
            PostUpdate.LikeUpdate like = update.getLikeUpdate();

            int index = indexOf(update.getOwnerId(), update.getPostId());
            if (index != -1) {
                mFeed.get(index).setLikeCount(like.getCount());
                mFeed.get(index).setUserLike(like.isLiked());
                callView(view -> view.notifyItemChanged(index));
            }
        }
    }

    private void requestFeedAtLast(String startFrom) {
        loadingHolder.dispose();

        int accountId = getAccountId();
        String sourcesIds = mSourceIds;

        loadingNowNextFrom = startFrom;
        loadingNow = true;

        resolveLoadMoreFooterView();
        resolveRefreshingView();

        if ("updates".equals(sourcesIds)) {
            loadingHolder.append(feedInteractor.getActualFeed(accountId, 25, startFrom, "photo,photo_tag,wall_photo,friend,audio,video", 9, sourcesIds)
                    .compose(applySingleIOToMainSchedulers())
                    .subscribe(pair -> onActualFeedReceived(startFrom, pair.getFirst(), pair.getSecond()), this::onActualFeedGetError));
        } else {
            loadingHolder.append(feedInteractor.getActualFeed(accountId, 25, startFrom, isEmpty(sourcesIds) ? "post" : null, 9, sourcesIds)
                    .compose(applySingleIOToMainSchedulers())
                    .subscribe(pair -> onActualFeedReceived(startFrom, pair.getFirst(), pair.getSecond()), this::onActualFeedGetError));
        }
    }

    private void onActualFeedGetError(Throwable t) {
        t.printStackTrace();

        loadingNow = false;
        loadingNowNextFrom = null;

        resolveLoadMoreFooterView();
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onActualFeedReceived(String startFrom, List<News> feed, String nextFrom) {
        loadingNow = false;
        loadingNowNextFrom = null;

        mNextFrom = nextFrom;

        if (isEmpty(startFrom)) {
            mFeed.clear();
            mFeed.addAll(feed);
            callView(IFeedView::notifyFeedDataChanged);
        } else {
            int startSize = mFeed.size();
            mFeed.addAll(feed);
            callView(view -> view.notifyDataAdded(startSize, feed.size()));
        }

        resolveRefreshingView();
        resolveLoadMoreFooterView();
    }

    @Override
    public void onGuiCreated(@NonNull IFeedView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayFeedSources(mFeedSources);

        int sourceIndex = getActiveFeedSourceIndex();
        if (sourceIndex != -1) {
            viewHost.scrollFeedSourcesToPosition(sourceIndex);
        }

        viewHost.displayFeed(mFeed, mTmpFeedScrollOnGuiReady);

        mTmpFeedScrollOnGuiReady = null;
    }

    private void setCacheLoadingNow(boolean cacheLoadingNow) {
        this.cacheLoadingNow = cacheLoadingNow;

        resolveRefreshingView();
        resolveLoadMoreFooterView();
    }

    private void loadCachedFeed(@Nullable String thenScrollToState) {
        int accountId = getAccountId();

        setCacheLoadingNow(true);

        cacheLoadingHolder.append(feedInteractor
                .getCachedFeed(accountId)
                .compose(applySingleIOToMainSchedulers())
                .subscribe(feed -> onCachedFeedReceived(feed, thenScrollToState), ignore()));
    }

    @Override
    public void onDestroyed() {
        loadingHolder.dispose();
        cacheLoadingHolder.dispose();
        super.onDestroyed();
    }

    private void onCachedFeedReceived(List<News> data, @Nullable String thenScrollToState) {
        setCacheLoadingNow(false);

        mFeed.clear();
        mFeed.addAll(data);

        if (nonNull(thenScrollToState)) {
            if (isGuiReady()) {
                getView().displayFeed(mFeed, thenScrollToState);
            } else {
                mTmpFeedScrollOnGuiReady = thenScrollToState;
            }
        } else {
            if (isGuiReady()) {
                getView().notifyFeedDataChanged();
            }
        }

        if (mFeed.isEmpty()) {
            requestFeedAtLast(null);
        } else {
            if (Utils.needReloadNews(getAccountId())) {
                int vr = Settings.get().main().getStart_newsMode();
                if (vr == 2) {
                    getView().askToReload();
                } else if (vr == 1) {
                    getView().scrollTo(0);
                    requestFeedAtLast(null);
                }
            }
        }
    }

    private boolean canLoadNextNow() {
        return nonEmpty(mNextFrom) && !cacheLoadingNow && !loadingNow;
    }

    private void onFeedListsUpdated(List<FeedList> lists) {
        List<FeedSource> sources = new ArrayList<>(lists.size());

        for (FeedList list : lists) {
            sources.add(new FeedSource("list" + list.getId(), list.getTitle(), true));
        }

        mFeedSources.clear();
        mFeedSources.addAll(createDefaultFeedSources());
        mFeedSources.addAll(sources);

        int selected = refreshFeedSourcesSelection();

        if (isGuiReady()) {
            getView().notifyFeedSourcesChanged();

            if (selected != -1) {
                getView().scrollFeedSourcesToPosition(selected);
            }
        }
    }

    private int refreshFeedSourcesSelection() {
        int result = -1;
        for (int i = 0; i < mFeedSources.size(); i++) {
            FeedSource source = mFeedSources.get(i);

            if (isEmpty(mSourceIds) && isEmpty(source.getValue())) {
                source.setActive(true);
                result = i;
                continue;
            }

            if (nonEmpty(mSourceIds) && nonEmpty(source.getValue()) && mSourceIds.equals(source.getValue())) {
                source.setActive(true);
                result = i;
                continue;
            }

            source.setActive(false);
        }

        return result;
    }

    private void restoreNextFromAndFeedSources() {
        mSourceIds = Settings.get()
                .other()
                .getFeedSourceIds(getAccountId());

        mNextFrom = Settings.get()
                .other()
                .restoreFeedNextFrom(getAccountId());
    }

    private boolean isRefreshing() {
        return cacheLoadingNow || (loadingNow && isEmpty(loadingNowNextFrom));
    }

    private boolean isMoreLoading() {
        return loadingNow && nonEmpty(loadingNowNextFrom);
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(isRefreshing());
        }
    }

    private int getActiveFeedSourceIndex() {
        for (int i = 0; i < mFeedSources.size(); i++) {
            if (mFeedSources.get(i).isActive()) {
                return i;
            }
        }

        return -1;
    }

    @OnGuiCreated
    private void resolveLoadMoreFooterView() {
        if (isGuiReady()) {
            if (mFeed.isEmpty() || isEmpty(mNextFrom)) {
                getView().setupLoadMoreFooter(LoadMoreState.END_OF_LIST);
            } else if (isMoreLoading()) {
                getView().setupLoadMoreFooter(LoadMoreState.LOADING);
            } else if (canLoadNextNow()) {
                getView().setupLoadMoreFooter(LoadMoreState.CAN_LOAD_MORE);
            } else {
                getView().setupLoadMoreFooter(LoadMoreState.END_OF_LIST);
            }
        }
    }

    public void fireScrollStateOnPause(String json) {
        Settings.get()
                .other()
                .storeFeedScrollState(getAccountId(), json);
    }

    public void fireRefresh() {
        cacheLoadingHolder.dispose();
        loadingHolder.dispose();
        loadingNow = false;
        cacheLoadingNow = false;

        requestFeedAtLast(null);
    }

    public void fireScrollToBottom() {
        if (canLoadNextNow()) {
            requestFeedAtLast(mNextFrom);
        }
    }

    public void fireLoadMoreClick() {
        if (canLoadNextNow()) {
            requestFeedAtLast(mNextFrom);
        }
    }

    public void fireAddToFaveList(Context context, ArrayList<Owner> owners) {
        if (isEmpty(owners)) {
            return;
        }
        List<Integer> Ids = new ArrayList<>(owners.size());
        for (Owner i : owners) {
            Ids.add(i.getOwnerId());
        }
        new InputTextDialog.Builder(context)
                .setTitleRes(R.string.set_news_list_title)
                .setAllowEmpty(false)
                .setInputType(InputType.TYPE_CLASS_TEXT)
                .setCallback(newValue -> appendDisposable(feedInteractor.saveList(getAccountId(), newValue.trim(), Ids)
                        .compose(applySingleIOToMainSchedulers())
                        .subscribe(t -> {
                            PhoenixToast.CreatePhoenixToast(context).showToastSuccessBottom(R.string.success);
                            requestActualFeedLists();
                        }, i -> showError(getView(), i))))
                .show();
    }

    public void fireFeedSourceClick(FeedSource entry) {
        mSourceIds = entry.getValue();
        mNextFrom = null;

        cacheLoadingHolder.dispose();
        loadingHolder.dispose();
        loadingNow = false;
        cacheLoadingNow = false;

        refreshFeedSourcesSelection();
        getView().notifyFeedSourcesChanged();

        requestFeedAtLast(null);
    }

    public void fireFeedSourceDelete(Integer id) {
        appendDisposable(feedInteractor.deleteList(getAccountId(), id)
                .compose(applySingleIOToMainSchedulers())
                .subscribe(ignore(), t -> showError(getView(), t)));
    }

    public void fireNewsShareLongClick(News news) {
        getView().goToReposts(getAccountId(), news.getType(), news.getSourceId(), news.getPostId());
    }

    public void fireNewsLikeLongClick(News news) {
        getView().goToLikes(getAccountId(), news.getType(), news.getSourceId(), news.getPostId());
    }

    public void fireNewsCommentClick(News news) {
        if ("post".equalsIgnoreCase(news.getType())) {
            getView().goToPostComments(getAccountId(), news.getPostId(), news.getSourceId());
        }
    }

    public void fireNewsBodyClick(News news) {
        if ("post".equals(news.getType())) {
            Post post = news.toPost();
            getView().openPost(getAccountId(), post);
        }
    }

    public void fireNewsRepostClick(News news) {
        if ("post".equals(news.getType())) {
            getView().repostPost(getAccountId(), news.toPost());
        }
    }

    public void fireLikeClick(News news) {
        if ("post".equalsIgnoreCase(news.getType())) {
            boolean add = !news.isUserLike();
            int accountId = getAccountId();

            appendDisposable(walls.like(accountId, news.getSourceId(), news.getPostId(), add)
                    .compose(applySingleIOToMainSchedulers())
                    .subscribe(ignore(), ignore()));
        }
    }

    private int indexOf(int sourceId, int postId) {
        for (int i = 0; i < mFeed.size(); i++) {
            if (mFeed.get(i).getSourceId() == sourceId && mFeed.get(i).getPostId() == postId) {
                return i;
            }
        }

        return -1;
    }
}