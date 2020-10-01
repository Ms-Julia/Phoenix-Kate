package biz.dealnote.messenger.db.interfaces;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.VideoEntity;
import biz.dealnote.messenger.model.VideoCriteria;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IVideoStorage extends IStorage {

    @CheckResult
    Single<List<VideoEntity>> findByCriteria(@NonNull VideoCriteria criteria);

    @CheckResult
    Completable insertData(int accountId, int ownerId, int albumId, List<VideoEntity> videos, boolean invalidateBefore);
}