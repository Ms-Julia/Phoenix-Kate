package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.domain.IOwnersRepository;
import biz.dealnote.messenger.domain.IPhotosInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoAlbum;
import biz.dealnote.messenger.model.wrappers.SelectablePhotoWrapper;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IVkPhotosView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.IUploadManager;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadResult;
import biz.dealnote.messenger.upload.UploadUtils;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.findIndexById;
import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;
import static biz.dealnote.messenger.util.Utils.nonEmpty;


public class VkPhotosPresenter extends AccountDependencyPresenter<IVkPhotosView> {

    private static final String SAVE_ALBUM = "save-album";
    private static final String SAVE_OWNER = "save-owner";
    private static final int COUNT = 100;

    private final int ownerId;
    private final int albumId;

    private final IPhotosInteractor interactor;
    private final IOwnersRepository ownersRepository;
    private final IUploadManager uploadManager;

    private final List<SelectablePhotoWrapper> photos;
    private final List<Upload> uploads;

    private final UploadDestination destination;
    private final String action;
    private final CompositeDisposable cacheDisposable = new CompositeDisposable();
    private PhotoAlbum album;
    private Owner owner;
    private List<String> mDownloads;
    private boolean requestNow;
    private boolean endOfContent;

    public VkPhotosPresenter(int accountId, int ownerId, int albumId, String action,
                             @Nullable Owner owner, @Nullable PhotoAlbum album, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);

        this.ownerId = ownerId;
        this.albumId = albumId;
        this.action = action;

        interactor = InteractorFactory.createPhotosInteractor();
        ownersRepository = Repository.INSTANCE.getOwners();
        uploadManager = Injection.provideUploadManager();

        destination = UploadDestination.forPhotoAlbum(albumId, ownerId);

        photos = new ArrayList<>();
        uploads = new ArrayList<>();

        if (isNull(savedInstanceState)) {
            this.album = album;
            this.owner = owner;
        } else {
            this.album = savedInstanceState.getParcelable(SAVE_ALBUM);
            ParcelableOwnerWrapper ownerWrapper = savedInstanceState.getParcelable(SAVE_OWNER);
            AssertUtils.requireNonNull(ownerWrapper);
            this.owner = ownerWrapper.get();
        }

        loadInitialData();

