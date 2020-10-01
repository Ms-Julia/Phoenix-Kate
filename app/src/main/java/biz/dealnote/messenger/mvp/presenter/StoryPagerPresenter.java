package biz.dealnote.messenger.mvp.presenter;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import biz.dealnote.messenger.App;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.media.gif.IGifPlayer;
import biz.dealnote.messenger.media.gif.PlayerPrepareException;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.model.Story;
import biz.dealnote.messenger.model.VideoSize;
import biz.dealnote.messenger.mvp.presenter.base.AccountDependencyPresenter;
import biz.dealnote.messenger.mvp.view.IStoryPagerView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.DownloadWorkUtils;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.reflect.OnGuiCreated;

public class StoryPagerPresenter extends AccountDependencyPresenter<IStoryPagerView> implements IGifPlayer.IStatusChangeListener, IGifPlayer.IVideoSizeChangeListener {

    private static final String SAVE_PAGER_INDEX = "save_pager_index";
    private static final VideoSize DEF_SIZE = new VideoSize(1, 1);
    private final ArrayList<Story> mStories;
    private final Context context;
    private IGifPlayer mGifPlayer;
    private int mCurrentIndex;

    public StoryPagerPresenter(int accountId, @NonNull ArrayList<Story> stories, int index, Context context, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        this.context = context;
        mStories = stories;

        if (savedInstanceState == null) {
            mCurrentIndex = index;
        } else {
            mCurrentIndex = savedInstanceState.getInt(SAVE_PAGER_INDEX);
        }
        initGifPlayer();
    }

    public boolean isStoryIsVideo(int pos) {
        return mStories.get(pos).getPhoto() == null && mStories.get(pos).getVideo() != null;
    }

    public Story getStory(int pos) {
        return mStories.get(pos);
    }

    @Override
    public void saveState(@NonNull Bundle outState) {
        super.saveState(outState);
        outState.putInt(SAVE_PAGER_INDEX, mCurrentIndex);
    }

    @OnGuiCreated
    private void resolveData() {
        if (isGuiReady()) {
            getView().displayData(mStories.size(), mCurrentIndex);
        }
    }

    public void fireSurfaceCreated(int adapterPosition) {
        if (mCurrentIndex == adapterPosition) {
            resolvePlayerDisplay();
        }
    }

    @OnGuiCreated
    private void resolveToolbarTitle() {
        if (isGuiReady()) {
            getView().setToolbarTitle(R.string.image_number, mCurrentIndex + 1, mStories.size());
        }
    }

    @OnGuiCreated
    private void resolvePlayerDisplay() {
        if (isGuiReady()) {
            getView().attachDisplayToPlayer(mCurrentIndex, mGifPlayer);
        } else {
            mGifPlayer.setDisplay(null);
        }
    }

    private void initGifPlayer() {
        if (Objects.nonNull(mGifPlayer)) {
            IGifPlayer old = mGifPlayer;
            mGifPlayer = null;
            old.release();
        }

        Story story = mStories.get(mCurrentIndex);
        AssertUtils.requireNonNull(story);

        if (story.getVideo() == null) {
            return;
        }

        String url = Utils.firstNonEmptyString(story.getVideo().getMp4link1080(), story.getVideo().getMp4link720(), story.getVideo().getMp4link480(),
                story.getVideo().getMp4link360(), story.getVideo().getMp4link240());
        if (url == null) {
            safeShowError(getView(), R.string.unable_to_play_file);
            return;
        }

        mGifPlayer = Injection.provideGifPlayerFactory().createGifPlayer(url, false);
        mGifPlayer.addStatusChangeListener(this);
        mGifPlayer.addVideoSizeChangeListener(this);

        try {
            mGifPlayer.play();
        } catch (PlayerPrepareException e) {
            safeShowError(getView(), R.string.unable_to_play_file);
        }
    }

    private void selectPage(int position) {
        if (mCurrentIndex == position) {
            return;
        }

        mCurrentIndex = position;
        initGifPlayer();
    }

    private boolean isMy() {
        return mStories.get(mCurrentIndex).getOwnerId() == getAccountId();
    }

    @OnGuiCreated
    private void resolveAspectRatio() {
        if (isGuiReady()) {
            VideoSize size = mGifPlayer.getVideoSize();
            if (size != null) {
                getView().setAspectRatioAt(mCurrentIndex, size.getWidth(), size.getHeight());
            }
        }
    }

    @OnGuiCreated
    private void resolvePreparingProgress() {
        boolean preparing = !Objects.isNull(mGifPlayer) && mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING;
        if (isGuiReady()) {
            getView().setPreparingProgressVisible(mCurrentIndex, preparing);
        }
    }

