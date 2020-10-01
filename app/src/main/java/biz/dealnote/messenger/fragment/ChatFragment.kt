package biz.dealnote.messenger.fragment

import android.Manifest
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.InputType
import android.util.SparseBooleanArray
import android.view.*
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import biz.dealnote.messenger.*
import biz.dealnote.messenger.activity.*
import biz.dealnote.messenger.adapter.AttachmentsBottomSheetAdapter
import biz.dealnote.messenger.adapter.AttachmentsViewBinder
import biz.dealnote.messenger.adapter.MessagesAdapter
import biz.dealnote.messenger.api.model.VKApiMessage
import biz.dealnote.messenger.crypt.KeyLocationPolicy
import biz.dealnote.messenger.dialog.ImageSizeAlertDialog
import biz.dealnote.messenger.fragment.base.PlaceSupportMvpFragment
import biz.dealnote.messenger.fragment.search.SearchContentType
import biz.dealnote.messenger.fragment.search.criteria.MessageSeachCriteria
import biz.dealnote.messenger.fragment.sheet.MessageAttachmentsFragment
import biz.dealnote.messenger.listener.BackPressCallback
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener
import biz.dealnote.messenger.listener.OnSectionResumeCallback
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener
import biz.dealnote.messenger.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment
import biz.dealnote.messenger.modalbottomsheetdialogfragment.Option
import biz.dealnote.messenger.modalbottomsheetdialogfragment.OptionRequest
import biz.dealnote.messenger.model.*
import biz.dealnote.messenger.model.Types
import biz.dealnote.messenger.model.selection.*
import biz.dealnote.messenger.mvp.presenter.ChatPrensenter
import biz.dealnote.messenger.mvp.view.IChatView
import biz.dealnote.messenger.picasso.PicassoInstance
import biz.dealnote.messenger.picasso.transforms.RoundTransformation
import biz.dealnote.messenger.place.PlaceFactory
import biz.dealnote.messenger.settings.CurrentTheme
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.settings.SwipesChatMode
import biz.dealnote.messenger.upload.UploadDestination
import biz.dealnote.messenger.util.*
import biz.dealnote.messenger.util.Objects
import biz.dealnote.messenger.util.Utils.nonEmpty
import biz.dealnote.messenger.view.InputViewController
import biz.dealnote.messenger.view.LoadMoreFooterHelper
import biz.dealnote.messenger.view.WeakViewAnimatorAdapter
import biz.dealnote.messenger.view.emoji.EmojiconTextView
import biz.dealnote.messenger.view.emoji.EmojiconsPopup
import biz.dealnote.messenger.view.emoji.StickersKeyWordsAdapter
import biz.dealnote.mvp.core.IPresenterFactory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import java.lang.ref.WeakReference
import java.util.*

