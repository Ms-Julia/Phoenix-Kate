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


public class RecommendationsFriendsPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final IRelationshipInteractor relationshipInteractor;
    private final CompositeDisposable actualDataDisposable = new CompositeDisposable();
    private boolean actualDataLoading;

    public RecommendationsFriendsPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        relationshipInteractor = InteractorFactory.createRelationshipInteractor();
    }

    public void doLoad() {
        requestActualData();
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

    private void requestActualData() {
        actualDataLoading = true;
        resolveRefreshingView();

        int accountId = getAccountId();
        actualDataDisposable.add(relationshipInteractor.getRecommendations(accountId, 50)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        actualDataLoading = false;
        resolveRefreshingView();

        showError(getView(), t);
    }

    private void onDataReceived(List<User> users) {
        actualDataLoading = false;

        data.clear();
        data.addAll(users);
        callView(ISimpleOwnersView::notifyDataSetChanged);

        resolveRefreshingView();
    }

    @Override
    void onUserScrolledToEnd() {

    }

    @Override
    void onUserRefreshed() {
        actualDataDisposable.clear();
        requestActualData();
    }

    @Override
    public void onDestroyed() {
        actualDataDisposable.dispose();
        super.onDestroyed();
    }
}
