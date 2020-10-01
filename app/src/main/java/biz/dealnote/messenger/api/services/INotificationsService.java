package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.NotificationsResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface INotificationsService {

    @POST("notifications.markAsViewed")
    Single<BaseResponse<Integer>> markAsViewed();

    @FormUrlEncoded
    @POST("notifications.get")
    Single<BaseResponse<NotificationsResponse>> get(@Field("count") Integer count,
                                                    @Field("start_from") String startFrom,
                                                    @Field("filters") String filters,
                                                    @Field("start_time") Long startTime,
                                                    @Field("end_time") Long endTime);

}
