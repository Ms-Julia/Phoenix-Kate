package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.VkApiStickersKeywords;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IStoreService {

    @FormUrlEncoded
    @POST("execute")
    Single<BaseResponse<VkApiStickersKeywords>> getStickers(@Field("code") String code);
}