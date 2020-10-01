package biz.dealnote.messenger.domain;

import android.content.Context;

import androidx.annotation.Nullable;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.fragment.search.criteria.AudioPlaylistSearchCriteria;
import biz.dealnote.messenger.fragment.search.criteria.AudioSearchCriteria;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioCatalog;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.model.CatalogBlock;
import biz.dealnote.messenger.model.IdPair;
import biz.dealnote.messenger.util.FindAt;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IAudioInteractor {
    Completable add(int accountId, Audio orig, Integer groupId, Integer albumId);

    Completable delete(int accountId, int audioId, int ownerId);

    Completable edit(int accountId, int ownerId, int audioId, String artist, String title, String text);

    Completable restore(int accountId, int audioId, int ownerId);

    Completable sendBroadcast(int accountId, int audioOwnerId, int audioId, @Nullable Collection<Integer> targetIds);

    Single<List<Audio>> get(int accountId, Integer album_id, int ownerId, int offset, int count, String accessKey);

    Single<List<Audio>> getById(int accountId, List<IdPair> audios);

    Single<List<Audio>> getByIdOld(int accountId, List<IdPair> audios);

    Single<String> getLyrics(int accountId, int lyrics_id);

    Single<List<Audio>> getPopular(int accountId, int foreign, int genre, int count);

    Single<List<Audio>> getRecommendations(int accountId, int audioOwnerId, int count);

    Single<List<Audio>> getRecommendationsByAudio(int accountId, String audio, int count);

    Single<List<Audio>> search(int accountId, AudioSearchCriteria criteria, int offset);

    Single<List<AudioPlaylist>> searchPlaylists(int accountId, AudioPlaylistSearchCriteria criteria, int offset);

    Single<List<AudioPlaylist>> getPlaylists(int accountId, int owner_id, int offset, int count);

    Single<AudioPlaylist> followPlaylist(int accountId, int playlist_id, int ownerId, String accessKey);

    Single<AudioPlaylist> getPlaylistById(int accountId, int playlist_id, int ownerId, String accessKey);

    Single<Integer> deletePlaylist(int accountId, int playlist_id, int ownerId);

    Single<List<AudioPlaylist>> getDualPlaylists(int accountId, int owner_id, int first_playlist, int second_playlist);

    Single<List<AudioCatalog>> getCatalog(int accountId, String artist_id);

    Single<CatalogBlock> getCatalogBlockById(int accountId, String block_id, String start_from);

    Completable PlaceToAudioCache(Context context);

    Single<List<Audio>> loadLocalAudios(int accountId, Context context);

    Single<Pair<FindAt, List<AudioPlaylist>>> search_owner_playlist(int accountId, String q, int ownerId, int count, int offset, int loaded);
}