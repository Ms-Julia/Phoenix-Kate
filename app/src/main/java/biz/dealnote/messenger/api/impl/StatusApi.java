package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IStatusApi;
import biz.dealnote.messenger.api.services.IStatusService;
import io.reactivex.rxjava3.core.Single;


class StatusApi extends AbsApi implements IStatusApi {

    StatusApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<Boolean> set(String text, Integer groupId) {
        return provideService(IStatusService.class, TokenType.USER)
                .flatMap(service -> service.set(text, groupId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }
}
