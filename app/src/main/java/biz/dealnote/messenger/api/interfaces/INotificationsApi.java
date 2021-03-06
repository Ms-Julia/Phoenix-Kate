package biz.dealnote.messenger.api.interfaces;

import androidx.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.NotificationsResponse;
import io.reactivex.rxjava3.core.Single;


public interface INotificationsApi {

    @CheckResult
    Single<Integer> markAsViewed();

    @CheckResult
    Single<NotificationsResponse> get(Integer count, String startFrom, String filters,
                                      Long startTime, Long endTime);

}
