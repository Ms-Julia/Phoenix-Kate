package biz.dealnote.messenger.mvp.presenter;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IAudiosView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.DownloadWorkUtils;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;

import static biz.dealnote.messenger.util.Utils.getCauseIfRuntime;

public class AudiosPresenter extends AccountDependencyPresenter<IAudiosView> {

    private static final int GET_COUNT = 100;
    private static final int REC_COUNT = 1000;
    private final IAudioInteractor audioInteractor;
    private final ArrayList<Audio> audios;
    private final int ownerId;
    private final int option_menu_id;
    private final int isAlbum;
    private final boolean iSSelectMode;
    private final String accessKey;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private boolean actualReceived;
    private List<AudioPlaylist> Curr;
    private boolean loadingNow;
    private boolean endOfContent;

    public AudiosPresenter(int accountId, int ownerId, int option_menu_id, int isAlbum, boolean iSSelectMode, String accessKey, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        audioInteractor = InteractorFactory.createAudioInteractor();
        audios = new ArrayList<>();
        this.ownerId = ownerId;
        this.option_menu_id = option_menu_id;
        this.isAlbum = isAlbum;
        this.iSSelectMode = iSSelectMode;
        this.accessKey = accessKey;
    }

    public void LoadAudiosTool() {
        if (audios.isEmpty()) {
            if (!iSSelectMode && isAlbum == 0 && option_menu_id == -1 && MusicUtils.Audios.containsKey(ownerId)) {
                audios.addAll(Objects.requireNonNull(MusicUtils.Audios.get(ownerId)));
                actualReceived = true;
                setLoadingNow(false);
                callView(IAudiosView::notifyListChanged);
            } else
                fireRefresh();
        }
    }

    private void loadedPlaylist(AudioPlaylist t) {
        List<AudioPlaylist> ret = new ArrayList<>(1);
        ret.add(t);
        Objects.requireNonNull(getView()).updatePlaylists(ret);
        Curr = ret;
    }

    public boolean isMyAudio() {
        return isAlbum == 0 && option_menu_id == -1 && ownerId == getAccountId();
    }

    public void setLoadingNow(boolean loadingNow) {
        this.loadingNow = loadingNow;
        resolveRefreshingView();
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

    private void requestNext() {
        setLoadingNow(true);
        int offset = audios.size();
        if (isAlbum == 0 && option_menu_id == -1)
            requestList(offset, null);
        else if (isAlbum == 1)
            requestList(offset, option_menu_id);
    }

    public void requestList(int offset, Integer album_id) {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.get(getAccountId(), album_id, ownerId, offset, GET_COUNT, accessKey)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(offset == 0 ? this::onListReceived : this::onNextListReceived, this::onListGetError));
    }

    private void onNextListReceived(List<Audio> next) {
        int startOwnSize = audios.size();
        audios.addAll(next);
        endOfContent = next.isEmpty();
        setLoadingNow(false);
        callView(view -> view.notifyDataAdded(startOwnSize, next.size()));
        if (isAlbum == 0 && option_menu_id == -1 && !iSSelectMode) {
            MusicUtils.Audios.put(ownerId, audios);
        }
    }

    private void onListReceived(List<Audio> data) {
        audios.clear();
        audios.addAll(data);
        endOfContent = data.isEmpty();
        actualReceived = true;
        setLoadingNow(false);
        callView(IAudiosView::notifyListChanged);

        if (isAlbum == 0 && option_menu_id == -1 && !iSSelectMode) {
            MusicUtils.Audios.put(ownerId, audios);
        }
    }

