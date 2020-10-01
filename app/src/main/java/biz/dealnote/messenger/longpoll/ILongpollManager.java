package biz.dealnote.messenger.longpoll;

import biz.dealnote.messenger.api.model.longpoll.VkApiLongpollUpdates;
import io.reactivex.rxjava3.core.Flowable;

public interface ILongpollManager {
    void forceDestroy(int accountId);

    Flowable<VkApiLongpollUpdates> observe();

    Flowable<Integer> observeKeepAlive();

    void keepAlive(int accountId);
}