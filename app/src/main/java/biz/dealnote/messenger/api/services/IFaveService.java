package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.FaveLinkDto;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.VkApiAttachments;
import biz.dealnote.messenger.api.model.response.BaseResponse;
import biz.dealnote.messenger.api.model.response.FavePageResponse;
import biz.dealnote.messenger.api.model.response.FavePostsResponse;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.POST;

public interface IFaveService {

    @FormUrlEncoded
    @POST("fave.getPages")
    Single<BaseResponse<Items<FavePageResponse>>> getPages(@Field("offset") Integer offset,
                                                           @Field("count") Integer count,
                                                           @Field("type") String type,
                                                           @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.get")
    Single<BaseResponse<Items<VkApiAttachments.Entry>>> getVideos(@Field("offset") Integer offset,
                                                                  @Field("count") Integer count,
                                                                  @Field("item_type") String item_type,
                                                                  @Field("extended") Integer extended,
                                                                  @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.get")
    Single<BaseResponse<Items<VkApiAttachments.Entry>>> getArticles(@Field("offset") Integer offset,
                                                                    @Field("count") Integer count,
                                                                    @Field("item_type") String item_type,
                                                                    @Field("extended") Integer extended,
                                                                    @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.get")
    Single<BaseResponse<FavePostsResponse>> getPosts(@Field("offset") Integer offset,
                                                     @Field("count") Integer count,
                                                     @Field("item_type") String item_type,
                                                     @Field("extended") Integer extended,
                                                     @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.get")
    Single<BaseResponse<Items<FaveLinkDto>>> getLinks(@Field("offset") Integer offset,
                                                      @Field("count") Integer count,
                                                      @Field("item_type") String item_type,
                                                      @Field("extended") Integer extended,
                                                      @Field("fields") String fields);

    @FormUrlEncoded
    @POST("fave.getPhotos")
    Single<BaseResponse<Items<VKApiPhoto>>> getPhotos(@Field("offset") Integer offset,
                                                      @Field("count") Integer count);

    @FormUrlEncoded
    @POST("fave.addLink")
    Single<BaseResponse<Integer>> addLink(@Field("link") String link);

    @FormUrlEncoded
    @POST("fave.addPage")
    Single<BaseResponse<Integer>> addPage(@Field("user_id") Integer userId,
                                          @Field("group_id") Integer groupId);

    @FormUrlEncoded
    @POST("fave.addVideo")
    Single<BaseResponse<Integer>> addVideo(@Field("owner_id") Integer owner_id,
                                           @Field("id") Integer id,
                                           @Field("access_key") String access_key);

    @FormUrlEncoded
    @POST("fave.addArticle")
    Single<BaseResponse<Integer>> addArticle(@Field("url") String url);

    @FormUrlEncoded
    @POST("fave.addPost")
    Single<BaseResponse<Integer>> addPost(@Field("owner_id") Integer owner_id,
                                          @Field("id") Integer id,
                                          @Field("access_key") String access_key);

    //https://vk.com/dev/fave.removePage
    @FormUrlEncoded
    @POST("fave.removePage")
    Single<BaseResponse<Integer>> removePage(@Field("user_id") Integer userId,
                                             @Field("group_id") Integer groupId);

    @FormUrlEncoded
    @POST("fave.removeLink")
    Single<BaseResponse<Integer>> removeLink(@Field("link_id") String linkId);

    @FormUrlEncoded
    @POST("fave.removeArticle")
    Single<BaseResponse<Integer>> removeArticle(@Field("owner_id") Integer owner_id,
                                                @Field("article_id") Integer article_id);

    @FormUrlEncoded
    @POST("fave.removePost")
    Single<BaseResponse<Integer>> removePost(@Field("owner_id") Integer owner_id,
                                             @Field("id") Integer id);

    @FormUrlEncoded
    @POST("fave.removeVideo")
    Single<BaseResponse<Integer>> removeVideo(@Field("owner_id") Integer owner_id,
                                              @Field("id") Integer id);

}
