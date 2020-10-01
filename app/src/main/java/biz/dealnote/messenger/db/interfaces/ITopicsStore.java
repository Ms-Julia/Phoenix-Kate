package biz.dealnote.messenger.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.OwnerEntities;
import biz.dealnote.messenger.db.model.entity.PollEntity;
import biz.dealnote.messenger.db.model.entity.TopicEntity;
import biz.dealnote.messenger.model.criteria.TopicsCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface ITopicsStore {

    @CheckResult
    Single<List<TopicEntity>> getByCriteria(@NonNull TopicsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, List<TopicEntity> topics, OwnerEntities owners, boolean canAddTopic, int defaultOrder, boolean clearBefore);

    @CheckResult
    Completable attachPoll(int accountId, int ownerId, int topicId, PollEntity pollDbo);
}