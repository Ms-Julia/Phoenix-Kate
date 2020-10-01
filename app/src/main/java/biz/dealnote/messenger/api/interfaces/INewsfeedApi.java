package biz.dealnote.messenger.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VkApiFeedList;
import biz.dealnote.messenger.api.model.response.NewsfeedCommentsResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedResponse;
import biz.dealnote.messenger.api.model.response.NewsfeedSearchResponse;
import io.reactivex.rxjava3.core.Single;

public interface INewsfeedApi {

    @CheckResult
    Single<Items<VkApiFeedList>> getLists(Collection<Integer> listIds);

    @CheckResult
    Single<NewsfeedSearchResponse> search(String query, Boolean extended, Integer count,
                                          Double latitude, Double longitude, Long startTime,
                                          Long endTime, String startFrom, String fields);

    @CheckResult
    Single<Integer> saveList(String title, Collection<Integer> listIds);

    @CheckResult
    Single<Integer> deleteList(Integer list_id);

    @CheckResult
    Single<NewsfeedCommentsResponse> getComments(Integer count, String filters, String reposts,
                                                 Long startTime, Long endTime, Integer lastCommentsCount,
                                                 String startFrom, String fields);

    @CheckResult
    Single<NewsfeedCommentsResponse> getMentions(Integer owner_id, Integer count, Integer offset, Long startTime, Long endTime);

    @CheckResult
    Single<NewsfeedResponse> get(String filters, Boolean returnBanned, Long startTime, Long endTime,
                                 Integer maxPhotoCount, String sourceIds, String startFrom, Integer count, String fields);

    @CheckResult
    Single<NewsfeedResponse> getRecommended(Long startTime, Long endTime,
                                            Integer maxPhotoCount, String startFrom, Integer count, String fields);

    @CheckResult
    Single<NewsfeedResponse> getFeedLikes(Integer maxPhotoCount, String startFrom, Integer count, String fields);

}
