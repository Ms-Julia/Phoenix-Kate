package biz.dealnote.messenger.db.serialize;


public interface ISerializeAdapter<T> {
    T deserialize(String raw);

    String serialize(T data);
}