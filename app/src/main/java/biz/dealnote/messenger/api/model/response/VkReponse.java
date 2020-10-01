package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.Error;


public class VkReponse {

    @SerializedName("error")
    public Error error;

    @SerializedName("execute_errors")
    public List<Error> executeErrors;
}
