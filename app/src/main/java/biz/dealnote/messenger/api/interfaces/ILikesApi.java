package biz.dealnote.messenger.api.interfaces;

import androidx.annotation.CheckResult;

import biz.dealnote.messenger.api.model.response.LikesListResponse;
import io.reactivex.rxjava3.core.Single;

public interface ILikesApi {

    @CheckResult
    Single<LikesListResponse> getList(String type, Integer ownerId, Integer itemId, String pageUrl, String filter,
                                      Boolean friendsOnly, Integer offset, Integer count, Boolean skipOwn, String fields);

    @CheckResult
    Single<Integer> delete(String type, Integer ownerId, int itemId);

    @CheckResult
    Single<Integer> add(String type, Integer ownerId, int itemId, String accessKey);

}
