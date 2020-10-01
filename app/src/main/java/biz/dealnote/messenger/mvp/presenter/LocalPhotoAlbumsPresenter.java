package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.mvp.presenter.base.RxSupportPresenter;
import biz.dealnote.messenger.mvp.view.ILocalPhotoAlbumsView;
import biz.dealnote.messenger.util.Analytics;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Utils.isEmpty;


public class LocalPhotoAlbumsPresenter extends RxSupportPresenter<ILocalPhotoAlbumsView> {

    private final List<LocalImageAlbum> mLocalImageAlbums;
    private final List<LocalImageAlbum> mLocalImageAlbums_Search;
    private boolean permissionRequestedOnce;
    private boolean mLoadingNow;
    private String q;

    public LocalPhotoAlbumsPresenter(@Nullable Bundle savedInstanceState) {
        super(savedInstanceState);
        mLocalImageAlbums = new ArrayList<>();
        mLocalImageAlbums_Search = new ArrayList<>();
    }

    public void fireSearchRequestChanged(String q) {
        String query = q == null ? null : q.trim();

        if (Objects.safeEquals(q, this.q)) {
            return;
        }
        this.q = query;
        mLocalImageAlbums_Search.clear();
        for (LocalImageAlbum i : mLocalImageAlbums) {
            if (isEmpty(i.getName())) {
                continue;
            }
            if (i.getName().toLowerCase().contains(q.toLowerCase())) {
                mLocalImageAlbums_Search.add(i);
            }
        }

        if (!isEmpty(q))
            callView(v -> v.displayData(mLocalImageAlbums_Search));
        else
            callView(v -> v.displayData(mLocalImageAlbums));
    }

    @Override
    public void onGuiCreated(@NonNull ILocalPhotoAlbumsView viewHost) {
        super.onGuiCreated(viewHost);

        if (!AppPerms.hasReadStoragePermision(getApplicationContext())) {
            if (!permissionRequestedOnce) {
                permissionRequestedOnce = true;
                getView().requestReadExternalStoragePermission();
            }
        } else {
            loadData();
        }

        getView().displayData(mLocalImageAlbums);
        resolveProgressView();
        resolveEmptyTextView();
    }

    private void changeLoadingNowState(boolean loading) {
        mLoadingNow = loading;
        resolveProgressView();
    }

    private void resolveProgressView() {
        if (isGuiReady()) getView().displayProgress(mLoadingNow);
    }

    private void loadData() {
        if (mLoadingNow) return;

        changeLoadingNowState(true);
        appendDisposable(Stores.getInstance()
                .localMedia()
                .getImageAlbums()
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onDataLoaded, this::onLoadError));
    }

    private void onLoadError(Throwable throwable) {
        Analytics.logUnexpectedError(throwable);
        changeLoadingNowState(false);
    }

    private void onDataLoaded(List<LocalImageAlbum> data) {
        changeLoadingNowState(false);
        mLocalImageAlbums.clear();
        mLocalImageAlbums.addAll(data);

        if (isGuiReady()) {
            getView().notifyDataChanged();
        }

        resolveEmptyTextView();
    }

    private void resolveEmptyTextView() {
        if (isGuiReady()) getView().setEmptyTextVisible(Utils.safeIsEmpty(mLocalImageAlbums));
    }

    public void fireRefresh() {
        loadData();
    }

    public void fireAlbumClick(@NonNull LocalImageAlbum album) {
        getView().openAlbum(album);
    }

    public void fireReadExternalStoregePermissionResolved() {
        if (AppPerms.hasReadStoragePermision(getApplicationContext())) {
            loadData();
        }
    }
}
