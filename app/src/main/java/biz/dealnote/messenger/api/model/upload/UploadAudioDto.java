package biz.dealnote.messenger.api.model.upload;

import com.google.gson.annotations.SerializedName;

public class UploadAudioDto {

    @SerializedName("server")
    public String server;

    @SerializedName("audio")
    public String audio;

    @SerializedName("hash")
    public String hash;

    @Override
    public String toString() {
        return "UploadAudioDto{" +
                "server='" + server + '\'' +
                ", audio='" + audio + '\'' +
                ", hash='" + hash + '\'' +
                '}';
    }
}
