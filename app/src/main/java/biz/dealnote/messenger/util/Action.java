package biz.dealnote.messenger.util;


public interface Action<T> {
    void call(T targer);
}
