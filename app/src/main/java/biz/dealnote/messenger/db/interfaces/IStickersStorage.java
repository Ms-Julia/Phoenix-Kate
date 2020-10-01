package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.db.model.entity.StickerSetEntity;
import biz.dealnote.messenger.db.model.entity.StickersKeywordsEntity;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;


public interface IStickersStorage extends IStorage {

    Completable store(int accountId, List<StickerSetEntity> sets);

    Completable storeKeyWords(int accountId, List<StickersKeywordsEntity> sets);

    Single<List<StickerSetEntity>> getPurchasedAndActive(int accountId);

    Single<List<StickersKeywordsEntity>> getKeywordsStickers(int accountId);
}