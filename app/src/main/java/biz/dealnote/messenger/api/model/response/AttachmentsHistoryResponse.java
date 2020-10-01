package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.VkApiAttachments;

public class AttachmentsHistoryResponse {

    @SerializedName("items")
    public List<One> items;

    @SerializedName("next_from")
    public String next_from;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    public static class One {

        @SerializedName("message_id")
        public int messageId;

        @SerializedName("attachment")
        public VkApiAttachments.Entry entry;
    }
}
