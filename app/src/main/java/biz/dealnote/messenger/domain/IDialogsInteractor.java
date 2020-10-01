package biz.dealnote.messenger.domain;

import biz.dealnote.messenger.model.Chat;
import io.reactivex.rxjava3.core.Single;

public interface IDialogsInteractor {
    Single<Chat> getChatById(int accountId, int peerId);
}