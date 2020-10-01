package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.api.model.VKApiUser;
import biz.dealnote.messenger.api.model.feedback.VkApiBaseFeedback;

public class NotificationsResponse {

    @SerializedName("count")
    public int count;

    @SerializedName("items")
    public List<VkApiBaseFeedback> notifications;

    @SerializedName("profiles")
    public List<VKApiUser> profiles;

    @SerializedName("groups")
    public List<VKApiCommunity> groups;

    @SerializedName("next_from")
    public String nextFrom;

    @SerializedName("last_viewed")
    public long lastViewed;
}
