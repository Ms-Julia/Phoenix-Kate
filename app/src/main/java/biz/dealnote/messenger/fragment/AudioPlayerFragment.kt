package biz.dealnote.messenger.fragment

import android.app.Dialog
import android.content.*
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.media.audiofx.AudioEffect
import android.os.*
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import biz.dealnote.messenger.Constants
import biz.dealnote.messenger.Extra
import biz.dealnote.messenger.R
import biz.dealnote.messenger.activity.SendAttachmentsActivity
import biz.dealnote.messenger.domain.IAudioInteractor
import biz.dealnote.messenger.domain.InteractorFactory
import biz.dealnote.messenger.fragment.search.SearchContentType
import biz.dealnote.messenger.fragment.search.criteria.AudioSearchCriteria
import biz.dealnote.messenger.materialpopupmenu.MaterialPopupMenuBuilder
import biz.dealnote.messenger.model.Audio
import biz.dealnote.messenger.picasso.PicassoInstance
import biz.dealnote.messenger.picasso.transforms.BlurTransformation
import biz.dealnote.messenger.picasso.transforms.PolyTopTransformation
import biz.dealnote.messenger.place.PlaceFactory
import biz.dealnote.messenger.player.ui.PlayPauseButton
import biz.dealnote.messenger.player.ui.RepeatButton
import biz.dealnote.messenger.player.ui.RepeatingImageButton
import biz.dealnote.messenger.player.ui.ShuffleButton
import biz.dealnote.messenger.player.util.MusicUtils
import biz.dealnote.messenger.player.util.MusicUtils.PlayerStatus
import biz.dealnote.messenger.settings.CurrentTheme
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.util.AppPerms
import biz.dealnote.messenger.util.DownloadWorkUtils.TrackIsDownloaded
import biz.dealnote.messenger.util.DownloadWorkUtils.doDownloadAudio
import biz.dealnote.messenger.util.Objects
import biz.dealnote.messenger.util.PhoenixToast.Companion.CreatePhoenixToast
import biz.dealnote.messenger.util.RxUtils
import biz.dealnote.messenger.util.Utils
import biz.dealnote.messenger.util.Utils.isEmpty
import biz.dealnote.messenger.view.CircleCounterButton
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.imageview.ShapeableImageView
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso3.BitmapTarget
import com.squareup.picasso3.Picasso
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import java.lang.ref.WeakReference
import java.util.*

class AudioPlayerFragment : BottomSheetDialogFragment(), OnSeekBarChangeListener {
    // Play and pause button
    private var mPlayPauseButton: PlayPauseButton? = null

    // Repeat button
    private var mRepeatButton: RepeatButton? = null

    // Shuffle button
    private var mShuffleButton: ShuffleButton? = null

    // Current time
    private var mCurrentTime: TextView? = null

    // Total time
    private var mTotalTime: TextView? = null
    private var mGetLyrics: ImageView? = null

    // Progress
    private var mProgress: SeekBar? = null

    // VK Additional action
    private var ivAdd: CircleCounterButton? = null
    private var ivSave: RepeatingImageButton? = null
    private var tvTitle: TextView? = null
    private var tvAlbum: TextView? = null
    private var tvSubtitle: TextView? = null
    private var ivCover: ShapeableImageView? = null
    private var ivBackground: ConstraintLayout? = null

    // Handler used to update the current time
    private var mTimeHandler: TimeHandler? = null
    private var mPosOverride: Long = -1
    private var mStartSeekPos: Long = 0
    private var mLastSeekEventTime: Long = 0
    private var mFromTouch = false
    private lateinit var mPlayerProgressStrings: Array<String>

    /**
     * Used to scan backwards through the track
     */
    private val mRewindListener = RepeatingImageButton.RepeatListener { _: View?, howlong: Long, repcnt: Int -> scanBackward(repcnt, howlong) }

