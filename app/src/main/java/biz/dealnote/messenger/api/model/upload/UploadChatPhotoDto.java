package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

public class UploadChatPhotoDto {

    @SerializedName("response")
    public String response;

    @Override
    public String toString() {
        return "UploadChatPhotoDto{" +
                "response='" + response + '\'' +
                '}';
    }
}
