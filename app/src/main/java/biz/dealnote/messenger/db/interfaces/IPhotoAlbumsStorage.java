package biz.dealnote.messenger.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.PhotoAlbumEntity;
import biz.dealnote.messenger.model.criteria.PhotoAlbumsCriteria;
import biz.dealnote.messenger.util.Optional;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IPhotoAlbumsStorage extends IStorage {

    @CheckResult
    Single<Optional<PhotoAlbumEntity>> findAlbumById(int accountId, int ownerId, int albumId);

    @CheckResult
    Single<List<PhotoAlbumEntity>> findAlbumsByCriteria(@NonNull PhotoAlbumsCriteria criteria);

    @CheckResult
    Completable store(int accountId, int ownerId, @NonNull List<PhotoAlbumEntity> albums, boolean clearBefore);

    @CheckResult
    Completable removeAlbumById(int accountId, int ownerId, int albumId);
}