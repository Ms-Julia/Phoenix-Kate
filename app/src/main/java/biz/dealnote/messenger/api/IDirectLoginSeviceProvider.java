package biz.dealnote.messenger.api;

import biz.dealnote.messenger.api.services.IAuthService;
import io.reactivex.rxjava3.core.Single;

public interface IDirectLoginSeviceProvider {
    Single<IAuthService> provideAuthService();
}