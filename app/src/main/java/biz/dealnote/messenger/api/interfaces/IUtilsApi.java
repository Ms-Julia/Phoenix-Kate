package biz.dealnote.messenger.api.interfaces;

import androidx.annotation.CheckResult;

import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiCheckedLink;
import biz.dealnote.messenger.api.model.VKApiShortLink;
import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import io.reactivex.rxjava3.core.Single;

public interface IUtilsApi {

    @CheckResult
    Single<ResolveDomailResponse> resolveScreenName(String screenName);

    @CheckResult
    Single<VKApiShortLink> getShortLink(String url, Integer t_private);

    @CheckResult
    Single<Items<VKApiShortLink>> getLastShortenedLinks(Integer count, Integer offset);

    @CheckResult
    Single<Integer> deleteFromLastShortened(String key);

    @CheckResult
    Single<VKApiCheckedLink> checkLink(String url);
}