        appendDisposable(uploadManager.observeAdding()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadQueueAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadsRemoved));

        appendDisposable(uploadManager.observeResults()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(Injection.provideMainThreadScheduler())
                .subscribe(this::onUploadProgressUpdate));

        refreshOwnerInfoIfNeed();
        refreshAlbumInfoIfNeed();
    }

    private static List<SelectablePhotoWrapper> wrappersOf(List<Photo> photos) {
        List<SelectablePhotoWrapper> wrappers = new ArrayList<>(photos.size());
        for (Photo photo : photos) {
            wrappers.add(new SelectablePhotoWrapper(photo));
        }
        return wrappers;
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putParcelable(SAVE_ALBUM, album);
        outState.putParcelable(SAVE_OWNER, new ParcelableOwnerWrapper(owner));
    }

    private void refreshOwnerInfoIfNeed() {
        int accountId = getAccountId();

        if (!isMy() && isNull(owner)) {
            appendDisposable(ownersRepository.getBaseOwnerInfo(accountId, ownerId, IOwnersRepository.MODE_NET)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onActualOwnerInfoReceived, RxUtils.ignore()));
        }
    }

    private void refreshAlbumInfoIfNeed() {
        int accountId = getAccountId();

        if (isNull(album)) {
            appendDisposable(interactor.getAlbumById(accountId, ownerId, albumId)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onAlbumInfoReceived, RxUtils.ignore()));
        }
    }

    private void onAlbumInfoReceived(PhotoAlbum album) {
        this.album = album;

        resolveToolbarView();

        if (!isSelectionMode()) {
            resolveButtonAddVisibility(true);
        }
    }

    private void onActualOwnerInfoReceived(Owner owner) {
        this.owner = owner;
        resolveButtonAddVisibility(true);
    }

    @OnGuiCreated
    private void resolveToolbarView() {
        if (isGuiReady()) {
            String ownerName = nonNull(owner) ? owner.getFullName() : null;
            String albumTitle = nonNull(album) ? album.getTitle() : "";

            getView().setToolbarSubtitle(albumTitle + " " + getString(R.string.photos_count, photos.size()));

            if (nonEmpty(ownerName)) {
                getView().setToolbarTitle(ownerName);
            } else {
                getView().displayDefaultToolbarTitle();
            }
        }
    }

    private void onUploadQueueAdded(List<Upload> added) {
        int startUploadSize = uploads.size();
        int count = 0;

        for (Upload upload : added) {
            if (destination.compareTo(upload.getDestination())) {
                uploads.add(upload);
                count++;
            }
        }

        if (count > 0) {
            int finalCount = count;
            callView(view -> view.notifyUploadAdded(startUploadSize, finalCount));
        }
    }

    private void onUploadsRemoved(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploads, id);

            if (index != -1) {
                uploads.remove(index);
                callView(view -> view.notifyUploadRemoved(index));
            }
        }
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        if (destination.compareTo(pair.getFirst().getDestination())) {
            Photo photo = (Photo) pair.getSecond().getResult();
            photos.add(0, new SelectablePhotoWrapper(photo));
            callView(view -> view.notifyPhotosAdded(0, 1));
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploads, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadProgressUpdate(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploads, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(update.getId(), update.getProgress()));
            }
        }
    }

    @Override
    public void onGuiCreated(@NonNull IVkPhotosView view) {
        super.onGuiCreated(view);
        view.displayData(photos, uploads);
        resolveButtonAddVisibility(false);
    }

    private void setRequestNow(boolean requestNow) {
        this.requestNow = requestNow;
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(requestNow);
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
        getView().setDrawerPhotosSelected(isMy());
    }

    private void requestActualData(int offset) {
        setRequestNow(true);
        if (albumId != -9001 && albumId != -9000) {
            appendDisposable(interactor.get(getAccountId(), ownerId, albumId, COUNT, offset, true)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        } else if (albumId == -9000) {
            appendDisposable(interactor.getUsersPhoto(getAccountId(), ownerId, 1, offset, COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        } else if (albumId == -9001) {
            appendDisposable(interactor.getAll(getAccountId(), ownerId, 1, 1, offset, COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(photos -> onActualPhotosReceived(offset, photos), this::onActualDataGetError));
        }
    }

    private void onActualDataGetError(Throwable t) {
        showError(getView(), getCauseIfRuntime(t));
        setRequestNow(false);
    }

    private void onActualPhotosReceived(int offset, List<Photo> data) {
        cacheDisposable.clear();
        endOfContent = data.isEmpty();

        setRequestNow(false);

        if (offset == 0) {
            photos.clear();

            if (Utils.isEmpty(mDownloads)) {
                photos.addAll(wrappersOf(data));
            } else {
                for (Photo i : data) {
                    photos.add(new SelectablePhotoWrapper(i).setDownloaded(existPhoto(i)));
                }
            }
            callView(IVkPhotosView::notifyDataSetChanged);
        } else {
            int startSize = photos.size();

            if (Utils.isEmpty(mDownloads)) {
                photos.addAll(wrappersOf(data));
            } else {
                for (Photo i : data) {
                    photos.add(new SelectablePhotoWrapper(i).setDownloaded(existPhoto(i)));
                }
            }
            callView(view -> view.notifyPhotosAdded(startSize, data.size()));
        }
        resolveToolbarView();
    }

    private void loadInitialData() {
        int accountId = getAccountId();
        cacheDisposable.add(interactor.getAllCachedData(accountId, ownerId, albumId)
                .zipWith(uploadManager.get(getAccountId(), destination), Pair.Companion::create)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onInitialDataReceived));
    }

    private String transform_owner(int owner_id) {
        if (owner_id < 0)
            return "club" + Math.abs(owner_id);
        else
            return "id" + owner_id;
    }

    private boolean existPhoto(Photo photo) {
        for (String i : mDownloads) {
            if (i.contains(transform_owner(photo.getOwnerId()) + "_" + photo.getId())) {
                return true;
            }
        }
        return false;
    }

    private void onInitialDataReceived(Pair<List<Photo>, List<Upload>> data) {
        photos.clear();
        if (Utils.isEmpty(mDownloads)) {
            photos.addAll(wrappersOf(data.getFirst()));
        } else {
            for (Photo i : data.getFirst()) {
                photos.add(new SelectablePhotoWrapper(i).setDownloaded(existPhoto(i)));
            }
        }

        uploads.clear();
        uploads.addAll(data.getSecond());

        callView(IVkPhotosView::notifyDataSetChanged);
        resolveToolbarView();

        requestActualData(0);
    }

    @Override
    public void onDestroyed() {
        cacheDisposable.dispose();
        super.onDestroyed();
    }

    public void fireUploadRemoveClick(Upload o) {
        uploadManager.cancel(o.getId());
    }

    public void fireRefresh() {
        if (!requestNow) {
            requestActualData(0);
        }
    }

    public void fireScrollToEnd() {
        if (!requestNow && nonEmpty(photos) && !endOfContent) {
            requestActualData(photos.size());
        }
    }

    private boolean isMy() {
        return getAccountId() == ownerId;
    }

    private boolean isAdmin() {
        return owner instanceof Community && ((Community) owner).getAdminLevel() >= VKApiCommunity.AdminLevel.MODERATOR;
    }

    private boolean canUploadToAlbum() {
        // можно загружать,
        // 1 - альбом не системный ОБЯЗАТЕЛЬНО
        // 2 - если я админ группы
        // 3 - если альбом мой
        // 4 - если альбом принадлежит группе, но разрешено в него грузить
        return albumId >= 0 && (isAdmin() || isMy() || (nonNull(album) && album.isCanUpload()));
    }

    public void firePhotosForUploadSelected(List<LocalPhoto> photos, int size) {
        List<UploadIntent> intents = UploadUtils.createIntents(getAccountId(), destination, photos, size, true);
        uploadManager.enqueue(intents);
    }

    public void firePhotoSelectionChanged(SelectablePhotoWrapper wrapper) {
        wrapper.setSelected(!wrapper.isSelected());
        onPhotoSelected(wrapper);
    }

    private void onPhotoSelected(SelectablePhotoWrapper selectedPhoto) {
        if (selectedPhoto.isSelected()) {
            int targetIndex = 1;
            for (SelectablePhotoWrapper photo : photos) {
                if (photo.getIndex() >= targetIndex) {
                    targetIndex = photo.getIndex() + 1;
                }
            }

            selectedPhoto.setIndex(targetIndex);
        } else {
            for (int i = 0; i < photos.size(); i++) {
                SelectablePhotoWrapper photo = photos.get(i);
                if (photo.getIndex() > selectedPhoto.getIndex()) {
                    photo.setIndex(photo.getIndex() - 1);
                }
            }

            selectedPhoto.setIndex(0);
        }

        if (selectedPhoto.isSelected()) {
            getView().setButtonAddVisible(true, true);
        } else {
            resolveButtonAddVisibility(true);
        }
    }

    private boolean isSelectionMode() {
        return IVkPhotosView.ACTION_SELECT_PHOTOS.equals(action);
    }

    private void resolveButtonAddVisibility(boolean anim) {
        if (isGuiReady()) {
            if (isSelectionMode()) {
                boolean hasSelected = false;
                for (SelectablePhotoWrapper wrapper : photos) {
                    if (wrapper.isSelected()) {
                        hasSelected = true;
                        break;
                    }
                }

                getView().setButtonAddVisible(hasSelected, anim);
            } else {
                getView().setButtonAddVisible(canUploadToAlbum(), anim);
            }
        }
    }

    public void firePhotoClick(SelectablePhotoWrapper wrapper) {
        int Index = 0;
        boolean trig = false;
        ArrayList<Photo> photos_ret = new ArrayList<>(photos.size());
        for (int i = 0; i < photos.size(); i++) {
            SelectablePhotoWrapper photo = photos.get(i);
            photos_ret.add(photo.getPhoto());
            if (!trig && photo.getPhoto().getId() == wrapper.getPhoto().getId() && photo.getPhoto().getOwnerId() == wrapper.getPhoto().getOwnerId()) {
                Index = i;
                trig = true;
            }
        }
        getView().displayGallery(getAccountId(), albumId, ownerId, photos_ret, Index);
    }

    public void fireSelectionCommitClick() {
        List<Photo> selected = getSelected();

        if (nonEmpty(selected)) {
            getView().returnSelectionToParent(selected);
        } else {
            getView().showSelectPhotosToast();
        }
    }

    private List<SelectablePhotoWrapper> getSelectedWrappers() {
        List<SelectablePhotoWrapper> result = Utils.getSelected(photos);
        Collections.sort(result);
        return result;
    }

    private List<Photo> getSelected() {
        List<SelectablePhotoWrapper> wrappers = getSelectedWrappers();
        List<Photo> photos = new ArrayList<>(wrappers.size());
        for (SelectablePhotoWrapper wrapper : wrappers) {
            photos.add(wrapper.getPhoto());
        }

        return photos;
    }

    public void fireAddPhotosClick() {
        if (canUploadToAlbum()) {
            getView().startLocalPhotosSelection();
        }
    }

    public void fireReadStoragePermissionChanged() {
        getView().startLocalPhotosSelectionIfHasPermission();
    }

    private void loadDownloadPath(String Path) {
        File temp = new File(Path);
        if (!temp.exists())
            return;
        File[] file_list = temp.listFiles();
        if (file_list == null || file_list.length <= 0)
            return;
        for (File u : file_list) {
            if (u.isFile())
                mDownloads.add(u.getName());
            else if (u.isDirectory()) {
                loadDownloadPath(u.getAbsolutePath());
            }
        }
    }

    public void loadDownload() {
        setRequestNow(true);
        appendDisposable(loadLocalImages()
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe(this::onCacheLoaded, t -> {/*TODO*/}));
    }

    private void onCacheLoaded() {
        for (SelectablePhotoWrapper i : photos) {
            i.setDownloaded(existPhoto(i.getPhoto()));
        }
        callView(IVkPhotosView::notifyDataSetChanged);
        setRequestNow(false);
    }

    private Completable loadLocalImages() {

        File temp = new File(Settings.get().other().getPhotoDir());
        if (!temp.exists())
            return Completable.complete();
        File[] file_list = temp.listFiles();
        if (file_list == null || file_list.length <= 0)
            return Completable.complete();
        if (mDownloads == null) {
            mDownloads = new ArrayList<>();
        } else {
            mDownloads.clear();
        }
        for (File u : file_list) {
            if (u.isFile())
                mDownloads.add(u.getName());
            else if (u.isDirectory()) {
                loadDownloadPath(u.getAbsolutePath());
            }
        }
        return Completable.complete();
    }
}