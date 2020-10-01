package biz.dealnote.messenger.mvp.view.conversations;

import androidx.annotation.NonNull;

import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.TmpSource;


public interface IChatAttachmentPhotosView extends IBaseChatAttachmentsView<Photo> {
    void goToTempPhotosGallery(int accountId, @NonNull TmpSource source, int index);
}