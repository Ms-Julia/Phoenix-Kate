package biz.dealnote.messenger.view;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.media.MediaMetadataRetriever;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.DrawableRes;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.FragmentActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.squareup.picasso3.Transformation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.SendAttachmentsActivity;
import biz.dealnote.messenger.adapter.AttachmentsViewBinder;
import biz.dealnote.messenger.domain.IAudioInteractor;
import biz.dealnote.messenger.domain.InteractorFactory;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.AudioSearchCriteria;
import biz.dealnote.messenger.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import biz.dealnote.messenger.modalbottomsheetdialogfragment.OptionRequest;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.menu.AudioItem;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.picasso.transforms.PolyTransformation;
import biz.dealnote.messenger.picasso.transforms.RoundTransformation;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AppPerms;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.DownloadWorkUtils;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;

import static biz.dealnote.messenger.player.util.MusicUtils.observeServiceBinding;
import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.isEmpty;
import static biz.dealnote.messenger.util.Utils.safeIsEmpty;

public class AudioContainer extends LinearLayout {
    private final Context mContext;
    private final CompositeDisposable audioListDisposable = new CompositeDisposable();
    private final IAudioInteractor mAudioInteractor = InteractorFactory.createAudioInteractor();
    private Disposable mPlayerDisposable = Disposable.disposed();
    private List<Audio> audios = Collections.emptyList();
    private Audio currAudio = MusicUtils.getCurrentAudio();

    public AudioContainer(Context context) {
        super(context);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
    }

    public AudioContainer(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mContext = context;
    }

    @DrawableRes
    private int getAudioCoverSimple() {
        return Settings.get().main().isAudio_round_icon() ? R.drawable.audio_button : R.drawable.audio_button_material;
    }

    private Transformation TransformCover() {
        return Settings.get().main().isAudio_round_icon() ? new RoundTransformation() : new PolyTransformation();
    }

    private void updateAudioStatus(AudioHolder holder, Audio audio) {
        if (!audio.equals(currAudio)) {
            holder.visual.setImageResource(isEmpty(audio.getUrl()) ? R.drawable.audio_died : R.drawable.song);
            holder.play_cover.clearColorFilter();
            return;
        }
        switch (MusicUtils.PlayerStatus()) {
            case 1:
                holder.visual.cancelAnimation();
                holder.visual.setAnimation(R.raw.play_visual);
                Utils.doAnimateLottie(holder.visual, true, 104);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;
            case 2:
                holder.visual.cancelAnimation();
                holder.visual.setAnimation(R.raw.play_visual);
                Utils.doAnimateLottie(holder.visual, false, 104);
                holder.play_cover.setColorFilter(Color.parseColor("#44000000"));
                break;

        }
    }

