package biz.dealnote.messenger.mvp.presenter.search;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.IMessagesRepository;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.fragment.search.criteria.DialogsSearchCriteria;
import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Chat;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.Peer;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.search.IDialogsSearchView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.core.Single;

public class DialogsSearchPresenter extends AbsSearchPresenter<IDialogsSearchView, DialogsSearchCriteria, Object, IntNextFrom> {

    private final IMessagesRepository messagesInteractor;

    public DialogsSearchPresenter(int accountId, @Nullable DialogsSearchCriteria criteria, @Nullable Bundle savedInstanceState) {
        super(accountId, criteria, savedInstanceState);
        messagesInteractor = Repository.INSTANCE.getMessages();
    }

    @Override
    IntNextFrom getInitialNextFrom() {
        return new IntNextFrom(0);
    }

    @Override
    boolean isAtLast(IntNextFrom startFrom) {
        return startFrom.getOffset() == 0;
    }

    @Override
    Single<Pair<List<Object>, IntNextFrom>> doSearch(int accountId, DialogsSearchCriteria criteria, IntNextFrom startFrom) {
        return messagesInteractor.searchDialogs(accountId, 20, criteria.getQuery())
                .map(models -> Pair.Companion.create(models, null));
        // null because load more not supported
    }

    @Override
    DialogsSearchCriteria instantiateEmptyCriteria() {
        return new DialogsSearchCriteria("");
    }

    @Override
    boolean canSearch(DialogsSearchCriteria criteria) {
        return Utils.trimmedNonEmpty(criteria.getQuery());
    }

    public void fireEntryClick(Object o) {
        int accountId = getAccountId();
        int messagesOwnerId = getAccountId(); // todo Community dialogs seacrh !!!

        if (o instanceof User) {
            User user = (User) o;
            Peer peer = new Peer(Peer.fromUserId(user.getId())).setTitle(user.getFullName()).setAvaUrl(user.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        } else if (o instanceof Community) {
            Community group = (Community) o;
            Peer peer = new Peer(Peer.fromGroupId(group.getId())).setTitle(group.getFullName()).setAvaUrl(group.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        } else if (o instanceof Chat) {
            Chat chat = (Chat) o;
            Peer peer = new Peer(Peer.fromChatId(chat.getId())).setTitle(chat.getTitle()).setAvaUrl(chat.getMaxSquareAvatar());
            getView().openChatWith(accountId, messagesOwnerId, peer);
        }
    }
}