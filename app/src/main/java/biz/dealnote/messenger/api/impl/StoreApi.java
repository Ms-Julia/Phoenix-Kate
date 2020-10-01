package biz.dealnote.messenger.api.impl;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.api.IServiceProvider;
import biz.dealnote.messenger.api.TokenType;
import biz.dealnote.messenger.api.interfaces.IStoreApi;
import biz.dealnote.messenger.api.model.VkApiStickersKeywords;
import biz.dealnote.messenger.api.services.IStoreService;
import biz.dealnote.messenger.settings.Settings;
import io.reactivex.rxjava3.core.Single;


class StoreApi extends AbsApi implements IStoreApi {

    StoreApi(int accountId, IServiceProvider provider) {
        super(accountId, provider);
    }

    @Override
    public Single<VkApiStickersKeywords> getStickers() {
        return provideService(IStoreService.class, TokenType.USER)
                .flatMap(service -> service
                        .getStickers(Settings.get().other().isHint_stickers() ? "var pack = API.store.getProducts({'v':'" + Constants.API_VERSION + "','extended':1,'filters':'active','type':'stickers'}); var recent = API.messages.getRecentStickers({'v':'" + Constants.API_VERSION + "'}); var dic=API.store.getStickersKeywords({'v':'" + Constants.API_VERSION + "','aliases':1,'all_products':1}).dictionary;return {'sticker_pack': pack, 'recent': recent, 'keywords': dic@.words, 'words_stickers': dic@.user_stickers};"
                                : "var pack = API.store.getProducts({'v':'" + Constants.API_VERSION + "','extended':1,'filters':'active','type':'stickers'}); var recent = API.messages.getRecentStickers(); return {'sticker_pack': pack, 'recent': recent};")
                        .map(extractResponseWithErrorHandling()));
    }
}