    private void deleteTrack(int accoutnId, Audio audio) {
        audioListDisposable.add(mAudioInteractor.delete(accoutnId, audio.getId(), audio.getOwnerId()).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> {
        }, ignore -> {
        }));
    }

    private void addTrack(int accountId, Audio audio) {
        audioListDisposable.add(mAudioInteractor.add(accountId, audio, null, null).compose(RxUtils.applyCompletableIOToMainSchedulers()).subscribe(() -> {
        }, ignore -> {
        }));
    }

    private void get_lyrics(Audio audio) {
        audioListDisposable.add(mAudioInteractor.getLyrics(Settings.get().accounts().getCurrent(), audio.getLyricsId())
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(t -> onAudioLyricsRecived(t, audio), t -> {/*TODO*/}));
    }

    private void onAudioLyricsRecived(String Text, Audio audio) {
        String title = audio.getArtistAndTitle();

        MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(mContext);
        dlgAlert.setIcon(R.drawable.dir_song);
        dlgAlert.setMessage(Text);
        dlgAlert.setTitle(title != null ? title : mContext.getString(R.string.get_lyrics));

        ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("response", Text);
        clipboard.setPrimaryClip(clip);

        dlgAlert.setPositiveButton("OK", null);
        dlgAlert.setCancelable(true);
        PhoenixToast.CreatePhoenixToast(mContext).showToast(R.string.copied_to_clipboard);
        dlgAlert.create().show();
    }

    public void dispose() {
        mPlayerDisposable.dispose();
        audios = Collections.emptyList();
    }

    public void displayAudios(ArrayList<Audio> audios, AttachmentsViewBinder.OnAttachmentsActionCallback mAttachmentsActionCallback) {
        setVisibility(safeIsEmpty(audios) ? View.GONE : View.VISIBLE);
        if (safeIsEmpty(audios)) {
            dispose();
            return;
        }
        this.audios = audios;

        int i = audios.size() - getChildCount();
        for (int j = 0; j < i; j++) {
            addView(LayoutInflater.from(mContext).inflate(R.layout.item_audio, this, false));
        }

        for (int g = 0; g < getChildCount(); g++) {
            ViewGroup root = (ViewGroup) getChildAt(g);
            if (g < audios.size()) {
                Audio audio = audios.get(g);

                AudioHolder holder = new AudioHolder(root);

                holder.tvTitle.setText(audio.getArtist());
                holder.tvSubtitle.setText(audio.getTitle());
                holder.quality.setVisibility(View.GONE);

                updateAudioStatus(holder, audio);
                int finalG = g;

                if (Settings.get().other().isShow_audio_cover()) {
                    if (!isEmpty(audio.getThumb_image_little())) {
                        PicassoInstance.with()
                                .load(audio.getThumb_image_little())
                                .placeholder(java.util.Objects.requireNonNull(ResourcesCompat.getDrawable(mContext.getResources(), getAudioCoverSimple(), mContext.getTheme())))
                                .transform(TransformCover())
                                .tag(Constants.PICASSO_TAG)
                                .into(holder.play_cover);
                    } else {
                        PicassoInstance.with().cancelRequest(holder.play_cover);
                        holder.play_cover.setImageResource(getAudioCoverSimple());
                    }
                } else {
                    PicassoInstance.with().cancelRequest(holder.play_cover);
                    holder.play_cover.setImageResource(getAudioCoverSimple());
                }

                holder.ibPlay.setOnLongClickListener(v -> {
                    if (!isEmpty(audio.getThumb_image_very_big())
                            || !isEmpty(audio.getThumb_image_big()) || !isEmpty(audio.getThumb_image_little())) {
                        mAttachmentsActionCallback.onUrlPhotoOpen(firstNonEmptyString(audio.getThumb_image_very_big(),
                                audio.getThumb_image_big(), audio.getThumb_image_little()), audio.getArtist(), audio.getTitle());
                    }
                    return true;
                });

                holder.ibPlay.setOnClickListener(v -> {
                    if (MusicUtils.isNowPlayingOrPreparingOrPaused(audio)) {
                        if (!Settings.get().other().isUse_stop_audio()) {
                            updateAudioStatus(holder, audio);
                            MusicUtils.playOrPause();
                        } else {
                            updateAudioStatus(holder, audio);
                            MusicUtils.stop();
                        }
                    } else {
                        updateAudioStatus(holder, audio);
                        mAttachmentsActionCallback.onAudioPlay(finalG, audios);
                    }
                });
                if (audio.getDuration() <= 0)
                    holder.time.setVisibility(View.INVISIBLE);
                else {
                    holder.time.setVisibility(View.VISIBLE);
                    holder.time.setText(AppTextUtils.getDurationString(audio.getDuration()));
                }

                int Status = DownloadWorkUtils.TrackIsDownloaded(audio);
                if (Status == 2) {
                    holder.saved.setImageResource(R.drawable.remote_cloud);
                } else {
                    holder.saved.setImageResource(R.drawable.save);
                }
                holder.saved.setVisibility(Status != 0 ? View.VISIBLE : View.GONE);

                holder.lyric.setVisibility(audio.getLyricsId() != 0 ? View.VISIBLE : View.GONE);
                holder.my.setVisibility(audio.getOwnerId() == Settings.get().accounts().getCurrent() ? View.VISIBLE : View.GONE);

                holder.Track.setOnLongClickListener(v -> {
                    if (!AppPerms.hasReadWriteStoragePermision(mContext)) {
                        AppPerms.requestReadWriteStoragePermission((Activity) mContext);
                        return false;
                    }
                    holder.saved.setVisibility(View.VISIBLE);
                    holder.saved.setImageResource(R.drawable.save);
                    int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false);
                    if (ret == 0)
                        PhoenixToast.CreatePhoenixToast(mContext).showToastBottom(R.string.saved_audio);
                    else if (ret == 1 || ret == 2) {
                        Utils.ThemedSnack(v, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true)).show();

                    } else {
                        holder.saved.setVisibility(View.GONE);
                        PhoenixToast.CreatePhoenixToast(mContext).showToastBottom(R.string.error_audio);
                    }
                    return true;
                });

                holder.Track.setOnClickListener(view -> {
                    holder.cancelSelectionAnimation();
                    holder.startSomeAnimation();

                    ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();

                    menus.add(new OptionRequest(AudioItem.play_item_audio, mContext.getString(R.string.play), R.drawable.play));
                    if (audio.getOwnerId() != Settings.get().accounts().getCurrent()) {
                        menus.add(new OptionRequest(AudioItem.add_item_audio, mContext.getString(R.string.action_add), R.drawable.list_add));
                        menus.add(new OptionRequest(AudioItem.add_and_download_button, mContext.getString(R.string.add_and_download_button), R.drawable.add_download));
                    } else
                        menus.add(new OptionRequest(AudioItem.add_item_audio, mContext.getString(R.string.delete), R.drawable.ic_outline_delete));
                    menus.add(new OptionRequest(AudioItem.share_button, mContext.getString(R.string.share), R.drawable.ic_outline_share));
                    menus.add(new OptionRequest(AudioItem.save_item_audio, mContext.getString(R.string.save), R.drawable.save));
                    if (audio.getAlbumId() != 0)
                        menus.add(new OptionRequest(AudioItem.open_album, mContext.getString(R.string.open_album), R.drawable.audio_album));
                    menus.add(new OptionRequest(AudioItem.get_recommendation_by_audio, mContext.getString(R.string.get_recommendation_by_audio), R.drawable.music_mic));

                    if (!isEmpty(audio.getMain_artists()))
                        menus.add(new OptionRequest(AudioItem.goto_artist, mContext.getString(R.string.audio_goto_artist), R.drawable.artist_icon));

                    if (audio.getLyricsId() != 0)
                        menus.add(new OptionRequest(AudioItem.get_lyrics_menu, mContext.getString(R.string.get_lyrics_menu), R.drawable.lyric));
                    if (!audio.isHLS()) {
                        menus.add(new OptionRequest(AudioItem.bitrate_item_audio, mContext.getString(R.string.get_bitrate), R.drawable.high_quality));
                    }
                    menus.add(new OptionRequest(AudioItem.search_by_artist, mContext.getString(R.string.search_by_artist), R.drawable.magnify));
                    menus.add(new OptionRequest(AudioItem.copy_url, mContext.getString(R.string.copy_url), R.drawable.content_copy));


                    menus.header(firstNonEmptyString(audio.getArtist(), " ") + " - " + audio.getTitle(), R.drawable.song, audio.getThumb_image_little());
                    menus.columns(2);
                    menus.show(((FragmentActivity) mContext).getSupportFragmentManager(), "audio_options", option -> {
                        switch (option.getId()) {
                            case AudioItem.play_item_audio:
                                mAttachmentsActionCallback.onAudioPlay(finalG, audios);
                                PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(mContext);
                                break;
                            case AudioItem.share_button:
                                SendAttachmentsActivity.startForSendAttachments(mContext, Settings.get().accounts().getCurrent(), audio);
                                break;
                            case AudioItem.search_by_artist:
                                PlaceFactory.getSingleTabSearchPlace(Settings.get().accounts().getCurrent(), SearchContentType.AUDIOS, new AudioSearchCriteria(audio.getArtist(), true, false)).tryOpenWith(mContext);
                                break;
                            case AudioItem.get_lyrics_menu:
                                get_lyrics(audio);
                                break;
                            case AudioItem.get_recommendation_by_audio:
                                PlaceFactory.SearchByAudioPlace(Settings.get().accounts().getCurrent(), audio.getOwnerId(), audio.getId()).tryOpenWith(mContext);
                                break;
                            case AudioItem.open_album:
                                PlaceFactory.getAudiosInAlbumPlace(Settings.get().accounts().getCurrent(), audio.getAlbum_owner_id(), audio.getAlbumId(), audio.getAlbum_access_key()).tryOpenWith(mContext);
                                break;
                            case AudioItem.copy_url:
                                ClipboardManager clipboard = (ClipboardManager) mContext.getSystemService(Context.CLIPBOARD_SERVICE);
                                ClipData clip = ClipData.newPlainText("response", audio.getUrl());
                                clipboard.setPrimaryClip(clip);
                                PhoenixToast.CreatePhoenixToast(mContext).showToast(R.string.copied);
                                break;
                            case AudioItem.add_item_audio:
                                boolean myAudio = audio.getOwnerId() == Settings.get().accounts().getCurrent();
                                if (myAudio) {
                                    deleteTrack(Settings.get().accounts().getCurrent(), audio);
                                    PhoenixToast.CreatePhoenixToast(mContext).showToast(R.string.deleted);
                                } else {
                                    addTrack(Settings.get().accounts().getCurrent(), audio);
                                    PhoenixToast.CreatePhoenixToast(mContext).showToast(R.string.added);
                                }
                                break;
                            case AudioItem.add_and_download_button:
                                addTrack(Settings.get().accounts().getCurrent(), audio);
                                PhoenixToast.CreatePhoenixToast(mContext).showToast(R.string.added);
                            case AudioItem.save_item_audio:
                                if (!AppPerms.hasReadWriteStoragePermision(mContext)) {
                                    AppPerms.requestReadWriteStoragePermission((Activity) mContext);
                                    break;
                                }
                                holder.saved.setVisibility(View.VISIBLE);
                                holder.saved.setImageResource(R.drawable.save);
                                int ret = DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), false);
                                if (ret == 0)
                                    PhoenixToast.CreatePhoenixToast(mContext).showToastBottom(R.string.saved_audio);
                                else if (ret == 1 || ret == 2) {
                                    Utils.ThemedSnack(view, ret == 1 ? R.string.audio_force_download : R.string.audio_force_download_pc, BaseTransientBottomBar.LENGTH_LONG).setAction(R.string.button_yes,
                                            v1 -> DownloadWorkUtils.doDownloadAudio(mContext, audio, Settings.get().accounts().getCurrent(), true)).show();
                                } else {
                                    holder.saved.setVisibility(View.GONE);
                                    PhoenixToast.CreatePhoenixToast(mContext).showToastBottom(R.string.error_audio);
                                }
                                break;
                            case AudioItem.bitrate_item_audio:
                                MediaMetadataRetriever retriever = new MediaMetadataRetriever();
                                retriever.setDataSource(Audio.getMp3FromM3u8(audio.getUrl()), new HashMap<>());
                                String bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE);
                                PhoenixToast.CreatePhoenixToast(mContext).showToast(mContext.getResources().getString(R.string.bitrate) + " " + (Long.parseLong(bitrate) / 1000) + " bit");
                                break;

                            case AudioItem.goto_artist:
                                String[][] artists = Utils.getArrayFromHash(audio.getMain_artists());
                                if (audio.getMain_artists().keySet().size() > 1) {
                                    new MaterialAlertDialogBuilder(mContext)
                                            .setItems(artists[1], (dialog, which) -> PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][which], false).tryOpenWith(mContext)).show();
                                } else {
                                    PlaceFactory.getArtistPlace(Settings.get().accounts().getCurrent(), artists[0][0], false).tryOpenWith(mContext);
                                }
                                break;
                        }
                    });
                });

                root.setVisibility(View.VISIBLE);
                root.setTag(audio);
            } else {
                root.setVisibility(View.GONE);
                root.setTag(null);
            }
        }
        mPlayerDisposable.dispose();
        mPlayerDisposable = observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe(this::onServiceBindEvent);
    }

    private void onServiceBindEvent(@MusicUtils.PlayerStatus int status) {
        switch (status) {
            case MusicUtils.PlayerStatus.UPDATE_TRACK_INFO:
            case MusicUtils.PlayerStatus.UPDATE_PLAY_PAUSE:
            case MusicUtils.PlayerStatus.SERVICE_KILLED:
                currAudio = MusicUtils.getCurrentAudio();
                if (getChildCount() < audios.size())
                    return;
                for (int g = 0; g < audios.size(); g++) {
                    ViewGroup root = (ViewGroup) getChildAt(g);
                    AudioHolder holder = new AudioHolder(root);
                    updateAudioStatus(holder, audios.get(g));
                }
                break;
            case MusicUtils.PlayerStatus.REPEATMODE_CHANGED:
            case MusicUtils.PlayerStatus.SHUFFLEMODE_CHANGED:
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        currAudio = MusicUtils.getCurrentAudio();
        if (!isEmpty(audios)) {
            mPlayerDisposable = observeServiceBinding()
                    .compose(RxUtils.applyObservableIOToMainSchedulers())
                    .subscribe(this::onServiceBindEvent);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mPlayerDisposable.dispose();
    }

    private class AudioHolder {
        TextView tvTitle;
        TextView tvSubtitle;
        View ibPlay;
        ImageView play_cover;
        TextView time;
        ImageView saved;
        ImageView lyric;
        ImageView my;
        ImageView quality;
        View Track;
        MaterialCardView selectionView;
        MaterialCardView isSelectedView;
        Animator.AnimatorListener animationAdapter;
        ObjectAnimator animator;
        LottieAnimationView visual;

        AudioHolder(View root) {
            tvTitle = root.findViewById(R.id.dialog_title);
            tvSubtitle = root.findViewById(R.id.dialog_message);
            ibPlay = root.findViewById(R.id.item_audio_play);
            play_cover = root.findViewById(R.id.item_audio_play_cover);
            time = root.findViewById(R.id.item_audio_time);
            saved = root.findViewById(R.id.saved);
            lyric = root.findViewById(R.id.lyric);
            Track = root.findViewById(R.id.track_option);
            my = root.findViewById(R.id.my);
            selectionView = root.findViewById(R.id.item_audio_selection);
            isSelectedView = root.findViewById(R.id.item_audio_select_add);
            isSelectedView.setVisibility(View.GONE);
            quality = root.findViewById(R.id.quality);
            visual = root.findViewById(R.id.item_audio_visual);
            animationAdapter = new WeakViewAnimatorAdapter<View>(selectionView) {
                @Override
                public void onAnimationEnd(View view) {
                    view.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationStart(View view) {
                    view.setVisibility(View.VISIBLE);
                }

                @Override
                protected void onAnimationCancel(View view) {
                    view.setVisibility(View.GONE);
                }
            };
        }

        void startSomeAnimation() {
            selectionView.setCardBackgroundColor(CurrentTheme.getColorSecondary(mContext));
            selectionView.setAlpha(0.5f);

            animator = ObjectAnimator.ofFloat(selectionView, View.ALPHA, 0.0f);
            animator.setDuration(500);
            animator.addListener(animationAdapter);
            animator.start();
        }

        void cancelSelectionAnimation() {
            if (animator != null) {
                animator.cancel();
                animator = null;
            }

            selectionView.setVisibility(View.INVISIBLE);
        }
    }
}
