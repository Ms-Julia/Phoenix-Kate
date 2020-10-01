package biz.dealnote.messenger.api.interfaces;

import biz.dealnote.messenger.api.model.VKApiWikiPage;
import io.reactivex.rxjava3.core.Single;


public interface IPagesApi {

    Single<VKApiWikiPage> get(int ownerId, int pageId, Boolean global, Boolean sitePreview,
                              String title, Boolean needSource, Boolean needHtml);

}
