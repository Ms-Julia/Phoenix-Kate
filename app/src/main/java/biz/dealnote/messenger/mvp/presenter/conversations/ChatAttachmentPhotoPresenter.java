package biz.dealnote.messenger.mvp.presenter.conversations;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiPhoto;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.db.serialize.Serializers;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;
import biz.dealnote.messenger.mvp.view.conversations.IChatAttachmentPhotosView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.DisposableHolder;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.core.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class ChatAttachmentPhotoPresenter extends BaseChatAttachmentsPresenter<Photo, IChatAttachmentPhotosView> {

    private final DisposableHolder<Void> openGalleryDisposableHolder = new DisposableHolder<>();

    public ChatAttachmentPhotoPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    Single<Pair<String, List<Photo>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, "photo", nextFrom, 50, null)
                .map(response -> {
                    List<Photo> photos = new ArrayList<>();

                    for (AttachmentsHistoryResponse.One one : response.items) {
                        if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiPhoto) {
                            VKApiPhoto dto = (VKApiPhoto) one.entry.attachment;
                            photos.add(Dto2Model.transform(dto));
                        }
                    }

                    return Pair.Companion.create(response.next_from, photos);
                });
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.photos_count, safeCountOf(data)));
        }
    }

    @Override
    public void onDestroyed() {
        openGalleryDisposableHolder.dispose();
        super.onDestroyed();
    }

    @SuppressWarnings("unused")
    public void firePhotoClick(int position, Photo photo) {
        List<Photo> photos = data;

        TmpSource source = new TmpSource(getInstanceId(), 0);

        fireTempDataUsage();

        openGalleryDisposableHolder.append(Stores.getInstance()
                .tempStore()
                .put(source.getOwnerId(), source.getSourceId(), data, Serializers.PHOTOS_SERIALIZER)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(() -> onPhotosSavedToTmpStore(position, source), Analytics::logUnexpectedError));
    }

    private void onPhotosSavedToTmpStore(int index, TmpSource source) {
        callView(view -> view.goToTempPhotosGallery(getAccountId(), source, index));
    }
}