    @OnGuiCreated
    private void resolveToolbarSubtitle() {
        if (isGuiReady()) {
            getView().setToolbarSubtitle(mStories.get(mCurrentIndex), getAccountId());
        }
    }

    public void firePageSelected(int position) {
        if (mCurrentIndex == position) {
            return;
        }

        selectPage(position);
        resolveToolbarTitle();
        resolveToolbarSubtitle();
        resolvePreparingProgress();
    }

    public void fireHolderCreate(int adapterPosition) {
        if (!isStoryIsVideo(adapterPosition))
            return;
        boolean isProgress = adapterPosition == mCurrentIndex && (mGifPlayer == null || mGifPlayer.getPlayerStatus() == IGifPlayer.IStatus.PREPARING);

        VideoSize size = mGifPlayer == null ? null : mGifPlayer.getVideoSize();
        if (Objects.isNull(size)) {
            size = DEF_SIZE;
        }

        getView().configHolder(adapterPosition, isProgress, size.getWidth(), size.getWidth());
    }

    public void fireDownloadButtonClick() {
        if (!AppPerms.hasWriteStoragePermision(App.getInstance())) {
            getView().requestWriteExternalStoragePermission();
            return;
        }

        downloadImpl();
    }

    private void onWritePermissionResolved() {
        if (AppPerms.hasWriteStoragePermision(App.getInstance())) {
            downloadImpl();
        }
    }

    public final void fireWritePermissionResolved() {
        onWritePermissionResolved();
    }

    @Override
    public void onGuiPaused() {
        super.onGuiPaused();
        if (Objects.nonNull(mGifPlayer)) {
            mGifPlayer.pause();
        }
    }

    @Override
    public void onGuiResumed() {
        super.onGuiResumed();

        if (Objects.nonNull(mGifPlayer)) {
            try {
                mGifPlayer.play();
            } catch (PlayerPrepareException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onDestroyed() {
        if (Objects.nonNull(mGifPlayer)) {
            mGifPlayer.release();
        }
        super.onDestroyed();
    }

    private void downloadImpl() {
        Story story = mStories.get(mCurrentIndex);
        if (story.getPhoto() != null)
            doSaveOnDrive(story);
        if (story.getVideo() != null) {
            String url = Utils.firstNonEmptyString(story.getVideo().getMp4link1080(), story.getVideo().getMp4link720(), story.getVideo().getMp4link480(),
                    story.getVideo().getMp4link360(), story.getVideo().getMp4link240());
            story.getVideo().setTitle(story.getOwner().getFullName());
            if (!Utils.isEmpty(url)) {
                DownloadWorkUtils.doDownloadVideo(App.getInstance(), story.getVideo(), url, "Story");
            }
        }
    }

    private void doSaveOnDrive(Story photo) {
        File dir = new File(Settings.get().other().getPhotoDir());
        if (!dir.isDirectory()) {
            boolean created = dir.mkdirs();
            if (!created) {
                safeShowError(getView(), "Can't create directory " + dir);
                return;
            }
        } else
            dir.setLastModified(Calendar.getInstance().getTime().getTime());

        DownloadResult(DownloadWorkUtils.makeLegalFilename(photo.getOwner().getFullName(), null), dir, photo.getPhoto());
    }

    private String transform_owner(int owner_id) {
        if (owner_id < 0)
            return "club" + Math.abs(owner_id);
        else
            return "id" + owner_id;
    }

    private void DownloadResult(String Prefix, File dir, Photo photo) {
        if (Prefix != null && Settings.get().other().isPhoto_to_user_dir()) {
            File dir_final = new File(dir.getAbsolutePath() + "/" + Prefix);
            if (!dir_final.isDirectory()) {
                boolean created = dir_final.mkdirs();
                if (!created) {
                    safeShowError(getView(), "Can't create directory " + dir_final);
                    return;
                }
            } else
                dir_final.setLastModified(Calendar.getInstance().getTime().getTime());
            dir = dir_final;
        }
        String url = photo.getUrlForSize(PhotoSize.W, true);
        DownloadWorkUtils.doDownloadPhoto(context, url, dir.getAbsolutePath(), (Prefix != null ? (Prefix + "_") : "") + transform_owner(photo.getOwnerId()) + "_" + photo.getId());
    }

    @Override
    public void onPlayerStatusChange(@NonNull IGifPlayer player, int previousStatus, int currentStatus) {
        if (mGifPlayer == player) {
            resolvePreparingProgress();
            resolvePlayerDisplay();
        }
    }

    @Override
    public void onVideoSizeChanged(@NonNull IGifPlayer player, VideoSize size) {
        if (mGifPlayer == player) {
            resolveAspectRatio();
        }
    }
}
