package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.mvp.core.IMvpView;

public interface IAudiosLocalView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Audio> audios);

    void notifyItemChanged(int index);

    void notifyItemRemoved(int index);

    void notifyListChanged();

    void notifyUploadItemsAdded(int position, int count);

    void notifyUploadItemRemoved(int position);

    void notifyUploadItemChanged(int position);

    void notifyUploadProgressChanged(int position, int progress, boolean smoothly);

    void setUploadDataVisible(boolean visible);

    void displayUploads(List<Upload> data);

    void notifyUploadDataChanged();

    void displayRefreshing(boolean refresing);
}
