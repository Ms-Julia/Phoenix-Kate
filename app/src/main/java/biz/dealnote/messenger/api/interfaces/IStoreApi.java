package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.VkApiStickersKeywords;
import io.reactivex.rxjava3.core.Single;


public interface IStoreApi {
    Single<VkApiStickersKeywords> getStickers();
}