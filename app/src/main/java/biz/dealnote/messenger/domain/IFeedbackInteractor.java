package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.model.feedback.Feedback;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IFeedbackInteractor {
    Single<List<Feedback>> getCachedFeedbacks(int accountId);

    Single<Pair<List<Feedback>, String>> getActualFeedbacks(int accountId, int count, String startFrom);

    Completable maskAaViewed(int accountId);
}