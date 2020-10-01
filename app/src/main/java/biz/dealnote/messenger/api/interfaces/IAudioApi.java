package biz.dealnote.messenger.api.interfaces;

import androidx.annotation.CheckResult;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiAudioCatalog;
import biz.dealnote.messenger.api.model.VKApiAudioPlaylist;
import biz.dealnote.messenger.api.model.VkApiLyrics;
import biz.dealnote.messenger.api.model.response.CatalogResponse;
import biz.dealnote.messenger.api.model.server.VkApiAudioUploadServer;
import io.reactivex.rxjava3.core.Single;


public interface IAudioApi {

    @CheckResult
    Single<int[]> setBroadcast(IdPair audio, Collection<Integer> targetIds);

    @CheckResult
    Single<Items<VKApiAudio>> search(String query, Boolean autoComplete, Boolean lyrics,
                                     Boolean performerOnly, Integer sort, Boolean searchOwn,
                                     Integer offset);

    @CheckResult
    Single<Items<VKApiAudioPlaylist>> searchPlaylists(String query, Integer offset);

    @CheckResult
    Single<VKApiAudio> restore(int audioId, Integer ownerId);

    @CheckResult
    Single<Boolean> delete(int audioId, int ownerId);

    @CheckResult
    Single<Integer> edit(int ownerId, int audioId, String artist, String title, String text);

    @CheckResult
    Single<Integer> add(int audioId, int ownerId, Integer groupId, Integer album_id);

    @CheckResult
    Single<Items<VKApiAudio>> get(Integer album_id, Integer ownerI,
                                  Integer offset, Integer count, String accessKey);

    @CheckResult
    Single<List<VKApiAudio>> getPopular(Integer foreign,
                                        Integer genre, Integer count);

    @CheckResult
    Single<Integer> deletePlaylist(int playlist_id, int ownerId);

    @CheckResult
    Single<VKApiAudioPlaylist> followPlaylist(int playlist_id, int ownerId, String accessKey);

    @CheckResult
    Single<VKApiAudioPlaylist> getPlaylistById(int playlist_id, int ownerId, String accessKey);

    @CheckResult
    Single<Items<VKApiAudio>> getRecommendations(Integer audioOwnerId, Integer count);

    @CheckResult
    Single<Items<VKApiAudio>> getRecommendationsByAudio(String audio, Integer count);

    @CheckResult
    Single<List<VKApiAudio>> getById(String audios);

    @CheckResult
    Single<List<VKApiAudio>> getByIdOld(String audios);

    @CheckResult
    Single<VkApiLyrics> getLyrics(int lyrics_id);

    @CheckResult
    Single<Items<VKApiAudioPlaylist>> getPlaylists(int owner_id, int offset, int count);

    @CheckResult
    Single<Items<VKApiAudioCatalog>> getCatalog(String artist_id);

    @CheckResult
    Single<CatalogResponse> getCatalogBlockById(String block_id, String start_from);

    @CheckResult
    Single<VkApiAudioUploadServer> getUploadServer();

    @CheckResult
    Single<VKApiAudio> save(String server, String audio, String hash, String artist, String title);
}