    /**
     * Used to scan ahead through the track
     */
    private val mFastForwardListener = RepeatingImageButton.RepeatListener { _: View?, howlong: Long, repcnt: Int -> scanForward(repcnt, howlong) }
    private var mAudioInteractor: IAudioInteractor? = null
    private var mAccountId = 0
    private val mBroadcastDisposable = CompositeDisposable()
    private val mCompositeDisposable = CompositeDisposable()
    private fun appendDisposable(disposable: Disposable?) {
        mCompositeDisposable.add(disposable!!)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mAccountId = requireArguments().getInt(Extra.ACCOUNT_ID)
        mAudioInteractor = InteractorFactory.createAudioInteractor()
        mTimeHandler = TimeHandler(this)
        mPlayerProgressStrings = resources.getStringArray(R.array.player_progress_state)
        appendDisposable(MusicUtils.observeServiceBinding()
                .compose(RxUtils.applyObservableIOToMainSchedulers())
                .subscribe { onServiceBindEvent(it) })
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return dialog
    }

    private fun onServiceBindEvent(status: Int) {
        when (status) {
            PlayerStatus.UPDATE_TRACK_INFO -> {
                updatePlaybackControls()
                updateNowPlayingInfo()
                resolveControlViews()
            }
            PlayerStatus.UPDATE_PLAY_PAUSE -> {
                updatePlaybackControls()
                resolveTotalTime()
                resolveControlViews()
            }
            PlayerStatus.REPEATMODE_CHANGED, PlayerStatus.SHUFFLEMODE_CHANGED -> {
                mRepeatButton!!.updateRepeatState()
                mShuffleButton!!.updateShuffleState()
            }
            PlayerStatus.SERVICE_KILLED -> {
                updatePlaybackControls()
                updateNowPlayingInfo()
                resolveControlViews()
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val layoutRes: Int = if (Utils.isLandscape(requireActivity()) && !Utils.is600dp(requireActivity())) {
            R.layout.fragment_player_land
        } else {
            R.layout.fragment_player_port_new
        }
        val root = inflater.inflate(layoutRes, container, false)
        mProgress = root.findViewById(android.R.id.progress)
        mPlayPauseButton = root.findViewById(R.id.action_button_play)
        mShuffleButton = root.findViewById(R.id.action_button_shuffle)
        mRepeatButton = root.findViewById(R.id.action_button_repeat)
        val mAdditional = root.findViewById<ImageView>(R.id.goto_button)
        mAdditional.setOnClickListener {
            val popupMenu = MaterialPopupMenuBuilder()
            popupMenu.section {
                if (isEqualizerAvailable) {
                    item {
                        labelRes = R.string.equalizer
                        icon = R.drawable.settings
                        iconColor = CurrentTheme.getColorSecondary(requireActivity())
                        callback = {
                            startEffectsPanel()
                        }
                    }
                }
                item {
                    labelRes = R.string.playlist
                    icon = R.drawable.ic_menu_24_white
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        if (!isEmpty(MusicUtils.getQueue())) {
                            PlaylistFragment.newInstance(MusicUtils.getQueue() as ArrayList<Audio?>).show(childFragmentManager, "audio_playlist")
                        }
                    }
                }
                item {
                    labelRes = R.string.copy_track_info
                    icon = R.drawable.content_copy
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        var Artist = if (MusicUtils.getArtistName() != null) MusicUtils.getArtistName() else ""
                        if (MusicUtils.getAlbumName() != null) Artist += " (" + MusicUtils.getAlbumName() + ")"
                        val Name = if (MusicUtils.getTrackName() != null) MusicUtils.getTrackName() else ""
                        val clip = ClipData.newPlainText("response", "$Artist - $Name")
                        clipboard.setPrimaryClip(clip)
                        CreatePhoenixToast(requireActivity()).showToast(R.string.copied_to_clipboard)
                    }
                }
                item {
                    labelRes = R.string.search_by_artist
                    icon = R.drawable.magnify
                    iconColor = CurrentTheme.getColorSecondary(requireActivity())
                    callback = {
                        PlaceFactory.getSingleTabSearchPlace(mAccountId, SearchContentType.AUDIOS, AudioSearchCriteria(MusicUtils.getArtistName(), true, false)).tryOpenWith(requireActivity())
                        dismissAllowingStateLoss()
                    }
                }
            }
            popupMenu.build().show(requireActivity(), it)
        }
        val mPreviousButton: RepeatingImageButton = root.findViewById(R.id.action_button_previous)
        val mNextButton: RepeatingImageButton = root.findViewById(R.id.action_button_next)
        ivCover = root.findViewById(R.id.cover)
        ivBackground = root.findViewById(R.id.audio_player_constraint)
        mCurrentTime = root.findViewById(R.id.audio_player_current_time)
        mTotalTime = root.findViewById(R.id.audio_player_total_time)
        tvTitle = root.findViewById(R.id.audio_player_title)
        tvAlbum = root.findViewById(R.id.audio_player_album)
        tvSubtitle = root.findViewById(R.id.audio_player_subtitle)
        mGetLyrics = root.findViewById(R.id.audio_player_get_lyrics)
        mGetLyrics?.setOnClickListener { onLyrics() }

        if (Settings.get().other().isClick_next_track) {
            ivCover?.setOnClickListener {
                MusicUtils.next()
            }
            ivCover?.setOnLongClickListener {
                MusicUtils.previous(requireActivity())
                true
            }
        } else {
            ivCover?.setOnLongClickListener {
                MusicUtils.next()
                true
            }
        }

        //to animate running text
        tvTitle?.isSelected = true
        tvSubtitle?.isSelected = true
        tvAlbum?.isSelected = true
        mPreviousButton.setRepeatListener(mRewindListener)
        mNextButton.setRepeatListener(mFastForwardListener)
        mProgress?.setOnSeekBarChangeListener(this)
        ivSave = root.findViewById(R.id.audio_save)
        ivSave?.setOnClickListener { onSaveButtonClick(it) }
        ivAdd = root.findViewById(R.id.audio_add)
        if (Settings.get().main().isPlayer_support_volume) {
            ivAdd?.setIcon(R.drawable.volume_minus)
            ivAdd?.setOnClickListener {
                val audio = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamVolume(AudioManager.STREAM_MUSIC) - 1, 0)
            }
        } else {
            ivAdd?.setIcon(R.drawable.plus)
            ivAdd?.setOnClickListener { onAddButtonClick() }
        }
        val ivShare: CircleCounterButton = root.findViewById(R.id.audio_share)
        if (Settings.get().main().isPlayer_support_volume) {
            ivShare.setIcon(R.drawable.volume_plus)
            ivShare.setOnClickListener {
                val audio = requireActivity().getSystemService(Context.AUDIO_SERVICE) as AudioManager
                audio.setStreamVolume(AudioManager.STREAM_MUSIC, audio.getStreamVolume(AudioManager.STREAM_MUSIC) + 1, 0)
            }
        } else {
            ivShare.setIcon(R.drawable.ic_outline_share)
            ivShare.setOnClickListener { shareAudio() }
        }

