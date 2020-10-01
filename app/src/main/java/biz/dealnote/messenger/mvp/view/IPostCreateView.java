package biz.dealnote.messenger.mvp.view;

import android.net.Uri;

import androidx.annotation.NonNull;

import java.util.List;


public interface IPostCreateView extends IBasePostEditView, IToolbarView {
    void goBack();

    void displayUploadUriSizeDialog(@NonNull List<Uri> uris);
}