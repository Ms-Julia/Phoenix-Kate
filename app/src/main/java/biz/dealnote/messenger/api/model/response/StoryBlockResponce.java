package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiStory;

public class StoryBlockResponce {

    @SerializedName("stories")
    public List<VKApiStory> stories;

    @SerializedName("grouped")
    public List<StoryBlockResponce> grouped;
}
