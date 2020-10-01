package biz.dealnote.messenger.domain;

import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Observable;

public interface IBlacklistRepository {
    Completable fireAdd(int accountId, User user);

    Completable fireRemove(int accountId, int userId);

    Observable<Pair<Integer, User>> observeAdding();

    Observable<Pair<Integer, Integer>> observeRemoving();
}