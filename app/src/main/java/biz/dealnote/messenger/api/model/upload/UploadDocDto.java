package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

public class UploadDocDto {

    @SerializedName("file")
    public String file;

    @Override
    public String toString() {
        return "UploadDocDto{" +
                "file='" + file + '\'' +
                '}';
    }
}
