package biz.dealnote.messenger.domain;

import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

import biz.dealnote.messenger.api.model.VKApiCheckedLink;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.Privacy;
import biz.dealnote.messenger.model.ShortLink;
import biz.dealnote.messenger.model.SimplePrivacy;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.rxjava3.core.Single;

public interface IUtilsInteractor {
    Single<Map<Integer, Privacy>> createFullPrivacies(int accountId, @NonNull Map<Integer, SimplePrivacy> orig);

    Single<Optional<Owner>> resolveDomain(int accountId, String domain);

    Single<ShortLink> getShortLink(int accountId, String url, Integer t_private);

    Single<List<ShortLink>> getLastShortenedLinks(int accountId, Integer count, Integer offset);

    Single<Integer> deleteFromLastShortened(int accountId, String key);

    Single<VKApiCheckedLink> checkLink(int accountId, String url);
}