package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

public class BlockResponse<T> {
    @SerializedName("block")
    public T block;
}
