package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiUser;

public class AccountsBannedResponce {
    @SerializedName("profiles")
    public List<VKApiUser> profiles;
}