        resolveAddButton()
        return root
    }

    private val isAudioStreaming: Boolean
        get() = Settings.get()
                .other()
                .isAudioBroadcastActive

    private fun onSaveButtonClick(v: View) {
        val audio = MusicUtils.getCurrentAudio() ?: return
        if (!AppPerms.hasReadWriteStoragePermision(context)) {
            AppPerms.requestReadWriteStoragePermission(requireActivity())
            return
        }
        when (doDownloadAudio(requireActivity(), audio, mAccountId, false)) {
            0 -> {
                CreatePhoenixToast(requireActivity()).showToastBottom(R.string.saved_audio)
                ivSave!!.setImageResource(R.drawable.succ)
            }
            1 -> {
                Snackbar.make(v, R.string.audio_force_download, Snackbar.LENGTH_LONG).setAction(R.string.button_yes
                ) { doDownloadAudio(requireActivity(), audio, mAccountId, true) }
                        .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity())).setAnchorView(mPlayPauseButton).setActionTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor("#ffffff") else Color.parseColor("#000000"))
                        .setTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor("#ffffff") else Color.parseColor("#000000")).show()
            }
            2 -> {
                Snackbar.make(v, R.string.audio_force_download_pc, Snackbar.LENGTH_LONG).setAnchorView(mPlayPauseButton).setAction(R.string.button_yes
                ) { doDownloadAudio(requireActivity(), audio, mAccountId, true) }
                        .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity())).setActionTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor("#ffffff") else Color.parseColor("#000000"))
                        .setTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity()))) Color.parseColor("#ffffff") else Color.parseColor("#000000")).show()
                ivSave!!.setImageResource(R.drawable.succ)
            }
            else -> CreatePhoenixToast(requireActivity()).showToastBottom(R.string.error_audio)
        }
    }

    private fun onAddButtonClick() {
        val audio = MusicUtils.getCurrentAudio() ?: return
        if (audio.isLocal) {
            CreatePhoenixToast(requireActivity()).showToastError(R.string.not_supported)
            return
        }
        if (audio.ownerId == mAccountId) {
            if (!audio.isDeleted) {
                delete(mAccountId, audio)
            } else {
                restore(mAccountId, audio)
            }
        } else {
            add(mAccountId, audio)
        }
    }

    private fun onLyrics() {
        val audio = MusicUtils.getCurrentAudio() ?: return
        get_lyrics(audio)
    }

    private fun add(accountId: Int, audio: Audio) {
        appendDisposable(mAudioInteractor!!.add(accountId, audio, null, null)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({ onAudioAdded() }) { })
    }

    private fun onAudioAdded() {
        CreatePhoenixToast(requireActivity()).showToast(R.string.added)
        resolveAddButton()
    }

    private fun delete(accoutnId: Int, audio: Audio) {
        val id = audio.id
        val ownerId = audio.ownerId
        appendDisposable(mAudioInteractor!!.delete(accoutnId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({ onAudioDeletedOrRestored(id, ownerId, true) }) { })
    }

    private fun restore(accountId: Int, audio: Audio) {
        val id = audio.id
        val ownerId = audio.ownerId
        appendDisposable(mAudioInteractor!!.restore(accountId, id, ownerId)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({ onAudioDeletedOrRestored(id, ownerId, false) }) { })
    }

    private fun get_lyrics(audio: Audio) {
        appendDisposable(mAudioInteractor!!.getLyrics(mAccountId, audio.lyricsId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe({ Text: String -> onAudioLyricsReceived(Text) }) { })
    }

    private fun onAudioLyricsReceived(Text: String) {
        var title: String? = null
        if (MusicUtils.getCurrentAudio() != null) title = MusicUtils.getCurrentAudio()?.artistAndTitle
        val dlgAlert = MaterialAlertDialogBuilder(requireActivity())
        dlgAlert.setIcon(R.drawable.dir_song)
        dlgAlert.setMessage(Text)
        dlgAlert.setTitle(title ?: requireContext().getString(R.string.get_lyrics))
        dlgAlert.setPositiveButton("OK", null)
        dlgAlert.setNeutralButton(requireContext().getString(R.string.copy_text)) { _: DialogInterface, _: Int ->
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("response", Text)
            clipboard.setPrimaryClip(clip)
            CreatePhoenixToast(requireActivity()).showToast(R.string.copied_to_clipboard)
        }
        dlgAlert.setCancelable(true)
        dlgAlert.create().show()
    }

    private fun onAudioDeletedOrRestored(id: Int, ownerId: Int, deleted: Boolean) {
        if (deleted) {
            CreatePhoenixToast(requireActivity()).showToast(R.string.deleted)
        } else {
            CreatePhoenixToast(requireActivity()).showToast(R.string.restored)
        }
        val current = MusicUtils.getCurrentAudio()
        if (Objects.nonNull(current) && current?.id == id && current.ownerId == ownerId) {
            current.isDeleted = deleted
        }
        resolveAddButton()
    }

    /**
     * {@inheritDoc}
     */
    override fun onProgressChanged(bar: SeekBar, progress: Int, fromuser: Boolean) {
        if (!fromuser || MusicUtils.mService == null) {
            return
        }
        val now = SystemClock.elapsedRealtime()
        if (now - mLastSeekEventTime > 250) {
            mLastSeekEventTime = now
            refreshCurrentTime()
            if (!mFromTouch) {
                // refreshCurrentTime();
                mPosOverride = -1
            }
        }
        mPosOverride = MusicUtils.duration() * progress / 1000
    }

    /**
     * {@inheritDoc}
     */
    override fun onStartTrackingTouch(bar: SeekBar) {
        mLastSeekEventTime = 0
        mFromTouch = true
        mCurrentTime!!.visibility = View.VISIBLE
    }

    /**
     * {@inheritDoc}
     */
    override fun onStopTrackingTouch(bar: SeekBar) {
        if (mPosOverride != -1L) {
            MusicUtils.seek(mPosOverride)
            val progress = (1000 * mPosOverride / MusicUtils.duration()).toInt()
            bar.progress = progress
            mPosOverride = -1
        }
        mFromTouch = false
    }

    /**
     * {@inheritDoc}
     */
    override fun onResume() {
        super.onResume()
        // Set the playback drawables
        updatePlaybackControls()
        // Current info
        updateNowPlayingInfo()

        resolveControlViews()
    }

    /**
     * {@inheritDoc}
     */
    override fun onStart() {
        super.onStart()
        // Refresh the current time
        val next = refreshCurrentTime()
        queueNextRefresh(next)
        MusicUtils.notifyForegroundStateChanged(requireActivity(), MusicUtils.isPlaying())
    }

    /**
     * {@inheritDoc}
     */
    override fun onStop() {
        super.onStop()
        MusicUtils.notifyForegroundStateChanged(requireActivity(), false)
    }

    /**
     * {@inheritDoc}
     */
    override fun onDestroy() {
        mCompositeDisposable.dispose()
        super.onDestroy()
        mTimeHandler!!.removeMessages(REFRESH_TIME)
        mBroadcastDisposable.dispose()
    }

    /**
     * Sets the track name, album name, and album art.
     */
    private fun updateNowPlayingInfo() {
        val coverUrl = MusicUtils.getAlbumCoverBig()
        if (mGetLyrics != null) {
            if (MusicUtils.getCurrentAudio() != null && MusicUtils.getCurrentAudio()?.lyricsId != 0) mGetLyrics!!.visibility = View.VISIBLE else mGetLyrics!!.visibility = View.GONE
        }
        if (tvAlbum != null) {
            var album = ""
            if (MusicUtils.getAlbumName() != null) album += requireContext().getString(R.string.album) + " " + MusicUtils.getAlbumName()
            tvAlbum!!.text = album
        }
        if (tvTitle != null) {
            tvTitle!!.text = MusicUtils.getArtistName()
        }
        if (tvSubtitle != null) {
            tvSubtitle!!.text = MusicUtils.getTrackName()
        }
        if (coverUrl != null) {
            ivCover!!.scaleType = ImageView.ScaleType.FIT_START
            PicassoInstance.with().load(coverUrl).tag(Constants.PICASSO_TAG).into(ivCover!!)
            if (Settings.get().other().isBlur_for_player) {
                PicassoInstance.with().load(coverUrl).tag(Constants.PICASSO_TAG).transform(PolyTopTransformation()).transform(BlurTransformation(25, 1, requireActivity())).into(object : BitmapTarget {
                    override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
                        ivBackground?.background = BitmapDrawable(resources, bitmap)
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                    }

                    override fun onBitmapFailed(e: Exception, errorDrawable: Drawable?) {
                        ivBackground?.setBackgroundResource(0)
                    }

                })
            }

        } else {
            ivBackground?.setBackgroundResource(0)
            PicassoInstance.with().cancelRequest(ivCover!!)
            ivCover!!.scaleType = ImageView.ScaleType.CENTER
            ivCover!!.setImageResource(R.drawable.itunes)
            ivCover!!.drawable.setTint(CurrentTheme.getColorOnSurface(requireContext()))
        }
        resolveAddButton()
        val current = MusicUtils.getCurrentAudio()
        if (current != null) {
            when {
                TrackIsDownloaded(current) == 1 -> {
                    ivSave!!.setImageResource(R.drawable.succ)
                }
                TrackIsDownloaded(current) == 2 -> {
                    ivSave!!.setImageResource(R.drawable.remote_cloud)
                }
                isEmpty(current.url) -> {
                    ivSave!!.setImageResource(R.drawable.audio_died)
                }
                else -> ivSave!!.setImageResource(R.drawable.save)
            }
        } else ivSave!!.setImageResource(R.drawable.save)

        //handle VK actions
        if (current != null && isAudioStreaming) {
            broadcastAudio()
        }

        // Set the total time
        resolveTotalTime()
    }

    private fun resolveTotalTime() {
        if (!isAdded || mTotalTime == null) {
            return
        }
        if (MusicUtils.isInitialized()) {
            mTotalTime!!.text = MusicUtils.makeTimeString(requireActivity(), MusicUtils.duration() / 1000)
        }
    }

    /**
     * Sets the correct drawable states for the playback controls.
     */
    private fun updatePlaybackControls() {
        if (!isAdded) {
            return
        }

        // Set the play and pause image
        if (Objects.nonNull(mPlayPauseButton)) {
            mPlayPauseButton!!.updateState()
        }

        // Set the shuffle image
        if (Objects.nonNull(mShuffleButton)) {
            mShuffleButton!!.updateShuffleState()
        }

        // Set the repeat image
        if (Objects.nonNull(mRepeatButton)) {
            mRepeatButton!!.updateRepeatState()
        }
    }

    private fun startEffectsPanel() {
        try {
            val effects = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            effects.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, requireContext().packageName)
            effects.putExtra(AudioEffect.EXTRA_AUDIO_SESSION, MusicUtils.getAudioSessionId())
            effects.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
            startActivityForResult(effects, REQUEST_EQ)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(requireActivity(), "No system equalizer found", Toast.LENGTH_SHORT).show()
        }
    }

    private val isEqualizerAvailable: Boolean
        get() {
            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
            val manager = requireActivity().packageManager
            val info = manager.queryIntentActivities(intent, 0)
            return info.size > 0
        }

    private fun shareAudio() {
        val current = MusicUtils.getCurrentAudio() ?: return
        if (current.isLocal) {
            CreatePhoenixToast(requireActivity()).showToastError(R.string.not_supported)
            return
        }
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), mAccountId, current)
    }

    private fun resolveAddButton() {
        if (Settings.get().main().isPlayer_support_volume) return
        if (!isAdded) return
        val currentAudio = MusicUtils.getCurrentAudio() ?: return
        //ivAdd.setVisibility(currentAudio == null ? View.INVISIBLE : View.VISIBLE);
        val myAudio = currentAudio.ownerId == mAccountId
        val icon = if (myAudio && !currentAudio.isDeleted) R.drawable.ic_outline_delete else R.drawable.plus
        ivAdd!!.setIcon(icon)
    }

    private fun broadcastAudio() {
        mBroadcastDisposable.clear()
        val currentAudio = MusicUtils.getCurrentAudio() ?: return
        val accountId = mAccountId
        val targetIds: Collection<Int> = setOf(accountId)
        val id = currentAudio.id
        val ownerId = currentAudio.ownerId
        mBroadcastDisposable.add(mAudioInteractor!!.sendBroadcast(accountId, ownerId, id, targetIds)
                .compose(RxUtils.applyCompletableIOToMainSchedulers())
                .subscribe({}) { })
    }

    /**
     * @param delay When to update
     */
    private fun queueNextRefresh(delay: Long) {
        val message = mTimeHandler!!.obtainMessage(REFRESH_TIME)
        mTimeHandler!!.removeMessages(REFRESH_TIME)
        mTimeHandler!!.sendMessageDelayed(message, delay)
    }

    private fun resolveControlViews() {
        if (!isAdded || mProgress == null) return
        val preparing = MusicUtils.isPreparing()
        val initialized = MusicUtils.isInitialized()
        mProgress!!.isEnabled = !preparing && initialized
        //mProgress!!.isIndeterminate = preparing
    }

    /**
     * Used to scan backwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param deltal  The long press duration
     */
    private fun scanBackward(repcnt: Int, deltal: Long) {
        var delta = deltal
        if (MusicUtils.mService == null) {
            return
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position()
            mLastSeekEventTime = 0
        } else {
            delta = if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta * 10
            } else {
                // seek at 40x after that
                50000 + (delta - 5000) * 40
            }
            var newpos = mStartSeekPos - delta
            if (newpos < 0) {
                // move to previous track
                MusicUtils.previous(requireActivity())
                val duration = MusicUtils.duration()
                mStartSeekPos += duration
                newpos += duration
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos)
                mLastSeekEventTime = delta
            }
            mPosOverride = if (repcnt >= 0) {
                newpos
            } else {
                -1
            }
            refreshCurrentTime()
        }
    }

    /**
     * Used to scan forwards in time through the curren track
     *
     * @param repcnt The repeat count
     * @param deltal  The long press duration
     */
    private fun scanForward(repcnt: Int, deltal: Long) {
        var delta = deltal
        if (MusicUtils.mService == null) {
            return
        }
        if (repcnt == 0) {
            mStartSeekPos = MusicUtils.position()
            mLastSeekEventTime = 0
        } else {
            delta = if (delta < 5000) {
                // seek at 10x speed for the first 5 seconds
                delta * 10
            } else {
                // seek at 40x after that
                50000 + (delta - 5000) * 40
            }
            var newpos = mStartSeekPos + delta
            val duration = MusicUtils.duration()
            if (newpos >= duration) {
                // move to next track
                MusicUtils.next()
                mStartSeekPos -= duration // is OK to go negative
                newpos -= duration
            }
            if (delta - mLastSeekEventTime > 250 || repcnt < 0) {
                MusicUtils.seek(newpos)
                mLastSeekEventTime = delta
            }
            mPosOverride = if (repcnt >= 0) {
                newpos
            } else {
                -1
            }
            refreshCurrentTime()
        }
    }

    private fun refreshCurrentTimeText(pos: Long) {
        mCurrentTime!!.text = MusicUtils.makeTimeString(requireActivity(), pos / 1000)
    }

    private fun refreshCurrentTime(): Long {
        //Logger.d("refreshTime", String.valueOf(mService == null));
        if (!MusicUtils.isInitialized()) {
            mCurrentTime!!.text = "--:--"
            mTotalTime!!.text = "--:--"
            mProgress!!.progress = 0
            return 500
        }
        try {
            val pos = if (mPosOverride < 0) MusicUtils.position() else mPosOverride
            val duration = MusicUtils.duration()
            if (pos >= 0 && duration > 0) {
                refreshCurrentTimeText(pos)
                val progress = (1000 * pos / duration).toInt()
                mProgress!!.progress = progress
                val bufferProgress = (MusicUtils.bufferPercent().toFloat() * 10f).toInt()
                mProgress!!.secondaryProgress = bufferProgress
                when {
                    mFromTouch -> {
                        return 500
                    }
                    MusicUtils.isPlaying() -> {
                        mCurrentTime!!.visibility = View.VISIBLE
                    }
                    else -> {
                        // blink the counter
                        val vis = mCurrentTime!!.visibility
                        mCurrentTime!!.visibility = if (vis == View.INVISIBLE) View.VISIBLE else View.INVISIBLE
                        return 500
                    }
                }
            } else {
                mCurrentTime!!.text = "--:--"
                mProgress!!.progress = 0
                val current = if (mTotalTime!!.tag == null) 0 else mTotalTime!!.tag as Int
                val next = if (current == mPlayerProgressStrings.size - 1) 0 else current + 1
                mTotalTime!!.tag = next
                mTotalTime!!.text = mPlayerProgressStrings[next]
                return 500
            }

            // calculate the number of milliseconds until the next full second,
            // so
            // the counter can be updated at just the right time
            val remaining = duration - pos % duration

            // approximate how often we would need to refresh the slider to
            // move it smoothly
            var width = mProgress!!.width
            if (width == 0) {
                width = 320
            }
            val smoothrefreshtime = duration / width
            if (smoothrefreshtime > remaining) {
                return remaining
            }
            return if (smoothrefreshtime < 20) {
                20
            } else smoothrefreshtime
        } catch (ignored: Exception) {
        }
        return 500
    }

    /**
     * Used to update the current time string
     */
    private class TimeHandler(player: AudioPlayerFragment) : Handler(Looper.getMainLooper()) {
        private val mAudioPlayer: WeakReference<AudioPlayerFragment> = WeakReference(player)
        override fun handleMessage(msg: Message) {
            if (msg.what == REFRESH_TIME) {
                if (mAudioPlayer.get() == null) return
                val next = mAudioPlayer.get()!!.refreshCurrentTime()
                mAudioPlayer.get()!!.queueNextRefresh(next)
            }
        }

    }

    companion object {
        // Message to refresh the time
        private const val REFRESH_TIME = 1
        private const val REQUEST_EQ = 139

        @JvmStatic
        fun buildArgs(accountId: Int): Bundle {
            val bundle = Bundle()
            bundle.putInt(Extra.ACCOUNT_ID, accountId)
            return bundle
        }

        fun newInstance(accountId: Int): AudioPlayerFragment {
            return newInstance(buildArgs(accountId))
        }

        @JvmStatic
        fun newInstance(args: Bundle?): AudioPlayerFragment {
            val fragment = AudioPlayerFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
