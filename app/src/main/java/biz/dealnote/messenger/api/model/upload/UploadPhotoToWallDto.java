package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

public class UploadPhotoToWallDto {

    @SerializedName("server")
    public int server;

    @SerializedName("photo")
    public String photo;

    @SerializedName("hash")
    public String hash;

    @Override
    public String toString() {
        return "UploadPhotoToWallDto{" +
                "server=" + server +
                ", photo='" + photo + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
