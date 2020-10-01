package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;

import java.util.List;

import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.mvp.core.IMvpView;


public interface IVideosListView extends IAccountDependencyView, IMvpView, IToolbarView, IErrorView {

    String ACTION_SELECT = "VideosFragment.ACTION_SELECT";
    String ACTION_SHOW = "VideosFragment.ACTION_SHOW";

    void displayData(@NonNull List<Video> data);

    void notifyDataAdded(int position, int count);

    void displayLoading(boolean loading);

    void notifyDataSetChanged();

    void returnSelectionToParent(Video video);

    void showVideoPreview(int accountId, Video video);

    void notifyUploadItemsAdded(int position, int count);

    void notifyUploadItemRemoved(int position);

    void notifyUploadItemChanged(int position);

    void notifyUploadProgressChanged(int position, int progress, boolean smoothly);

    void setUploadDataVisible(boolean visible);

    void startSelectUploadFileActivity(int accountId);

    void requestReadExternalStoragePermission();

    void displayUploads(List<Upload> data);

    void notifyUploadDataChanged();

    void onUploaded(Video upload);
}