class ChatFragment : PlaceSupportMvpFragment<ChatPrensenter, IChatView>(), IChatView, InputViewController.OnInputActionCallback,
        BackPressCallback, MessagesAdapter.OnMessageActionListener, InputViewController.RecordActionsCallback,
        AttachmentsViewBinder.VoiceActionListener, EmojiconsPopup.OnStickerClickedListener,
        EmojiconTextView.OnHashTagClickListener {

    private var headerView: View? = null
    private var loadMoreFooterHelper: LoadMoreFooterHelper? = null

    private var recyclerView: RecyclerView? = null
    private var stickersKeywordsView: RecyclerView? = null
    private var stickersAdapter: StickersKeyWordsAdapter? = null
    private var adapter: MessagesAdapter? = null

    private var inputViewController: InputViewController? = null
    private var emptyText: TextView? = null

    private var pinnedView: View? = null
    private var pinnedAvatar: ImageView? = null
    private var pinnedTitle: TextView? = null
    private var pinnedSubtitle: TextView? = null
    private var buttonUnpin: View? = null

    private val optionMenuSettings = SparseBooleanArray()

    private var toolbarRootView: ViewGroup? = null
    private var actionModeHolder: ActionModeHolder? = null

    private var editMessageGroup: ViewGroup? = null
    private var editMessageText: TextView? = null

    private var goto_button: FloatingActionButton? = null

    private var Writing_msg_Group: View? = null
    private var Writing_msg: TextView? = null
    private var Writing_msg_Ava: ImageView? = null

    private var Title: TextView? = null
    private var SubTitle: TextView? = null

    private var Avatar: ImageView? = null

    private var toolbar: Toolbar? = null
    private var EmptyAvatar: TextView? = null

    private var InputView: View? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val root = inflater.inflate(R.layout.fragment_chat, container, false) as ViewGroup
        root.background = CurrentTheme.getChatBackground(activity)

        toolbar = root.findViewById(R.id.toolbar)
        toolbar?.inflateMenu(R.menu.menu_chat)
        PrepareOptionsMenu(toolbar?.menu!!)
        toolbar!!.setOnMenuItemClickListener { item: MenuItem ->
            OptionsItemSelected(item)
        }

        stickersKeywordsView = root.findViewById(R.id.stickers)
        stickersAdapter = StickersKeyWordsAdapter(requireActivity(), Collections.emptyList())
        stickersAdapter?.setStickerClickedListener { presenter?.fireStickerSendClick(it); presenter?.resetDraftMessage() }
        stickersKeywordsView?.layoutManager = LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false)
        stickersKeywordsView?.adapter = stickersAdapter
        stickersKeywordsView?.visibility = View.GONE

        Title = root.findViewById(R.id.dialog_title)
        SubTitle = root.findViewById(R.id.dialog_subtitle)
        Avatar = root.findViewById(R.id.toolbar_avatar)
        EmptyAvatar = root.findViewById(R.id.empty_avatar_text)

        emptyText = root.findViewById(R.id.fragment_chat_empty_text)
        toolbarRootView = root.findViewById(R.id.toolbar_root)

        Writing_msg_Group = root.findViewById(R.id.writingGroup)
        Writing_msg = root.findViewById(R.id.writing)
        Writing_msg_Ava = root.findViewById(R.id.writingava)
        InputView = root.findViewById(R.id.linearLayout)

        Writing_msg_Group?.visibility = View.GONE

        recyclerView = root.findViewById(R.id.fragment_friend_dialog_list)
        recyclerView?.apply {
            layoutManager = createLayoutManager()
            if (!Settings.get().main().is_smooth_chat) {
                itemAnimator?.changeDuration = 0
                itemAnimator?.addDuration = 0
                itemAnimator?.moveDuration = 0
                itemAnimator?.removeDuration = 0
            }
            addOnScrollListener(PicassoPauseOnScrollListener(Constants.PICASSO_TAG))
            addOnScrollListener(object : EndlessRecyclerOnScrollListener() {
                override fun onScrollToLastElement() {
                    presenter?.fireScrollToEnd()
                }
            })
        }

        ItemTouchHelper(MessagesReplyItemCallback { presenter?.fireResendSwipe(adapter!!.getItemRawPosition(it)) }).attachToRecyclerView(recyclerView)

        headerView = inflater.inflate(R.layout.footer_load_more, recyclerView, false)

        loadMoreFooterHelper = LoadMoreFooterHelper.createFrom(headerView) {
            presenter?.fireLoadUpButtonClick()
        }
        loadMoreFooterHelper?.setEndOfListText("• • • • • • • •")

        inputViewController = InputViewController(requireActivity(), root, this)
                .also {
                    it.setSendOnEnter(Settings.get().main().isSendByEnter)
                    it.setRecordActionsCallback(this)
                    it.setOnSickerClickListener(this)
                }

        pinnedView = root.findViewById(R.id.pinned_root_view)
        pinnedAvatar = pinnedView?.findViewById(R.id.pinned_avatar)
        pinnedTitle = pinnedView?.findViewById(R.id.pinned_title)
        pinnedSubtitle = pinnedView?.findViewById(R.id.pinned_subtitle)
        buttonUnpin = pinnedView?.findViewById(R.id.buttonUnpin)
        buttonUnpin?.setOnClickListener { presenter?.fireUnpinClick() }

        pinnedView?.setOnLongClickListener { pinnedView?.visibility = View.GONE; true }

        goto_button = root.findViewById(R.id.goto_button)
        if (Utils.isHiddenCurrent()) {
            goto_button?.setImageResource(R.drawable.attachment)
            goto_button?.setOnClickListener { presenter?.fireDialogAttachmentsClick() }
        } else {
            goto_button?.setImageResource(R.drawable.view)
            goto_button?.setOnClickListener { recyclerView?.smoothScrollToPosition(presenter?.getConversation()!!.unreadCount) }
            goto_button?.setOnLongClickListener { presenter?.fireDialogAttachmentsClick(); true; }
        }

        if (!Settings.get().other().isEnable_last_read)
            goto_button?.visibility = View.GONE
        else
            goto_button?.visibility = View.VISIBLE

        editMessageGroup = root.findViewById(R.id.editMessageGroup)
        editMessageText = editMessageGroup?.findViewById(R.id.editMessageText)
        editMessageGroup?.findViewById<View>(R.id.buttonCancelEditing)?.setOnClickListener { presenter?.fireCancelEditingClick() }
        return root
    }

    override fun displayEditingMessage(message: Message?) {
        editMessageGroup?.visibility = if (message == null) View.GONE else View.VISIBLE
        message?.run {
            editMessageText?.text = if (body.isNullOrEmpty()) getString(R.string.attachments) else body
        }
    }

    override fun displayWriting(owner_id: Int) {
        Writing_msg_Group?.visibility = View.VISIBLE
        Writing_msg_Group?.alpha = 0.0f
        ObjectAnimator.ofFloat(Writing_msg_Group, View.ALPHA, 1f).setDuration(200).start()
        Writing_msg?.setText(R.string.user_type_message)
        Writing_msg_Ava?.setImageResource(R.drawable.background_gray_round)
        presenter?.ResolveWritingInfo(requireActivity(), owner_id)
    }

    @SuppressLint("SetTextI18n")
    override fun displayWriting(owner: Owner) {
        Writing_msg?.text = owner.fullName + " "
        ViewUtils.displayAvatar(Writing_msg_Ava!!, CurrentTheme.createTransformationForAvatar(requireContext()),
                owner.get100photoOrSmaller(), null)
    }

    override fun updateStickers(items: List<Sticker>) {
        if (Utils.isEmpty(items)) {
            stickersKeywordsView?.visibility = View.GONE
        } else {
            stickersKeywordsView?.visibility = View.VISIBLE
        }
        if (Settings.get().ui().swipes_chat_mode == SwipesChatMode.V1) {
            PlaceFactory.enableTouchViewPagerDialogs(Utils.isEmpty(items)).tryOpenWith(requireActivity())
        }
        stickersAdapter?.setData(items)
    }

    override fun hideWriting() {
        val animator: ObjectAnimator? = ObjectAnimator.ofFloat(Writing_msg_Group, View.ALPHA, 0.0f)
        animator?.addListener(object : WeakViewAnimatorAdapter<View>(Writing_msg_Group) {
            override fun onAnimationEnd(view: View) {
                Writing_msg_Group?.visibility = View.GONE
            }

            override fun onAnimationStart(view: View) {
            }

            override fun onAnimationCancel(view: View) {
            }
        })
        animator?.setDuration(200)?.start()
    }

    private class ActionModeHolder(val rootView: View, fragment: ChatFragment) : View.OnClickListener {

        override fun onClick(v: View) {
            when (v.id) {
                R.id.buttonClose -> hide()
                R.id.buttonEdit -> {
                    reference.get()?.presenter?.fireActionModeEditClick()
                    hide()
                }
                R.id.buttonForward -> {
                    reference.get()?.presenter?.onActionModeForwardClick()
                    hide()
                }
                R.id.buttonCopy -> {
                    reference.get()?.presenter?.fireActionModeCopyClick()
                    hide()
                }
                R.id.buttonDelete -> {
                    reference.get()?.presenter?.fireActionModeDeleteClick()
                    hide()
                }
                R.id.buttonPin -> {
                    reference.get()?.presenter?.fireActionModePinClick()
                    hide()
                }
                R.id.buttonStar -> {
                    reference.get()?.presenter?.fireActionModeStarClick()
                    hide()
                }
            }
        }

        val reference = WeakReference(fragment)
        val buttonClose: View = rootView.findViewById(R.id.buttonClose)
        val buttonEdit: View = rootView.findViewById(R.id.buttonEdit)
        val buttonForward: View = rootView.findViewById(R.id.buttonForward)
        val buttonCopy: View = rootView.findViewById(R.id.buttonCopy)
        val buttonDelete: View = rootView.findViewById(R.id.buttonDelete)
        val buttonPin: View = rootView.findViewById(R.id.buttonPin)
        val buttonStar: ImageView = rootView.findViewById(R.id.buttonStar)
        val titleView: TextView = rootView.findViewById(R.id.actionModeTitle)

        init {
            buttonClose.setOnClickListener(this)
            buttonEdit.setOnClickListener(this)
            buttonForward.setOnClickListener(this)
            buttonCopy.setOnClickListener(this)
            buttonDelete.setOnClickListener(this)
            buttonPin.setOnClickListener(this)
            buttonStar.setOnClickListener(this)
        }

        fun show() {
            rootView.visibility = View.VISIBLE
        }

        fun isVisible(): Boolean = rootView.visibility == View.VISIBLE

        fun hide() {
            rootView.visibility = View.GONE
            reference.get()?.presenter?.fireActionModeDestroy()
        }
    }

    override fun onRecordCancel() {
        presenter?.fireRecordCancelClick()
    }

    override fun onEmojiOpened(opened: Boolean) {
        if (Settings.get().ui().swipes_chat_mode == SwipesChatMode.V1) {
            PlaceFactory.enableTouchViewPagerDialogs(!opened).tryOpenWith(requireActivity())
        }
    }

    override fun onSwithToRecordMode() {
        presenter?.fireRecordingButtonClick()
    }

    override fun onRecordSendClick() {
        presenter?.fireRecordSendClick()
    }

    override fun onResumePauseClick() {
        presenter?.fireRecordResumePauseClick()
    }

    private fun createLayoutManager(): RecyclerView.LayoutManager {
        return LinearLayoutManager(activity, RecyclerView.VERTICAL, true)
    }

    override fun displayMessages(messages: List<Message>, lastReadId: LastReadId) {
        adapter = MessagesAdapter(activity, messages, lastReadId, this, false)
                .also {
                    it.setOnMessageActionListener(this)
                    it.setVoiceActionListener(this)
                    it.addFooter(headerView)
                    it.setOnHashTagClickListener(this)
                }

        recyclerView?.adapter = adapter
    }

    override fun notifyMessagesUpAdded(position: Int, count: Int) {
        adapter?.run {
            notifyItemRangeChanged(position + headersCount, count) //+header if exist
        }
    }

    override fun notifyDataChanged() {
        adapter?.notifyDataSetChanged()
    }

    override fun notifyMessagesDownAdded(count: Int) {

    }

    override fun configNowVoiceMessagePlaying(voiceId: Int, progress: Float, paused: Boolean, amin: Boolean) {
        adapter?.configNowVoiceMessagePlaying(voiceId, progress, paused, amin)
    }

    override fun bindVoiceHolderById(holderId: Int, play: Boolean, paused: Boolean, progress: Float, amin: Boolean) {
        adapter?.bindVoiceHolderById(holderId, play, paused, progress, amin)
    }

    override fun disableVoicePlaying() {
        adapter?.disableVoiceMessagePlaying()
    }

    override fun getPresenterFactory(saveInstanceState: Bundle?): IPresenterFactory<ChatPrensenter> = object : IPresenterFactory<ChatPrensenter> {
        override fun create(): ChatPrensenter {
            val aid = requireArguments().getInt(Extra.ACCOUNT_ID)
            val messagesOwnerId = requireArguments().getInt(Extra.OWNER_ID)
            val peer = requireArguments().getParcelable<Peer>(Extra.PEER)
            return ChatPrensenter(aid, messagesOwnerId, peer!!, createStartConfig(), saveInstanceState)
        }
    }

    private fun createStartConfig(): ChatConfig {
        val config = ChatConfig()

        config.isCloseOnSend = requireActivity() is SendAttachmentsActivity

        val inputStreams = ActivityUtils.checkLocalStreams(requireActivity())
        config.uploadFiles = if (inputStreams == null) null else {
            if (inputStreams.uris.nullOrEmpty()) null else inputStreams.uris
        }
        config.uploadFilesMimeType = inputStreams?.mime

        val models = requireActivity().intent.getParcelableExtra<ModelsBundle>(MainActivity.EXTRA_INPUT_ATTACHMENTS)

        models?.run {
            config.appendAll(this)
        }

        val initialText = ActivityUtils.checkLinks(requireActivity())
        config.initialText = if (initialText.isNullOrEmpty()) null else initialText
        return config
    }

    override fun setupLoadUpHeaderState(@LoadMoreState state: Int) {
        loadMoreFooterHelper?.switchToState(state)
    }

    override fun displayDraftMessageAttachmentsCount(count: Int) {
        inputViewController?.setAttachmentsCount(count)
    }

    override fun displayDraftMessageText(text: String?) {
        inputViewController?.setTextQuietly(text)
        presenter?.fireTextEdited(text)
    }

    override fun AppendMessageText(text: String?) {
        inputViewController?.AppendTextQuietly(text)
    }

    override fun displayToolbarTitle(text: String?) {
        Title?.text = text
    }

    override fun displayToolbarSubtitle(text: String?) {
        SubTitle?.text = text
    }

    override fun displayToolbarAvatar(peer: Peer?) {
        if (nonEmpty(peer?.avaUrl)) {
            EmptyAvatar?.visibility = View.GONE
            PicassoInstance.with()
                    .load(peer?.avaUrl)
                    .transform(RoundTransformation())
                    .into(Avatar!!)
        } else {
            PicassoInstance.with().cancelRequest(Avatar!!)
            EmptyAvatar?.visibility = View.VISIBLE
            var name: String = peer!!.title
            if (name.length > 2) name = name.substring(0, 2)
            name = name.trim { it <= ' ' }
            EmptyAvatar?.text = name
            Avatar?.setImageBitmap(RoundTransformation().transform(Utils.createGradientChatImage(200, 200, peer.id)).bitmap)
        }
    }

    override fun setupPrimaryButtonAsEditing(canSave: Boolean) {
        inputViewController?.switchModeToEditing(canSave)
    }

    override fun setupPrimaryButtonAsRecording() {
        inputViewController?.switchModeToRecording()
    }

    override fun setupPrimaryButtonAsRegular(canSend: Boolean, canStartRecoring: Boolean) {
        inputViewController?.switchModeToNormal(canSend, canStartRecoring)
    }

    override fun requestRecordPermissions() {
        requestPermissions(arrayOf(Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE), REQUEST_RECORD_PERMISSIONS)
    }

    override fun displayRecordingDuration(time: Long) {
        inputViewController?.setRecordingDuration(time)
    }

    override fun doCloseAfterSend() {
        try {
            requireActivity().finish()
        } catch (ignored: Exception) {

        }
    }

    override fun goToMessagesLookup(accountId: Int, peerId: Int, messageId: Int) {
        PlaceFactory.getMessagesLookupPlace(accountId, peerId, messageId).tryOpenWith(requireActivity())
    }

    override fun displayPinnedMessage(pinned: Message?, canChange: Boolean) {
        pinnedView?.run {
            visibility = if (pinned == null) View.GONE else View.VISIBLE

            pinned?.run {
                ViewUtils.displayAvatar(pinnedAvatar!!, CurrentTheme.createTransformationForAvatar(requireContext()),
                        sender.get100photoOrSmaller(), null)

                pinnedTitle?.text = sender.fullName
                if (Utils.isEmpty(body) && pinned.isHasAttachments) {
                    body = getString(R.string.attachments)
                }
                pinnedSubtitle?.text = body
                buttonUnpin?.visibility = if (canChange) View.VISIBLE else View.GONE
                pinnedView?.setOnClickListener { presenter?.fireMessagesLookup(pinned); }
            }
        }
    }

    override fun hideInputView() {
        inputViewController?.run {
            switchModeToDisabled()
        }
    }

    override fun onVoiceHolderBinded(voiceMessageId: Int, voiceHolderId: Int) {
        presenter?.fireVoiceHolderCreated(voiceMessageId, voiceHolderId)
    }

    override fun onVoicePlayButtonClick(voiceHolderId: Int, voiceMessageId: Int, voiceMessage: VoiceMessage) {
        presenter?.fireVoicePlayButtonClick(voiceHolderId, voiceMessageId, voiceMessage)
    }

    override fun onTranscript(voiceMessageId: String, messageId: Int) {
        presenter?.fireTranscript(voiceMessageId, messageId)
    }

    override fun onStickerClick(sticker: Sticker) {
        presenter?.fireStickerSendClick(sticker)
    }

    override fun onHashTagClicked(hashTag: String) {
        presenter?.fireHashtagClick(hashTag)
    }

    override fun showActionMode(title: String, canEdit: Boolean, canPin: Boolean, canStar: Boolean, doStar: Boolean) {
        toolbarRootView?.run {
            if (childCount == 1) {
                val v = LayoutInflater.from(context).inflate(R.layout.view_actionmode, this, false)
                actionModeHolder = ActionModeHolder(v, this@ChatFragment)
                addView(v)
//                bringChildToFront(v)
            }
        }

        actionModeHolder?.show()
        actionModeHolder?.titleView?.text = title
        actionModeHolder?.buttonEdit?.visibility = if (canEdit) View.VISIBLE else View.GONE
        actionModeHolder?.buttonPin?.visibility = if (canPin) View.VISIBLE else View.GONE
        actionModeHolder?.buttonStar?.visibility = if (canStar) View.VISIBLE else View.GONE
        actionModeHolder?.buttonStar?.setImageResource(if (doStar) R.drawable.star_add else R.drawable.star_none)
    }

    override fun finishActionMode() {
        actionModeHolder?.rootView?.visibility = View.GONE
    }

    private fun onEditLocalPhotosSelected(photos: List<LocalPhoto>) {
        when (val defaultSize = Settings.get().main().uploadImageSize) {
            null -> {
                ImageSizeAlertDialog.Builder(activity)
                        .setOnSelectedCallback { size -> presenter?.fireEditLocalPhotosSelected(photos, size) }
                        .show()
            }
            else -> presenter?.fireEditLocalPhotosSelected(photos, defaultSize)
        }
    }

    private fun onEditLocalVideoSelected(video: LocalVideo) {
        presenter?.fireEditLocalVideoSelected(video)
    }

    private fun onEditLocalFileSelected(file: String) {
        MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select)
                .setNegativeButton(R.string.video) { _, _ ->
                    run {
                        presenter?.fireFileVideoForUploadSelected(file)
                    }
                }
                .setPositiveButton(R.string.photo) { _, _ ->
                    run {
                        when (val defaultSize = Settings.get().main().uploadImageSize) {
                            null -> {
                                ImageSizeAlertDialog.Builder(activity)
                                        .setOnSelectedCallback { size -> presenter?.fireFilePhotoForUploadSelected(file, size) }
                                        .show()
                            }
                            else -> presenter?.fireFilePhotoForUploadSelected(file, defaultSize)
                        }
                    }
                }
                .create().show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_EDIT_MESSAGE) {
            if (data != null && data.hasExtra(Extra.BUNDLE)) {
                val bundle = data.getParcelableExtra<ModelsBundle>(Extra.BUNDLE)
                if (bundle != null)
                    presenter?.fireEditMessageResult(bundle)
            }

            if (resultCode == Activity.RESULT_OK) {
                presenter?.fireSendClickFromAttachmens()
            }
        } else if (requestCode == REQUEST_ADD_VKPHOTO && resultCode == Activity.RESULT_OK) {
            val vkphotos: List<Photo> = data?.getParcelableArrayListExtra(Extra.ATTACHMENTS)
                    ?: Collections.emptyList()
            val localPhotos: List<LocalPhoto> = data?.getParcelableArrayListExtra(Extra.PHOTOS)
                    ?: Collections.emptyList()

            val vid: LocalVideo? = data?.getParcelableExtra(Extra.VIDEO)

            val file = data?.getStringExtra(FileManagerFragment.returnFileParameter)

            if (file != null && nonEmpty(file)) {
                onEditLocalFileSelected(file)
            } else if (vkphotos.isNotEmpty()) {
                presenter?.fireEditAttachmentsSelected(vkphotos)
            } else if (localPhotos.isNotEmpty()) {
                onEditLocalPhotosSelected(localPhotos)
            } else if (vid != null) {
                onEditLocalVideoSelected(vid)
            }
        } else if (requestCode == REQUEST_ADD_ATTACHMENT && resultCode == Activity.RESULT_OK) {
            val attachments: List<AbsModel> = data?.getParcelableArrayListExtra(Extra.ATTACHMENTS)
                    ?: Collections.emptyList()
            presenter?.fireEditAttachmentsSelected(attachments)
        } else if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            when (val defaultSize = Settings.get().main().uploadImageSize) {
                null -> {
                    ImageSizeAlertDialog.Builder(activity)
                            .setOnSelectedCallback { size -> presenter?.fireEditPhotoMaked(size) }
                            .show()
                }
                else -> presenter?.fireEditPhotoMaked(defaultSize)
            }
        } else if (requestCode == REQUEST_UPLOAD_CHAT_AVATAR && resultCode == Activity.RESULT_OK) {
            val photos: ArrayList<LocalPhoto>? = data?.getParcelableArrayListExtra(Extra.PHOTOS)
            if (nonEmpty(photos)) {
                presenter?.fireNewChatPhotoSelected(photos!![0])
            }
        }
    }

    override fun goToMessageAttachmentsEditor(
            accountId: Int, messageOwnerId: Int, destination: UploadDestination,
            body: String?, attachments: ModelsBundle?
    ) {
        val fragment = MessageAttachmentsFragment.newInstance(accountId, messageOwnerId, destination.id, attachments)
        fragment.setTargetFragment(this, REQUEST_EDIT_MESSAGE)
        fragment.show(parentFragmentManager, "message-attachments")
    }

    override fun startImagesSelection(accountId: Int, ownerId: Int) {
        val sources = Sources()
                .with(LocalPhotosSelectableSource())
                .with(LocalGallerySelectableSource())
                .with(LocalVideosSelectableSource())
                .with(VkPhotosSelectableSource(accountId, ownerId))
                .with(FileManagerSelectableSource())

        val intent = DualTabPhotoActivity.createIntent(activity, 10, sources)
        startActivityForResult(intent, REQUEST_ADD_VKPHOTO)
    }

    override fun startVideoSelection(accountId: Int, ownerId: Int) {
        val intent = VideoSelectActivity.createIntent(activity, accountId, ownerId)
        startActivityForResult(intent, REQUEST_ADD_ATTACHMENT)
    }

    override fun startAudioSelection(accountId: Int) {
        val intent = AudioSelectActivity.createIntent(activity, accountId)
        startActivityForResult(intent, REQUEST_ADD_ATTACHMENT)
    }

    override fun startDocSelection(accountId: Int) {
        val intent = AttachmentsActivity.createIntent(activity, accountId, Types.DOC)
        startActivityForResult(intent, REQUEST_ADD_ATTACHMENT)
    }

    private fun onEditCameraClick() {
        if (AppPerms.hasCameraPermision(requireContext())) {
            presenter?.fireEditCameraClick()
        } else {
            requestPermissions(arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSION_CAMERA_EDIT)
        }
    }

    override fun startCamera(fileUri: Uri) {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (intent.resolveActivity(requireActivity().packageManager) != null) {
            intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri)
            startActivityForResult(intent, REQUEST_PHOTO_FROM_CAMERA)
        }
    }

    override fun showDeleteForAllDialog(ids: ArrayList<Int>) {
        MaterialAlertDialogBuilder(requireActivity()).apply {
            setTitle(R.string.confirmation)
            setMessage(R.string.messages_delete_for_all_question_message)
            setNeutralButton(R.string.button_cancel, null)
            setPositiveButton(R.string.button_for_all) { _, _ -> presenter?.fireDeleteForAllClick(ids) }
            setNegativeButton(R.string.button_for_me) { _, _ -> presenter?.fireDeleteForMeClick(ids) }
        }.show()
    }

    override fun showSnackbar(@StringRes res: Int, isLong: Boolean) {
        val view = super.getView()
        if (Objects.nonNull(view)) {
            Snackbar.make(view!!, res, if (isLong) Snackbar.LENGTH_LONG else Snackbar.LENGTH_SHORT).setAnchorView(InputView).show()
        }
    }

    private class EditAttachmentsHolder(rootView: View, fragment: ChatFragment, attachments: MutableList<AttachmenEntry>) : AttachmentsBottomSheetAdapter.ActionListener, View.OnClickListener {
        override fun onClick(v: View) {
            when (v.id) {
                R.id.buttonHide -> reference.get()?.hideEditAttachmentsDialog()
                R.id.buttonSave -> reference.get()?.onEditAttachmentSaveClick()
                R.id.buttonVideo -> reference.get()?.presenter?.onEditAddVideoClick()
                R.id.buttonAudio -> reference.get()?.presenter?.onEditAddAudioClick()
                R.id.buttonDoc -> reference.get()?.presenter?.onEditAddDocClick()
                R.id.buttonCamera -> reference.get()?.onEditCameraClick()
            }
        }

        override fun onAddPhotoButtonClick() {
            reference.get()?.presenter?.fireEditAddImageClick()
        }

        override fun onButtonRemoveClick(entry: AttachmenEntry) {
            reference.get()?.presenter?.fireEditAttachmentRemoved(entry)
        }

        override fun onButtonRetryClick(entry: AttachmenEntry) {
            reference.get()?.presenter?.fireEditAttachmentRetry(entry)
        }

        val reference = WeakReference(fragment)
        val recyclerView: RecyclerView = rootView.findViewById(R.id.recyclerView)
        val emptyView: View = rootView.findViewById(R.id.no_attachments_text)
        val adapter = AttachmentsBottomSheetAdapter(rootView.context, attachments, this)

        init {
            recyclerView.layoutManager = LinearLayoutManager(rootView.context, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = adapter

            rootView.findViewById<View>(R.id.buttonHide).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonVideo).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonAudio).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonDoc).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonCamera).setOnClickListener(this)
            rootView.findViewById<View>(R.id.buttonSave).setOnClickListener(this)

            checkEmptyViewVisibility()
        }

        fun checkEmptyViewVisibility() {
            emptyView.visibility = if (adapter.itemCount < 2) View.VISIBLE else View.INVISIBLE
        }

        fun notifyAttachmentRemoved(index: Int) {
            adapter.notifyItemRemoved(index + 1)
            checkEmptyViewVisibility()
        }

        fun notifyAttachmentChanged(index: Int) {
            adapter.notifyItemChanged(index + 1)
        }

        fun notifyAttachmentsAdded(position: Int, count: Int) {
            adapter.notifyItemRangeInserted(position + 1, count)
            checkEmptyViewVisibility()
        }

        fun notifyAttachmentProgressUpdate(index: Int, progress: Int) {
            adapter.changeUploadProgress(index, progress, true)
        }
    }

    override fun notifyEditUploadProgressUpdate(index: Int, progress: Int) {
        editAttachmentsHolder?.notifyAttachmentProgressUpdate(index, progress)
    }

    override fun notifyEditAttachmentsAdded(position: Int, size: Int) {
        editAttachmentsHolder?.notifyAttachmentsAdded(position, size)
    }

    override fun notifyEditAttachmentChanged(index: Int) {
        editAttachmentsHolder?.notifyAttachmentChanged(index)
    }

    override fun notifyEditAttachmentRemoved(index: Int) {
        editAttachmentsHolder?.notifyAttachmentRemoved(index)
    }

    private fun onEditAttachmentSaveClick() {
        hideEditAttachmentsDialog()
        presenter?.fireEditMessageSaveClick()
    }

    private fun hideEditAttachmentsDialog() {
        editAttachmentsDialog?.dismiss()
    }

    override fun onSaveClick() {
        presenter?.fireEditMessageSaveClick()
    }

    private var editAttachmentsHolder: EditAttachmentsHolder? = null
    private var editAttachmentsDialog: Dialog? = null

    override fun showEditAttachmentsDialog(attachments: MutableList<AttachmenEntry>) {
        val view = View.inflate(requireActivity(), R.layout.bottom_sheet_attachments_edit, null)

        val reference = WeakReference(this)
        editAttachmentsHolder = EditAttachmentsHolder(view, this, attachments)
        editAttachmentsDialog = com.google.android.material.bottomsheet.BottomSheetDialog(requireActivity())
                .apply {
                    setContentView(view)
                    setOnDismissListener { reference.get()?.editAttachmentsHolder = null }
                    show()
                }
    }

    override fun showErrorSendDialog(message: Message) {
        val items = arrayOf(getString(R.string.try_again), getString(R.string.delete))

        MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.sending_message_failed)
                .setItems(items) { _, i ->
                    when (i) {
                        0 -> presenter?.fireSendAgainClick(message)
                        1 -> presenter?.fireErrorMessageDeleteClick(message)
                    }
                }
                .setCancelable(true)
                .show()
    }

    override fun notifyItemRemoved(position: Int) {
        adapter?.run {
            notifyItemRemoved(position + headersCount) // +headers count
        }
    }

    override fun configOptionMenu(
            canLeaveChat: Boolean, canChangeTitle: Boolean, canShowMembers: Boolean,
            encryptionStatusVisible: Boolean, encryprionEnabled: Boolean, encryptionPlusEnabled: Boolean, keyExchangeVisible: Boolean, HronoVisible: Boolean, ProfileVisible: Boolean
    ) {
        optionMenuSettings.put(LEAVE_CHAT_VISIBLE, canLeaveChat)
        optionMenuSettings.put(CHANGE_CHAT_TITLE_VISIBLE, canChangeTitle)
        optionMenuSettings.put(CHAT_MEMBERS_VISIBLE, canShowMembers)
        optionMenuSettings.put(ENCRYPTION_STATUS_VISIBLE, encryptionStatusVisible)
        optionMenuSettings.put(ENCRYPTION_ENABLED, encryprionEnabled)
        optionMenuSettings.put(ENCRYPTION_PLUS_ENABLED, encryptionPlusEnabled)
        optionMenuSettings.put(KEY_EXCHANGE_VISIBLE, keyExchangeVisible)
        optionMenuSettings.put(HRONO_VISIBLE, HronoVisible)
        optionMenuSettings.put(PROFILE_VISIBLE, ProfileVisible)

        try {
            PrepareOptionsMenu(toolbar?.menu!!)
        } catch (ignored: Exception) {

        }

    }

    override fun goToSearchMessage(accountId: Int, peer: Peer) {
        val criteria = MessageSeachCriteria("").setPeerId(peer.id)
        PlaceFactory.getSingleTabSearchPlace(accountId, SearchContentType.MESSAGES, criteria).tryOpenWith(requireActivity())
    }

    override fun showImageSizeSelectDialog(streams: List<Uri>) {
        ImageSizeAlertDialog.Builder(activity)
                .setOnSelectedCallback { size -> presenter?.fireImageUploadSizeSelected(streams, size) }
                .setOnCancelCallback { presenter?.fireUploadCancelClick() }
                .show()
    }

    override fun resetUploadImages() {
        ActivityUtils.resetInputPhotos(requireActivity())
    }

    override fun resetInputAttachments() {
        requireActivity().intent.removeExtra(MainActivity.EXTRA_INPUT_ATTACHMENTS)
        ActivityUtils.resetInputText(requireActivity())
    }

    private fun resolveToolbarNavigationIcon() {
        if (Objects.isNull(toolbar)) return
        val tr = AppCompatResources.getDrawable(requireActivity(), R.drawable.arrow_left)
        Utils.setColorFilter(tr, CurrentTheme.getColorPrimary(requireActivity()))
        toolbar!!.navigationIcon = tr
        toolbar!!.setNavigationOnClickListener { requireActivity().onBackPressed() }
    }

    private fun resolveLeftButton(peerId: Int) {
        try {
            if (Objects.nonNull(toolbar)) {
                resolveToolbarNavigationIcon()
                if (peerId < VKApiMessage.CHAT_PEER) {
                    Avatar?.setOnClickListener {
                        showUserWall(Settings.get().accounts().current, peerId)
                    }
                    Avatar?.setOnLongClickListener {
                        presenter?.fireLongAvatarClick(peerId)
                        true
                    }
                } else {
                    Avatar?.setOnClickListener {
                        ChatUsersDomainFragment.newInstance(Settings.get().accounts().current, Peer.toChatId(peerId)) { t -> insertDomain(t) }.show(childFragmentManager, "chat_users_domain")
                    }
                    Avatar?.setOnLongClickListener {
                        AppendMessageText("@all,")
                        true
                    }
                }
            }
        } catch (ignored: java.lang.Exception) {
        }
    }

    private fun insertDomain(owner: Owner) {
        if (nonEmpty(owner.domain)) {
            AppendMessageText("@" + owner.domain + ",")
        } else {
            AppendMessageText("@id" + owner.ownerId + ",")
        }
    }

    override fun notifyChatResume(accountId: Int, peerId: Int, title: String?, image: String?) {
        if (activity is OnSectionResumeCallback) {
            (activity as OnSectionResumeCallback).onChatResume(accountId, peerId, title, image)
        }
        Title?.text = title
        resolveLeftButton(peerId)
    }

    override fun goToConversationAttachments(accountId: Int, peerId: Int) {
        val types = arrayOf(FindAttachmentType.TYPE_PHOTO, FindAttachmentType.TYPE_VIDEO, FindAttachmentType.TYPE_DOC, FindAttachmentType.TYPE_AUDIO, FindAttachmentType.TYPE_LINK, FindAttachmentType.TYPE_POST)

        val menus = ModalBottomSheetDialogFragment.Builder()
        menus.add(OptionRequest(0, getString(R.string.photos), R.drawable.photo_album))
        menus.add(OptionRequest(1, getString(R.string.videos), R.drawable.video))
        menus.add(OptionRequest(2, getString(R.string.documents), R.drawable.book))
        menus.add(OptionRequest(3, getString(R.string.music), R.drawable.song))
        menus.add(OptionRequest(4, getString(R.string.links), R.drawable.web))
        menus.add(OptionRequest(5, getString(R.string.posts), R.drawable.pencil))

        menus.show(childFragmentManager, "attachments_select",
                object : ModalBottomSheetDialogFragment.Listener {
                    override fun onModalOptionSelected(option: Option) {
                        showConversationAttachments(accountId, peerId, types[option.id])
                    }
                })
    }

    private fun showConversationAttachments(accountId: Int, peerId: Int, type: String) {
        PlaceFactory.getConversationAttachmentsPlace(accountId, peerId, type).tryOpenWith(requireActivity())
    }

    override fun goToChatMembers(accountId: Int, chatId: Int) {
        PlaceFactory.getChatMembersPlace(accountId, chatId).tryOpenWith(requireActivity())
    }

    override fun showChatTitleChangeDialog(initialValue: String?) {
        MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.edit_chat)
                .setCancelable(true)
                .setPositiveButton(R.string.change_name) { _, _ ->
                    run {
                        InputTextDialog.Builder(requireActivity())
                                .setAllowEmpty(false)
                                .setInputType(InputType.TYPE_CLASS_TEXT)
                                .setValue(initialValue)
                                .setTitleRes(R.string.change_chat_title)
                                .setCallback { newValue -> presenter?.fireChatTitleTyped(newValue) }
                                .show()
                    }
                }
                .setNeutralButton(R.string.change_photo) { _, _ ->
                    val attachPhotoIntent = Intent(requireActivity(), PhotosActivity::class.java)
                    attachPhotoIntent.putExtra(PhotosActivity.EXTRA_MAX_SELECTION_COUNT, 1)
                    startActivityForResult(attachPhotoIntent, REQUEST_UPLOAD_CHAT_AVATAR)
                }
                .setNegativeButton(R.string.delete_photo) { _, _ -> presenter?.fireDeleteChatPhoto() }
                .show()
    }

    override fun showUserWall(accountId: Int, peerId: Int) {
        PlaceFactory.getOwnerWallPlace(accountId, peerId, null).tryOpenWith(requireActivity())
    }

    override fun forwardMessagesToAnotherConversation(messages: ArrayList<Message>, accountId: Int) {
        SendAttachmentsActivity.startForSendAttachments(requireActivity(), accountId, FwdMessages(messages))
    }

    override fun diplayForwardTypeSelectDialog(messages: ArrayList<Message>) {
        val items = arrayOf(getString(R.string.here), getString(R.string.to_another_dialogue))

        val listener = DialogInterface.OnClickListener { _, which ->
            when (which) {
                0 -> presenter?.fireForwardToHereClick(messages)
                1 -> presenter?.fireForwardToAnotherClick(messages)
            }
        }

        MaterialAlertDialogBuilder(requireActivity())
                .setItems(items, listener)
                .setCancelable(true)
                .show()
    }

    override fun setEmptyTextVisible(visible: Boolean) {
        emptyText?.visibility = if (visible) View.VISIBLE else View.GONE
    }

    override fun setupRecordPauseButton(available: Boolean, isPlaying: Boolean) {
        inputViewController?.setupRecordPauseButton(available, isPlaying)
    }

    override fun displayIniciateKeyExchangeQuestion(@KeyLocationPolicy keyStoragePolicy: Int) {
        MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.key_exchange)
                .setMessage(R.string.you_dont_have_encryption_keys_stored_initiate_key_exchange)
                .setPositiveButton(R.string.button_ok) { _, _ -> presenter?.fireIniciateKeyExchangeClick(keyStoragePolicy) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showEncryptionKeysPolicyChooseDialog(requestCode: Int) {
        val view = View.inflate(activity, R.layout.dialog_select_encryption_key_policy, null)
        val buttonOnDisk = view.findViewById<RadioButton>(R.id.button_on_disk)
        val buttonInRam = view.findViewById<RadioButton>(R.id.button_in_ram)

        buttonOnDisk.isChecked = true

        MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.choose_location_key_store)
                .setView(view)
                .setPositiveButton(R.string.button_ok) { _, _ ->
                    if (buttonOnDisk.isChecked) {
                        presenter?.fireDiskKeyStoreSelected(requestCode)
                    } else if (buttonInRam.isChecked) {
                        presenter?.fireRamKeyStoreSelected(requestCode)
                    }
                }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun showEncryptionDisclaimerDialog(requestCode: Int) {
        val view = View.inflate(activity, R.layout.content_encryption_terms_of_use, null)
        MaterialAlertDialogBuilder(requireActivity())
                .setView(view)
                .setTitle(R.string.phoenix_encryption)
                .setPositiveButton(R.string.button_accept) { _, _ -> presenter?.fireTermsOfUseAcceptClick(requestCode) }
                .setNegativeButton(R.string.button_cancel, null)
                .show()
    }

    override fun onResume() {
        super.onResume()
        ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(activity, true)
                .build()
                .apply(requireActivity())
    }

    @SuppressLint("ResourceType")
    fun PrepareOptionsMenu(menu: Menu) {

        menu.findItem(R.id.action_leave_chat).isVisible = optionMenuSettings.get(LEAVE_CHAT_VISIBLE, false)
        menu.findItem(R.id.action_edit_chat).isVisible = optionMenuSettings.get(CHANGE_CHAT_TITLE_VISIBLE, false)
        menu.findItem(R.id.action_chat_members).isVisible = optionMenuSettings.get(CHAT_MEMBERS_VISIBLE, false)
        menu.findItem(R.id.action_key_exchange).isVisible = optionMenuSettings.get(KEY_EXCHANGE_VISIBLE, false)
        menu.findItem(R.id.change_hrono_history).isVisible = optionMenuSettings.get(HRONO_VISIBLE, false)
        menu.findItem(R.id.show_profile).isVisible = optionMenuSettings.get(PROFILE_VISIBLE, false)

        val encryptionStatusItem = menu.findItem(R.id.crypt_state)
        val encryptionStatusVisible = optionMenuSettings.get(ENCRYPTION_STATUS_VISIBLE, false)

        encryptionStatusItem.isVisible = encryptionStatusVisible

        if (encryptionStatusVisible) {
            @DrawableRes
            var drawableRes = R.drawable.ic_outline_lock_open

            if (optionMenuSettings.get(ENCRYPTION_ENABLED, false)) {
                drawableRes = if (optionMenuSettings.get(ENCRYPTION_PLUS_ENABLED, false)) {
                    R.drawable.lock_plus
                } else {
                    R.drawable.ic_outline_lock
                }
            }

            try {
                encryptionStatusItem.setIcon(drawableRes)
            } catch (e: Exception) {
                //java.lang.NullPointerException: Attempt to invoke virtual method
                // 'android.content.res.Resources$Theme android.app.Activity.getTheme()' on a null object reference
            }

        }
    }

    override fun ScrollTo(position: Int) {
        recyclerView?.smoothScrollToPosition(position)
    }

    private fun OptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.last_read -> {
                recyclerView?.smoothScrollToPosition(presenter?.getConversation()!!.unreadCount)
                return true
            }
            R.id.action_refresh -> {
                recyclerView?.scrollToPosition(0)
                presenter?.reset_Hrono()
                presenter?.fireRefreshClick()
                return true
            }
            R.id.show_profile -> {
                presenter?.fireShow_Profile()
                return true
            }
            R.id.action_short_link -> {
                presenter?.fireShortLinkClick(requireActivity())
                return true
            }
            R.id.change_hrono_history -> {
                recyclerView?.scrollToPosition(0)
                presenter?.invert_Hrono()
                presenter?.fireRefreshClick()
                return true
            }
            R.id.action_leave_chat -> {
                presenter?.fireLeaveChatClick()
                return true
            }
            R.id.delete_chat -> {
                Snackbar.make(requireView(), R.string.delete_chat_do, Snackbar.LENGTH_LONG).setAction(R.string.button_yes
                ) { presenter?.removeDialog() }
                        .setAnchorView(InputView)
                        .setBackgroundTint(CurrentTheme.getColorPrimary(requireActivity())).setActionTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity())))
                            Color.parseColor("#ffffff") else Color.parseColor("#000000")).setTextColor(if (Utils.isColorDark(CurrentTheme.getColorPrimary(requireActivity())))
                            Color.parseColor("#ffffff") else Color.parseColor("#000000")).show()
                return true
            }
            R.id.action_edit_chat -> {
                presenter?.fireChatTitleClick()
                return true
            }
            R.id.action_chat_members -> {
                presenter?.fireChatMembersClick()
                return true
            }

            R.id.download_peer_to -> {
                if (!AppPerms.hasReadWriteStoragePermision(requireActivity())) {
                    AppPerms.requestReadWriteStoragePermission(requireActivity())
                    return true
                }
                MaterialAlertDialogBuilder(requireActivity())
                        .setTitle(R.string.confirmation)
                        .setMessage(R.string.select_type)
                        .setPositiveButton(R.string.to_json) { _, _ -> presenter?.fireChatDownloadClick(requireActivity(), "json") }
                        .setNegativeButton(R.string.to_html) { _, _ -> presenter?.fireChatDownloadClick(requireActivity(), "html") }
                        .setNeutralButton(R.string.button_cancel, null)
                        .show()
                return true
            }

            R.id.action_attachments_in_conversation -> presenter?.fireDialogAttachmentsClick()
            R.id.messages_search -> presenter?.fireSearchClick()
            R.id.crypt_state -> presenter?.fireEncriptionStatusClick()
            R.id.action_key_exchange -> presenter?.fireKeyExchangeClick()
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_RECORD_PERMISSIONS) {
            presenter?.fireRecordPermissionsResolved()
        }

        if (requestCode == REQUEST_PERMISSION_CAMERA_EDIT && AppPerms.hasCameraPermision(App.instance)) {
            presenter?.fireEditCameraClick()
        }
    }

    override fun onInputTextChanged(s: String) {
        presenter?.fireDraftMessageTextEdited(s)
        presenter?.fireTextEdited(s)
    }

    override fun onSendClicked(body: String) {
        presenter?.fireSendClick()
    }

    override fun onAttachClick() {
        presenter?.fireAttachButtonClick()
    }

    override fun onBackPressed(): Boolean {
        if (actionModeHolder?.isVisible() == true) {
            actionModeHolder?.hide()
            return false
        }

        if (inputViewController?.onBackPressed() == false) {
            return false
        }

        if (presenter!!.inInverHrono) {
            presenter?.reset_Hrono()
            presenter?.fireRefreshClick()
            return false
        }

        return presenter?.onBackPressed() == true
    }

    private fun isActionModeVisible(): Boolean {
        return actionModeHolder?.rootView?.visibility == View.VISIBLE
    }

    override fun onMessageDelete(message: Message) {
        val ids: ArrayList<Int> = ArrayList()
        ids.add(message.id)
        presenter?.fireDeleteForMeClick(ids)
    }

    override fun onAvatarClick(message: Message, userId: Int) {
        if (isActionModeVisible()) {
            presenter?.fireMessageClick(message)
        } else {
            presenter?.fireOwnerClick(userId)
        }
    }

    override fun onRestoreClick(message: Message, position: Int) {
        presenter?.fireMessageRestoreClick(message)
    }

    override fun onMessageLongClick(message: Message): Boolean {
        presenter?.fireMessageLongClick(message)
        return true
    }

    override fun onMessageClicked(message: Message) {
        presenter?.fireMessageClick(message)
    }

    override fun onLongAvatarClick(message: Message, userId: Int) {
        presenter?.fireLongAvatarClick(userId)
    }

    fun saveState() {
        inputViewController?.storeEmoji()
        presenter?.saveDraftMessageBody()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        inputViewController?.destroyView()
        inputViewController = null
    }


    companion object {

        private const val REQUEST_RECORD_PERMISSIONS = 15
        private const val REQUEST_EDIT_MESSAGE = 150
        private const val REQUEST_ADD_VKPHOTO = 151
        private const val REQUEST_ADD_ATTACHMENT = 152
        private const val REQUEST_PERMISSION_CAMERA_EDIT = 153
        private const val REQUEST_PHOTO_FROM_CAMERA = 154
        private const val REQUEST_UPLOAD_CHAT_AVATAR = 155

        fun newInstance(accountId: Int, messagesOwnerId: Int, peer: Peer): ChatFragment {
            val args = Bundle()
            args.putInt(Extra.ACCOUNT_ID, accountId)
            args.putInt(Extra.OWNER_ID, messagesOwnerId)
            args.putParcelable(Extra.PEER, peer)

            val fragment = ChatFragment()
            fragment.arguments = args
            return fragment
        }

        private const val LEAVE_CHAT_VISIBLE = 1
        private const val CHANGE_CHAT_TITLE_VISIBLE = 2
        private const val CHAT_MEMBERS_VISIBLE = 3
        private const val ENCRYPTION_STATUS_VISIBLE = 4
        private const val ENCRYPTION_ENABLED = 5
        private const val ENCRYPTION_PLUS_ENABLED = 6
        private const val KEY_EXCHANGE_VISIBLE = 7
        private const val HRONO_VISIBLE = 8
        private const val PROFILE_VISIBLE = 9
    }
}
