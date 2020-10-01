package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

public class UploadVideoDto {

    @SerializedName("owner_id")
    public int owner_id;

    @SerializedName("video_id")
    public int video_id;

    @SerializedName("video_hash")
    public String video_hash;

    @Override
    public String toString() {
        return "UploadVideoDto{" +
                "owner_id=" + owner_id +
                ", video_id='" + video_id + '\'' +
                ", video_hash='" + video_hash + '\'' +
                '}';
    }
}
