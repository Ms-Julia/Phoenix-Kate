package biz.dealnote.messenger.fragment

import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import biz.dealnote.messenger.Extra
import biz.dealnote.messenger.R
import biz.dealnote.messenger.adapter.AudioRecyclerAdapter
import biz.dealnote.messenger.listener.BackPressCallback
import biz.dealnote.messenger.model.Audio
import biz.dealnote.messenger.place.PlaceFactory
import biz.dealnote.messenger.player.MusicPlaybackService
import biz.dealnote.messenger.player.MusicPlaybackService.Companion.startForPlayList
import biz.dealnote.messenger.player.util.MusicUtils
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.util.Objects
import biz.dealnote.messenger.util.PhoenixToast.Companion.CreatePhoenixToast
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class PlaylistFragment : BottomSheetDialogFragment(), AudioRecyclerAdapter.ClickListener, BackPressCallback {
    private var mRecyclerView: RecyclerView? = null
    private var mAdapter: AudioRecyclerAdapter? = null
    private var mData: ArrayList<Audio>? = null
    private var mPlaybackStatus: PlaybackStatus? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mData = requireArguments().getParcelableArrayList(Extra.AUDIOS)
    }

    private fun getAudioPos(audio: Audio): Int {
        if (mData != null && mData!!.isNotEmpty()) {
            for ((pos, i) in mData!!.withIndex()) {
                if (i.id == audio.id && i.ownerId == audio.ownerId) {
                    i.isAnimationNow = true
                    mAdapter!!.notifyDataSetChanged()
                    return pos
                }
            }
        }
        return -1
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = BottomSheetDialog(requireActivity(), theme)
        val behavior = dialog.behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.skipCollapsed = true
        return dialog
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_playlist, container, false)
        mRecyclerView = root.findViewById(R.id.list)
        val manager = LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false)
        mRecyclerView?.layoutManager = manager
        val Goto: FloatingActionButton = root.findViewById(R.id.goto_button)
        Goto.setOnLongClickListener {
            val curr = MusicUtils.getCurrentAudio()
            if (curr != null) {
                PlaceFactory.getPlayerPlace(Settings.get().accounts().current).tryOpenWith(requireActivity())
            } else CreatePhoenixToast(requireActivity()).showToastError(R.string.null_audio)
            false
        }
        Goto.setOnClickListener {
            val curr = MusicUtils.getCurrentAudio()
            if (curr != null) {
                val index = getAudioPos(curr)
                if (index >= 0) {
                    if (Settings.get().other().isShow_audio_cover) mRecyclerView?.scrollToPosition(index) else mRecyclerView?.smoothScrollToPosition(index)
                } else CreatePhoenixToast(requireActivity()).showToast(R.string.audio_not_found)
            } else CreatePhoenixToast(requireActivity()).showToastError(R.string.null_audio)
        }
        ItemTouchHelper(simpleItemTouchCallback).attachToRecyclerView(mRecyclerView)
        return root
    }

    private var simpleItemTouchCallback: ItemTouchHelper.SimpleCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        override fun onMove(recyclerView: RecyclerView,
                            viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, swipeDir: Int) {
            viewHolder.itemView.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
            mAdapter?.notifyItemChanged(viewHolder.bindingAdapterPosition)
            startForPlayList(requireActivity(), mData!!, mAdapter!!.getItemRawPosition(viewHolder.bindingAdapterPosition), false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mAdapter = AudioRecyclerAdapter(requireActivity(), mData, false, false, 0)
        mAdapter!!.setClickListener(this)
        mRecyclerView!!.adapter = mAdapter
        val my = MusicUtils.getCurrentAudio()
        if (my != null) {
            var index = 0
            var o = 0
            for (i in mData!!) {
                if (i === my) {
                    index = o
                    break
                }
                o++
            }
            mRecyclerView!!.scrollToPosition(index)
        }
    }

    override fun onClick(position: Int, catalog: Int, audio: Audio) {
        startForPlayList(requireActivity(), mData!!, position, false)
    }

    override fun onEdit(position: Int, audio: Audio?) {
        TODO("Not yet implemented")
    }

    override fun onDelete(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUrlPhotoOpen(url: String, prefix: String, photo_prefix: String) {
        PlaceFactory.getSingleURLPhotoPlace(url, prefix, photo_prefix).tryOpenWith(requireActivity())
    }

    override fun onResume() {
        super.onResume()
        mPlaybackStatus = PlaybackStatus()
        val filter = IntentFilter()
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED)
        filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED)
        filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED)
        filter.addAction(MusicPlaybackService.META_CHANGED)
        filter.addAction(MusicPlaybackService.PREPARED)
        filter.addAction(MusicPlaybackService.REFRESH)
        requireActivity().registerReceiver(mPlaybackStatus, filter)
    }

    override fun onBackPressed(): Boolean {
        return true
    }

    override fun onPause() {
        try {
            requireActivity().unregisterReceiver(mPlaybackStatus)
        } catch (ignored: Throwable) {
        }
        super.onPause()
    }

    private inner class PlaybackStatus : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (Objects.isNull(action)) return
            if (MusicPlaybackService.PLAYSTATE_CHANGED == action) {
                mAdapter!!.notifyDataSetChanged()
            }
        }
    }

    companion object {
        fun buildArgs(playlist: ArrayList<Audio?>?): Bundle {
            val bundle = Bundle()
            bundle.putParcelableArrayList(Extra.AUDIOS, playlist)
            return bundle
        }

        fun newInstance(playlist: ArrayList<Audio?>?): PlaylistFragment {
            return newInstance(buildArgs(playlist))
        }

        fun newInstance(args: Bundle?): PlaylistFragment {
            val fragment = PlaylistFragment()
            fragment.arguments = args
            return fragment
        }
    }
}
