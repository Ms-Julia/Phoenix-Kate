package biz.dealnote.messenger.api.impl;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.interfaces.IAudioApi;
import biz.dealnote.messenger.api.model.IdPair;
import biz.dealnote.messenger.api.model.Items;
import biz.dealnote.messenger.api.model.VKApiAudio;
import biz.dealnote.messenger.api.model.VKApiAudioCatalog;
import biz.dealnote.messenger.api.model.VKApiAudioPlaylist;
import biz.dealnote.messenger.api.model.VkApiLyrics;
import biz.dealnote.messenger.api.model.response.CatalogResponse;
import biz.dealnote.messenger.api.model.server.VkApiAudioUploadServer;
import biz.dealnote.messenger.api.services.IAudioService;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.rxjava3.core.Single;


class AudioApi extends AbsApi implements IAudioApi {

    AudioApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<int[]> setBroadcast(IdPair audio, Collection<Integer> targetIds) {
        String audioStr = Objects.isNull(audio) ? null : audio.ownerId + "_" + audio.id;
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .setBroadcast(audioStr, join(targetIds, ","))
                        .map(extractResponseWithErrorHandling()));

    }

    @Override
    public Single<Items<VKApiAudio>> search(String query, Boolean autoComplete, Boolean lyrics, Boolean performerOnly, Integer sort, Boolean searchOwn, Integer offset) {

        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .searchOld(query, integerFromBoolean(autoComplete), integerFromBoolean(lyrics),
                                    integerFromBoolean(performerOnly), sort, integerFromBoolean(searchOwn), offset, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .search(query, integerFromBoolean(autoComplete), integerFromBoolean(lyrics),
                                    integerFromBoolean(performerOnly), sort, integerFromBoolean(searchOwn), offset)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<Items<VKApiAudioPlaylist>> searchPlaylists(String query, Integer offset) {

        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .searchPlaylistsOld(query, offset, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .searchPlaylists(query, offset)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<VKApiAudio> restore(int audioId, Integer ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .restore(audioId, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Boolean> delete(int audioId, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .delete(audioId, ownerId)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response == 1));
    }

    @Override
    public Single<Integer> edit(int ownerId, int audioId, String artist, String title, String text) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .edit(ownerId, audioId, artist, title, text)
                        .map(extractResponseWithErrorHandling())
                        .map(response -> response));
    }

    @Override
    public Single<Integer> add(int audioId, int ownerId, Integer groupId, Integer albumId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .add(audioId, ownerId, groupId, albumId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Integer> deletePlaylist(int playlist_id, int ownerId) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .deletePlaylist(playlist_id, ownerId)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> followPlaylist(int playlist_id, int ownerId, String accessKey) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .followPlaylist(playlist_id, ownerId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudioPlaylist> getPlaylistById(int playlist_id, int ownerId, String accessKey) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPlaylistById(playlist_id, ownerId, accessKey)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<Items<VKApiAudioCatalog>> getCatalog(String artist_id) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getCatalogOld(artist_id, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getCatalog(artist_id)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<Items<VKApiAudio>> get(Integer album_id, Integer ownerId, Integer offset, Integer count, String accessKey) {
        if (Settings.get().other().isUse_old_vk_api())
            return provideService(IAudioService.class).flatMap(service -> service.getOld(album_id, ownerId, offset, count, "5.90", accessKey).map(extractResponseWithErrorHandling()));
        else
            return provideService(IAudioService.class).flatMap(service -> service.get(album_id, ownerId, offset, count, accessKey).map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiAudio>> getPopular(Integer foreign,
                                               Integer genre, Integer count) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getPopularOld(foreign, genre, count, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getPopular(foreign, genre, count)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<Items<VKApiAudio>> getRecommendations(Integer audioOwnerId, Integer count) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getRecommendationsOld(audioOwnerId, count, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getRecommendations(audioOwnerId, count)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<Items<VKApiAudio>> getRecommendationsByAudio(String audio, Integer count) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getRecommendationsByAudioOld(audio, count, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getRecommendationsByAudio(audio, count)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<Items<VKApiAudioPlaylist>> getPlaylists(int owner_id, int offset, int count) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getPlaylists(owner_id, offset, count)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<List<VKApiAudio>> getById(String audios) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getByIdOld(audios, "5.90")
                            .map(extractResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getById(audios)
                            .map(extractResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<List<VKApiAudio>> getByIdOld(String audios) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getByIdOld(audios, "5.90")
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VkApiLyrics> getLyrics(int lyrics_id) {
        return provideService(IAudioService.class)
                .flatMap(service -> service
                        .getLyrics(lyrics_id)
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<CatalogResponse> getCatalogBlockById(String block_id, String start_from) {
        if (Settings.get().other().isUse_old_vk_api()) {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getCatalogBlockByIdOld(block_id, start_from, "5.90")
                            .map(extractBlockResponseWithErrorHandling()));
        } else {
            return provideService(IAudioService.class)
                    .flatMap(service -> service
                            .getCatalogBlockById(block_id, start_from)
                            .map(extractBlockResponseWithErrorHandling()));
        }
    }

    @Override
    public Single<VkApiAudioUploadServer> getUploadServer() {
        return provideService(IAudioService.class)
                .flatMap(service -> service.getUploadServer()
                        .map(extractResponseWithErrorHandling()));
    }

    @Override
    public Single<VKApiAudio> save(String server, String audio, String hash, String artist, String title) {
        return provideService(IAudioService.class)
                .flatMap(service -> service.save(server, audio, hash, artist, title)
                        .map(extractResponseWithErrorHandling()));
    }
}