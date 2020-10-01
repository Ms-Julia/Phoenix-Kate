package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.Topic;
import io.reactivex.rxjava3.core.Single;

public interface IBoardInteractor {
    Single<List<Topic>> getCachedTopics(int accountId, int ownerId);

    Single<List<Topic>> getActualTopics(int accountId, int ownerId, int count, int offset);
}