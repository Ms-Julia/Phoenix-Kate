package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IRelationshipInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.messenger.util.RxUtils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.nonEmpty;


public class MutualFriendsPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final int userId;
    private final IRelationshipInteractor relationshipInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean actualDataLoading;

    public MutualFriendsPresenter(int accountId, int userId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.userId = userId;
        relationshipInteractor = InteractorFactory.createRelationshipInteractor();

        requestActualData(0);
    }

    public void doLoad() {
        requestActualData(0);
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(actualDataLoading);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void requestActualData(int offset) {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getMutualFriends(accountId, userId, 200, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(users -> onDataReceived(offset, users), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        actualDataLoading = false;
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onDataReceived(int offset, List<User> users) {
        actualDataLoading = false;

        endOfContent = users.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(users);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int sizeBefore = data.size();
            data.addAll(users);
            callView(view -> view.notifyDataAdded(sizeBefore, users.size()));
        }

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {
        if (!endOfContent && !actualDataLoading && nonEmpty(data)) {
            requestActualData(data.size());
        }
    }

    @Override
    void onUserRefreshed() {
        actualDataDisposable.clear();
        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}