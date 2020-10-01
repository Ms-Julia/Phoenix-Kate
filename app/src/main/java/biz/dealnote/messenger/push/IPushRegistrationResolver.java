package biz.dealnote.messenger.push;

import io.reactivex.rxjava3.core.Completable;

public interface IPushRegistrationResolver {
    boolean canReceivePushNotification();

    Completable resolvePushRegistration();
}