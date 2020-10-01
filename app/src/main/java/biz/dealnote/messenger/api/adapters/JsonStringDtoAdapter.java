package biz.dealnote.messenger.api.adapters;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import biz.dealnote.messenger.api.model.VkApiJsonString;

public class JsonStringDtoAdapter extends AbsAdapter implements JsonDeserializer<VkApiJsonString> {

    @Override
    public VkApiJsonString deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject root = json.getAsJsonObject();

        VkApiJsonString story = new VkApiJsonString();
        story.json_data = root.toString();
        return story;
    }
}
