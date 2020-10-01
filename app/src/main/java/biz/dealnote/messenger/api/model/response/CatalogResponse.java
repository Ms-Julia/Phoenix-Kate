package biz.dealnote.messenger.api.model.response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiAudioPlaylist;
import biz.dealnote.messenger.api.model.VKApiCatalogLink;
import biz.dealnote.messenger.api.model.VKApiVideo;


public class CatalogResponse {

    @SerializedName("audios")
    public List<VKApiAudio> audios;

    @SerializedName("playlists")
    public List<VKApiAudioPlaylist> playlists;

    @SerializedName("videos")
    public List<VKApiVideo> videos;

    @SerializedName("items")
    public List<VKApiCatalogLink> items;

    @SerializedName("next_from")
    public String nextFrom;
}
