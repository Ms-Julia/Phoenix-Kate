package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IUtilsApi;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiCheckedLink;
import biz.dealnote.messenger.api.model.VKApiShortLink;
import biz.dealnote.messenger.api.model.response.ResolveDomailResponse;
import biz.dealnote.messenger.api.services.IUtilsService;
import io.reactivex.rxjava3.core.Single;


class UtilsApi extends AbsApi implements IUtilsApi {

    UtilsApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<ResolveDomailResponse> resolveScreenName(String screenName) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.resolveScreenName(screenName)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiShortLink> getShortLink(String url, Integer t_private) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.getShortLink(url, t_private)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiShortLink>> getLastShortenedLinks(Integer count, Integer offset) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.getLastShortenedLinks(count, offset)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deleteFromLastShortened(String key) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.deleteFromLastShortened(key)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiCheckedLink> checkLink(String url) {
        return provideService(IUtilsService.class, TokenType.USER)
                .flatMap(service -> service.checkLink(url)
                        .map(extractResponseWithErrorHandling()));
    }
}