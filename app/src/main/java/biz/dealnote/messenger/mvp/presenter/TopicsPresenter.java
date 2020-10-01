package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IBoardInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.LoadMoreState;
import biz.dealnote.messenger.model.Topic;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.ITopicsView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.disposables.CompositeDisposable;


public class TopicsPresenter extends AccountDependencyPresenter<ITopicsView> {

    private static final int COUNT_PER_REQUEST = 20;

    private final int ownerId;
    private final List<Topic> topics;
    private final IBoardInteractor boardInteractor;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataReceived;
    private boolean cacheLoadingNow;
    private boolean netLoadingNow;
    private int netLoadingNowOffset;

    public TopicsPresenter(int accountId, int ownerId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        topics = new ArrayList<>();
        boardInteractor = InteractorFactory.createBoardInteractor();

        loadCachedData();
        requestActualData(0);
    }

    private void loadCachedData() {
        int accountId = getAccountId();

        cacheDisposable.add(boardInteractor.getCachedTopics(accountId, ownerId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onCachedDataReceived, RxUtils.ignore()));
    }

    private void onCachedDataReceived(List<Topic> topics) {
        cacheLoadingNow = false;

        this.topics.clear();
        this.topics.addAll(topics);

        callView(ITopicsView::notifyDataSetChanged);
    }

    private void requestActualData(int offset) {
        int accountId = getAccountId();

        netLoadingNow = true;
        netLoadingNowOffset = offset;

        resolveRefreshingView();
        resolveLoadMoreFooter();

        netDisposable.add(boardInteractor.getActualTopics(accountId, ownerId, COUNT_PER_REQUEST, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(topics -> onActualDataReceived(offset, topics), this::onActualDataGetError));
    }

    private void onActualDataGetError(Throwable t) {
        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        showError(getView(), t);
    }

    private void onActualDataReceived(int offset, List<Topic> topics) {
        cacheDisposable.clear();
        cacheLoadingNow = false;

        netLoadingNow = false;
        resolveRefreshingView();
        resolveLoadMoreFooter();

        actualDataReceived = true;
        endOfContent = topics.isEmpty();

        if (offset == 0) {
            this.topics.clear();
            this.topics.addAll(topics);
            callView(ITopicsView::notifyDataSetChanged);
        } else {
            int startCount = this.topics.size();
            this.topics.addAll(topics);
            callView(view -> view.notifyDataAdd(startCount, topics.size()));
        }
    }

    @Override
    public void onGuiCreated(@NonNull ITopicsView viewHost) {
        super.onGuiCreated(viewHost);
        viewHost.displayData(topics);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        netDisposable.dispose();
        super.onDestroyed();
    }

    @OnGuiCreated
    private void resolveRefreshingView() {
        if (isGuiReady()) {
            getView().showRefreshing(netLoadingNow);
        }
    }

    @OnGuiCreated
    private void resolveLoadMoreFooter() {
        if (isGuiReady()) {
            if (netLoadingNow && netLoadingNowOffset > 0) {
                getView().setupLoadMore(LoadMoreState.LOADING);
                return;
            }

            if (actualDataReceived && !netLoadingNow) {
                getView().setupLoadMore(LoadMoreState.CAN_LOAD_MORE);
            }

            getView().setupLoadMore(LoadMoreState.END_OF_LIST);
        }
    }

    public void fireLoadMoreClick() {
        if (canLoadMore()) {
            requestActualData(topics.size());
        }
    }

    private boolean canLoadMore() {
        return actualDataReceived && !cacheLoadingNow && !endOfContent && !topics.isEmpty();
    }

    public void fireButtonCreateClick() {
        safeShowError(getView(), R.string.not_yet_implemented_message);
    }

    public void fireRefresh() {
        netDisposable.clear();
        netLoadingNow = false;

        cacheDisposable.clear();
        cacheLoadingNow = false;

        requestActualData(0);
    }

    public void fireTopicClick(Topic topic) {
        getView().goToComments(getAccountId(), topic);
    }

    public void fireScrollToEnd() {
        if (canLoadMore()) {
            requestActualData(topics.size());
        }
    }
}