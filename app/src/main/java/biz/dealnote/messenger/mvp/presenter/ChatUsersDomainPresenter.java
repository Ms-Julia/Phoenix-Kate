package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.domain.IMessagesRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.AppChatUser;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IChatUsersDomainView;
import biz.dealnote.messenger.util.RxUtils;


public class ChatUsersDomainPresenter extends AccountDependencyPresenter<IChatUsersDomainView> {

    private final int chatId;

    private final IMessagesRepository messagesInteractor;

    private final List<AppChatUser> users;
    private boolean refreshing;

    public ChatUsersDomainPresenter(int accountId, int chatId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.chatId = chatId;
        users = new ArrayList<>();
        messagesInteractor = Repository.INSTANCE.getMessages();

        requestData();
    }

    @Override
    public void onGuiCreated(@NonNull IChatUsersDomainView view) {
        super.onGuiCreated(view);
        view.displayData(users);
    }

    private void resolveRefreshing() {
        if (isGuiResumed()) {
            getView().displayRefreshing(refreshing);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshing();
    }

    private void setRefreshing(boolean refreshing) {
        this.refreshing = refreshing;
        resolveRefreshing();
    }

    private void requestData() {
        int accountId = getAccountId();

        setRefreshing(true);
        appendDisposable(messagesInteractor.getChatUsers(accountId, chatId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataReceived, this::onDataGetError));
    }

    private void onDataGetError(Throwable t) {
        setRefreshing(false);
        showError(getView(), t);
    }

    private void onDataReceived(List<AppChatUser> users) {
        setRefreshing(false);

        this.users.clear();
        this.users.addAll(users);

        callView(IChatUsersDomainView::notifyDataSetChanged);
    }

    public void fireRefresh() {
        if (!refreshing) {
            requestData();
        }
    }

    public void fireUserClick(AppChatUser user) {
        getView().openUserWall(getAccountId(), user.getMember());
    }
}
