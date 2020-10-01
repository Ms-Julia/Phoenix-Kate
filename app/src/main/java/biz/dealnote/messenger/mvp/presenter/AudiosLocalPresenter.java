package biz.dealnote.messenger.mvp.presenter;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.db.Stores;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IAudiosLocalView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.IUploadManager;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.upload.UploadDestination;
import biz.dealnote.messenger.upload.UploadIntent;
import biz.dealnote.messenger.upload.UploadResult;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.Injection.provideMainThreadScheduler;
import static biz.dealnote.messenger.util.Utils.findIndexById;

public class AudiosLocalPresenter extends AccountDependencyPresenter<IAudiosLocalView> {

    private final ArrayList<Audio> origin_audios;
    private final ArrayList<Audio> audios;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private final IUploadManager uploadManager;
    private final List<Upload> uploadsData;
    private final UploadDestination destination;
    private boolean actualReceived;
    private boolean loadingNow;
    private String query;

    public AudiosLocalPresenter(int accountId, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        destination = UploadDestination.forAudio(accountId);
        uploadManager = Injection.provideUploadManager();
        uploadsData = new ArrayList<>(0);
        audios = new ArrayList<>();
        origin_audios = new ArrayList<>();
    }

    public void LoadAudiosTool() {

        appendDisposable(uploadManager.get(getAccountId(), destination)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onUploadsDataReceived));

        appendDisposable(uploadManager.observeAdding()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadsAdded));

        appendDisposable(uploadManager.observeDeleting(true)
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadDeleted));

        appendDisposable(uploadManager.observeResults()
                .filter(pair -> destination.compareTo(pair.getFirst().getDestination()))
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadResults));

        appendDisposable(uploadManager.obseveStatus()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onUploadStatusUpdate));

        appendDisposable(uploadManager.observeProgress()
                .observeOn(provideMainThreadScheduler())
                .subscribe(this::onProgressUpdates));

        fireRefresh();
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
    }

    public void updateCriteria() {
        setLoadingNow(true);
        audios.clear();
        if (Utils.isEmpty(query)) {
            audios.addAll(origin_audios);
            setLoadingNow(false);
            callView(IAudiosLocalView::notifyListChanged);
            return;
        }
        for (Audio i : origin_audios) {
            if (i.getTitle().toLowerCase().contains(query.toLowerCase()) || i.getArtist().toLowerCase().contains(query.toLowerCase())) {
                audios.add(i);
            }
        }
        setLoadingNow(false);
        callView(IAudiosLocalView::notifyListChanged);
    }

    public void fireQuery(String q) {
        if (Utils.isEmpty(q))
            query = null;
        else {
            query = q;
        }
        updateCriteria();
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();
        resolveRefreshingView();
    }

    private void resolveRefreshingView() {
        if (isGuiResumed()) {
            getView().displayRefreshing(loadingNow);
        }
    }

    public void requestList() {
        setLoadingNow(true);
        audioListDisposable.add(Stores.getInstance()
                .localMedia()
                .getAudios(getAccountId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onListReceived, this::onListGetError));
    }

    private void onListReceived(List<Audio> data) {
        if (Utils.isEmpty(data)) {
            actualReceived = true;
            setLoadingNow(false);
            return;
        }
        origin_audios.clear();
        actualReceived = true;
        origin_audios.addAll(data);
        updateCriteria();
        setLoadingNow(false);
    }

    public void playAudio(Context context, int position) {
        MusicPlaybackService.startForPlayList(context, audios, position, false);
        if (!Settings.get().other().isShow_mini_player())
            PlaceFactory.getPlayerPlace(getAccountId()).tryOpenWith(context);
    }

    public void fireDelete(int position) {
        audios.remove(position);
        callView(v -> v.notifyItemRemoved(position));
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    public void fireRemoveClick(Upload upload) {
        uploadManager.cancel(upload.getId());
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);
        if (isGuiResumed()) {
            showError(getView(), Utils.getCauseIfRuntime(t));
        }
    }

    public void fireFileForUploadSelected(String file) {
        UploadIntent intent = new UploadIntent(getAccountId(), destination)
                .setAutoCommit(true)
                .setFileUri(Uri.parse(file));

        uploadManager.enqueue(Collections.singletonList(intent));
    }

    public int getAudioPos(Audio audio) {
        if (!Utils.isEmpty(audios) && audio != null) {
            int pos = 0;
            for (Audio i : audios) {
                if (i.getId() == audio.getId() && i.getOwnerId() == audio.getOwnerId()) {
                    i.setAnimationNow(true);
                    int finalPos = pos;
                    callView(v -> v.notifyItemChanged(finalPos));
                    return pos;
                }
                pos++;
            }
        }
        return -1;
    }

    public void fireRefresh() {
        audioListDisposable.clear();
        requestList();
    }

    public void fireScrollToEnd() {
        if (actualReceived) {
            requestList();
        }
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosLocalView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
        view.displayUploads(uploadsData);
    }

    private void onUploadsDataReceived(List<Upload> data) {
        uploadsData.clear();
        uploadsData.addAll(data);

        resolveUploadDataVisiblity();
    }

    private void onUploadResults(Pair<Upload, UploadResult<?>> pair) {
        Audio obj = (Audio) pair.getSecond().getResult();
        if (obj.getId() == 0)
            getView().getPhoenixToast().showToastError(R.string.error);
        else {
            getView().getPhoenixToast().showToast(R.string.uploaded);
        }

    }

    private void onProgressUpdates(List<IUploadManager.IProgressUpdate> updates) {
        for (IUploadManager.IProgressUpdate update : updates) {
            int index = findIndexById(uploadsData, update.getId());
            if (index != -1) {
                callView(view -> view.notifyUploadProgressChanged(index, update.getProgress(), true));
            }
        }
    }

    private void onUploadStatusUpdate(Upload upload) {
        int index = findIndexById(uploadsData, upload.getId());
        if (index != -1) {
            callView(view -> view.notifyUploadItemChanged(index));
        }
    }

    private void onUploadsAdded(List<Upload> added) {
        for (Upload u : added) {
            if (destination.compareTo(u.getDestination())) {
                int index = uploadsData.size();
                uploadsData.add(u);
                callView(view -> view.notifyUploadItemsAdded(index, 1));
            }
        }

        resolveUploadDataVisiblity();
    }

    private void onUploadDeleted(int[] ids) {
        for (int id : ids) {
            int index = findIndexById(uploadsData, id);
            if (index != -1) {
                uploadsData.remove(index);
                callView(view -> view.notifyUploadItemRemoved(index));
            }
        }

        resolveUploadDataVisiblity();
    }

    @OnGuiCreated
    private void resolveUploadDataVisiblity() {
        if (isGuiReady()) {
            getView().setUploadDataVisible(!uploadsData.isEmpty());
        }
    }

}
