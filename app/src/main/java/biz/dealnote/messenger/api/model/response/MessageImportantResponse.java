package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiMessage;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VkApiConversation;

public class MessageImportantResponse {

    @SerializedName("messages")
    public Message messages;

    @SerializedName("conversations")
    public List<VkApiConversation> conversations;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    public static final class Message {
        @SerializedName("items")
        public List<VKApiMessage> items;

        @SerializedName("count")
        public int count;
    }

}