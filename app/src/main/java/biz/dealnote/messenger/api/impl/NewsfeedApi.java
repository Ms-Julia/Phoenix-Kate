package biz.dealnote.messenger.api.impl;

import java.util.Collection;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.INewsfeedApi;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedSearchResponse;
import biz.dealnote.messenger.api.services.INewsfeedService;
import io.reactivex.rxjava3.core.Single;


class NewsfeedApi extends AbsApi implements INewsfeedApi {

    NewsfeedApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Items<VkApiFeedList>> getLists(Collection<Integer> listIds) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.getLists(join(listIds, ","), 1)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> saveList(String title, Collection<Integer> listIds) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.saveList(title, join(listIds, ","))
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deleteList(Integer list_id) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service.deleteList(list_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedSearchResponse> search(String query, Boolean extended, Integer count, Double latitude, Double longitude, Long startTime, Long endTime, String startFrom, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .search(query, integerFromBoolean(extended), count, latitude, longitude,
                                startTime, endTime, startFrom, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedCommentsResponse> getComments(Integer count, String filters, String reposts, Long startTime, Long endTime, Integer lastCommentsCount, String startFrom, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getComments(count, filters, reposts, startTime, endTime, lastCommentsCount, startFrom, fields, null)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedCommentsResponse> getMentions(Integer owner_id, Integer count, Integer offset, Long startTime, Long endTime) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getMentions(owner_id, count, offset, startTime, endTime)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> get(String filters, Boolean returnBanned, Long startTime,
                                        Long endTime, Integer maxPhotoCount, String sourceIds,
                                        String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .get(filters, integerFromBoolean(returnBanned), startTime, endTime,
                                maxPhotoCount, sourceIds, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> getRecommended(Long startTime, Long endTime,
                                                   Integer maxPhotoCount, String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getRecommended(startTime, endTime,
                                maxPhotoCount, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<NewsfeedResponse> getFeedLikes(Integer maxPhotoCount, String startFrom, Integer count, String fields) {
        return provideService(INewsfeedService.class, TokenType.USER)
                .flatMap(service -> service
                        .getFeedLikes(maxPhotoCount, startFrom, count, fields)
                        .map(extractResponseWithErrorHandling()));
    }
}
