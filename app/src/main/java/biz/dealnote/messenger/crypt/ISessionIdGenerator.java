package biz.dealnote.messenger.crypt;

import io.reactivex.rxjava3.core.Single;

public interface ISessionIdGenerator {
    Single<Long> generateNextId();
}