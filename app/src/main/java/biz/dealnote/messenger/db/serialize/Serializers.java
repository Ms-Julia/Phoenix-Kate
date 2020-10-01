package biz.dealnote.messenger.db.serialize;

import com.google.gson.Gson;

import biz.dealnote.messenger.model.Photo;


public class Serializers {

    private static final Gson GSON = new Gson();

    public static final ISerializeAdapter<Photo> PHOTOS_SERIALIZER = new ISerializeAdapter<Photo>() {
        @Override
        public Photo deserialize(String raw) {
            return GSON.fromJson(raw, Photo.class);
        }

        @Override
        public String serialize(Photo data) {
            return GSON.toJson(data);
        }
    };
}