    private void onEndlessListReceived(List<Audio> data) {
        audios.clear();
        audios.addAll(data);
        endOfContent = true;
        actualReceived = true;
        setLoadingNow(false);
        callView(IAudiosView::notifyListChanged);
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

    public void getListByGenre(boolean foreign, int genre) {
        setLoadingNow(true);
        audioListDisposable.add(audioInteractor.getPopular(getAccountId(), foreign ? 1 : 0, genre, REC_COUNT)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(this::onEndlessListReceived, this::onListGetError));
    }

    public void getRecommendations() {
        setLoadingNow(true);
        if (isAlbum == 2) {
            audioListDisposable.add(audioInteractor.getRecommendationsByAudio(getAccountId(), ownerId + "_" + option_menu_id, REC_COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onEndlessListReceived));
        } else {
            audioListDisposable.add(audioInteractor.getRecommendations(getAccountId(), ownerId, REC_COUNT)
                    .compose(RxUtils.applySingleIOToMainSchedulers())
                    .subscribe(this::onEndlessListReceived, this::onListGetError));
        }
    }

    @Override
    public void onDestroyed() {
        audioListDisposable.dispose();
        super.onDestroyed();
    }

    private void onListGetError(Throwable t) {
        setLoadingNow(false);

        if (ownerId != getAccountId()) {
            showError(getView(), getCauseIfRuntime(t));
            return;
        }
        if (isGuiResumed()) {
            showError(getView(), getCauseIfRuntime(t));
        }
    }

    public ArrayList<Audio> getSelected(boolean noDownloaded) {
        ArrayList<Audio> ret = new ArrayList<>();
        for (Audio i : audios) {
            if (i.isSelected()) {
                if (noDownloaded) {
                    if (DownloadWorkUtils.TrackIsDownloaded(i) == 0 && !Utils.isEmpty(i.getUrl()) && !i.getUrl().contains("file://") && !i.getUrl().contains("content://")) {
                        ret.add(i);
                    }
                } else {
                    ret.add(i);
                }
            }
        }
        return ret;
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

    public void fireUpdateSelectMode() {
        for (Audio i : audios) {
            if (i.isSelected()) {
                i.setIsSelected(false);
            }
        }
        callView(IAudiosView::notifyListChanged);
    }

    public void fireRefresh() {
        audioListDisposable.clear();
        if (isAlbum == 0 && option_menu_id == -1) {
            requestList(0, null);
        } else if (isAlbum == 0 && option_menu_id != -2) {
            getListByGenre(false, option_menu_id);
        } else if (isAlbum == 0 || isAlbum == 2) {
            getRecommendations();
        } else {
            if (isAlbum == 1) {
                audioListDisposable.add(audioInteractor.getPlaylistById(getAccountId(), option_menu_id, ownerId, accessKey)
                        .compose(RxUtils.applySingleIOToMainSchedulers())
                        .subscribe(this::loadedPlaylist, t -> showError(getView(), getCauseIfRuntime(t))));
            }
            requestList(0, option_menu_id);
        }
    }

    public void onDelete(AudioPlaylist album) {
        int accountId = getAccountId();
        audioListDisposable.add(audioInteractor.deletePlaylist(accountId, album.getId(), album.getOwnerId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getPhoenixToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    public void onAdd(AudioPlaylist album) {
        int accountId = getAccountId();
        audioListDisposable.add(audioInteractor.followPlaylist(accountId, album.getId(), album.getOwnerId(), album.getAccess_key())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(data -> getView().getPhoenixToast().showToast(R.string.success), throwable ->
                        showError(getView(), throwable)));
    }

    public void fireScrollToEnd() {
        if (actualReceived && !endOfContent) {
            requestNext();
        }
    }

    public void fireEditTrackIn(Context context, Audio audio) {
        audioListDisposable.add(audioInteractor.getLyrics(Settings.get().accounts().getCurrent(), audio.getLyricsId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> fireEditTrack(context, audio, t), v -> fireEditTrack(context, audio, null)));
    }

    public void fireEditTrack(Context context, Audio audio, String lyrics) {
        View root = View.inflate(context, R.layout.entry_audio_info, null);
        ((TextInputEditText) root.findViewById(R.id.edit_artist)).setText(audio.getArtist());
        ((TextInputEditText) root.findViewById(R.id.edit_title)).setText(audio.getTitle());
        ((TextInputEditText) root.findViewById(R.id.edit_lyrics)).setText(lyrics);
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(context)
                .setTitle(R.string.enter_audio_info)
                .setCancelable(true)
                .setView(root)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> audioListDisposable.add(audioInteractor.edit(getAccountId(), audio.getOwnerId(), audio.getId(),
                        ((TextInputEditText) root.findViewById(R.id.edit_artist)).getText().toString(), ((TextInputEditText) root.findViewById(R.id.edit_title)).getText().toString(),
                        ((TextInputEditText) root.findViewById(R.id.edit_lyrics)).getText().toString()).compose(RxUtils.applyCompletableIOToMainSchedulers())
                        .subscribe(this::fireRefresh, t -> showError(getView(), getCauseIfRuntime(t)))))
                .setNegativeButton(R.string.button_cancel, null);
        builder.create().show();
    }

    @Override
    public void onGuiCreated(@NonNull IAudiosView view) {
        super.onGuiCreated(view);
        view.displayList(audios);
        if (Curr != null)
            Objects.requireNonNull(getView()).updatePlaylists(Curr);
    }

}