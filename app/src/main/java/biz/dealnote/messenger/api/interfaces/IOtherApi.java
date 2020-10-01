package biz.dealnote.messenger.api.interfaces;

import java.util.Map;

import biz.dealnote.messenger.util.Optional;
import io.reactivex.rxjava3.core.Single;

public interface IOtherApi {
    Single<Optional<String>> rawRequest(String method, Map<String, String> postParams);
}