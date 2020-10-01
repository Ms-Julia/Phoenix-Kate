package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.ILikesInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

public class LikesListPresenter extends SimpleOwnersPresenter<ISimpleOwnersView> {

    private final String type;
    private final int ownerId;
    private final int itemId;
    private final String filter;

    private final ILikesInteractor likesInteractor;
    private final CompositeDisposable netDisposable = new CompositeDisposable();
    private boolean endOfContent;
    private boolean loadingNow;

    public LikesListPresenter(int accountId, String type, int ownerId, int itemId, String filter, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.type = type;
        this.ownerId = ownerId;
        this.itemId = itemId;
        this.filter = filter;

        likesInteractor = InteractorFactory.createLikesInteractor();

        requestData(0);
    }
    //private int loadingOffset;

    private void requestData(int offset) {
        loadingNow = true;
        //this.loadingOffset = offset;

        int accountId = getAccountId();

        resolveRefreshingView();
        netDisposable.add(likesInteractor.getLikes(accountId, type, ownerId, itemId, filter, 50, offset)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(owners -> onDataReceived(offset, owners), this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        showError(getView(), Utils.getCauseIfRuntime(t));
        resolveRefreshingView();
    }

    private void onDataReceived(int offset, List<Owner> owners) {
        loadingNow = false;
        endOfContent = owners.isEmpty();

        if (offset == 0) {
            data.clear();
            data.addAll(owners);
            callView(ISimpleOwnersView::notifyDataSetChanged);
        } else {
            int sizeBefore = data.size();
            data.addAll(owners);
            callView(view -> view.notifyDataAdded(sizeBefore, owners.size()));
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
            getView().displayRefreshing(loadingNow);
        }
    }

    @Override
    public void onDestroyed() {
        netDisposable.dispose();
        super.onDestroyed();
    }

    @Override
    void onUserRefreshed() {
        netDisposable.clear();
        requestData(0);
    }

    @Override
    void onUserScrolledToEnd() {
        if (!loadingNow && !endOfContent && Utils.nonEmpty(data)) {
            requestData(data.size());
        }
    }
}