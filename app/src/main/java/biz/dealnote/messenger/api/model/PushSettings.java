package biz.dealnote.messenger.api.model;

import com.google.gson.annotations.SerializedName;


public class PushSettings {

    @SerializedName("sound")
    public int sound;

    @SerializedName("disabled_until")
    public int disabledUntil;
}