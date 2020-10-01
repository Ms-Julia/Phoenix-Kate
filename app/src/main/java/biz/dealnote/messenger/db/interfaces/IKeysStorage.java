package biz.dealnote.messenger.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.crypt.AesKeyPair;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Single;

public interface IKeysStorage extends IStorage {
    @CheckResult
    Completable saveKeyPair(@NonNull AesKeyPair pair);

    @CheckResult
    Single<List<AesKeyPair>> getAll(int accountId);

    @CheckResult
    Single<List<AesKeyPair>> getKeys(int accountId, int peerId);

    @CheckResult
    Single<Optional<AesKeyPair>> findLastKeyPair(int accountId, int peerId);

    @CheckResult
    Maybe<AesKeyPair> findKeyPairFor(int accountId, long sessionId);

    @CheckResult
    Completable deleteAll(int accountId);
}
