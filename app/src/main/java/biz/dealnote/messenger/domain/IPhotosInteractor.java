package biz.dealnote.messenger.domain;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.VKApiPhotoTags;
import biz.dealnote.messenger.model.AccessIdPair;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IPhotosInteractor {
    Single<List<Photo>> get(int accountId, int ownerId, int albumId, int count, int offset, boolean rev);

    Single<List<Photo>> getUsersPhoto(int accountId, Integer ownerId, Integer extended, Integer offset, Integer count);

    Single<List<Photo>> getAll(int accountId, Integer ownerId, Integer extended, Integer photo_sizes, Integer offset, Integer count);

    Single<List<Photo>> getAllCachedData(int accountId, int ownerId, int albumId);

    Single<PhotoAlbum> getAlbumById(int accountId, int ownerId, int albumId);

    Single<List<PhotoAlbum>> getCachedAlbums(int accountId, int ownerId);

    Single<List<PhotoAlbum>> getActualAlbums(int accountId, int ownerId, int count, int offset);

    Single<Integer> like(int accountId, int ownerId, int photoId, boolean add, String accessKey);

    Single<Integer> copy(int accountId, int ownerId, int photoId, String accessKey);

    Completable removedAlbum(int accountId, int ownerId, int albumId);

    Completable deletePhoto(int accountId, int ownerId, int photoId);

    Completable restorePhoto(int accountId, int ownerId, int photoId);

    Single<List<Photo>> getPhotosByIds(int accountId, Collection<AccessIdPair> ids);

    Single<List<VKApiPhotoTags>> getTags(int accountId, Integer ownerId, Integer photo_id, String access_key);
}