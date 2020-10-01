package biz.dealnote.messenger.mvp.view;

import androidx.annotation.Nullable;

import biz.dealnote.messenger.model.Comment;


public interface ICommentEditView extends IBaseAttachmentsEditView, IProgressView {
    void goBackWithResult(@Nullable Comment comment);

    void showConfirmWithoutSavingDialog();

    void goBack();
}
