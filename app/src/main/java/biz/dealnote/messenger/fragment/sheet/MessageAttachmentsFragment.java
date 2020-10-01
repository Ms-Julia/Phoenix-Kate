package biz.dealnote.messenger.fragment.sheet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.AttachmentsActivity;
import biz.dealnote.messenger.activity.AudioSelectActivity;
import biz.dealnote.messenger.activity.DualTabPhotoActivity;
import biz.dealnote.messenger.activity.VideoSelectActivity;
import biz.dealnote.messenger.adapter.AttachmentsBottomSheetAdapter;
import biz.dealnote.messenger.fragment.FileManagerFragment;
import biz.dealnote.messenger.model.AbsModel;
import biz.dealnote.messenger.model.AttachmenEntry;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.LocalVideo;
import biz.dealnote.messenger.model.ModelsBundle;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.Types;
import biz.dealnote.messenger.model.selection.FileManagerSelectableSource;
import biz.dealnote.messenger.model.selection.LocalGallerySelectableSource;
import biz.dealnote.messenger.model.selection.LocalPhotosSelectableSource;
import biz.dealnote.messenger.model.selection.LocalVideosSelectableSource;
import biz.dealnote.messenger.model.selection.Sources;
import biz.dealnote.messenger.model.selection.VkPhotosSelectableSource;
import biz.dealnote.messenger.mvp.presenter.MessageAttachmentsPresenter;
import biz.dealnote.messenger.mvp.view.IMessageAttachmentsView;
import biz.dealnote.messenger.service.ErrorLocalizer;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class MessageAttachmentsFragment extends AbsPresenterBottomSheetFragment<MessageAttachmentsPresenter,
        IMessageAttachmentsView> implements IMessageAttachmentsView, AttachmentsBottomSheetAdapter.ActionListener {

    private static final int REQUEST_ADD_VKPHOTO = 17;
    private static final int REQUEST_PERMISSION_CAMERA = 16;
    private static final int REQUEST_PHOTO_FROM_CAMERA = 15;
    private static final int REQUEST_SELECT_ATTACHMENTS = 14;
    private AttachmentsBottomSheetAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private View mEmptyView;

    public static MessageAttachmentsFragment newInstance(int accountId, int messageOwnerId, int messageId, ModelsBundle bundle) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.MESSAGE_ID, messageId);
        args.putInt(Extra.OWNER_ID, messageOwnerId);
        args.putParcelable(Extra.BUNDLE, bundle);
        MessageAttachmentsFragment fragment = new MessageAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(@NotNull Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = View.inflate(requireActivity(), R.layout.bottom_sheet_attachments, null);

        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));

        mEmptyView = view.findViewById(R.id.no_attachments_text);

        view.findViewById(R.id.button_send).setOnClickListener(v -> {
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_OK, null);
            getDialog().dismiss();
        });

        view.findViewById(R.id.button_hide).setOnClickListener(v -> getDialog().dismiss());
        view.findViewById(R.id.button_video).setOnClickListener(v -> getPresenter().fireButtonVideoClick());
        view.findViewById(R.id.button_audio).setOnClickListener(v -> getPresenter().fireButtonAudioClick());
        view.findViewById(R.id.button_doc).setOnClickListener(v -> getPresenter().fireButtonDocClick());
        view.findViewById(R.id.button_camera).setOnClickListener(v -> getPresenter().fireButtonCameraClick());
        view.findViewById(R.id.button_photo_settings).setOnClickListener(v -> getPresenter().fireCompressSettings(requireActivity()));

        dialog.setContentView(view);
        fireViewCreated();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle extras = nonNull(data) ? data.getExtras() : null;

        if (requestCode == REQUEST_ADD_VKPHOTO && resultCode == Activity.RESULT_OK) {
            ArrayList<Photo> vkphotos = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            ArrayList<LocalPhoto> localPhotos = data.getParcelableArrayListExtra(Extra.PHOTOS);
            String file = data.getStringExtra(FileManagerFragment.returnFileParameter);
            LocalVideo video = data.getParcelableExtra(Extra.VIDEO);
            getPresenter().firePhotosSelected(vkphotos, localPhotos, file, video);
        }

        if (requestCode == REQUEST_SELECT_ATTACHMENTS && resultCode == Activity.RESULT_OK) {
            ArrayList<AbsModel> attachments = data.getParcelableArrayListExtra(Extra.ATTACHMENTS);
            getPresenter().fireAttachmentsSelected(attachments);
        }

        if (requestCode == REQUEST_PHOTO_FROM_CAMERA && resultCode == Activity.RESULT_OK) {
            getPresenter().firePhotoMaked();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<MessageAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            int messageId = getArguments().getInt(Extra.MESSAGE_ID);
            int messageOwnerId = getArguments().getInt(Extra.OWNER_ID);
            ModelsBundle bundle = getArguments().getParcelable(Extra.BUNDLE);
            return new MessageAttachmentsPresenter(accountId, messageOwnerId, messageId, requireActivity(), bundle, saveInstanceState);
        };
    }

    @Override
    public void displayAttachments(List<AttachmenEntry> entries) {
        if (nonNull(mRecyclerView)) {
            mAdapter = new AttachmentsBottomSheetAdapter(requireActivity(), entries, this);
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void notifyDataAdded(int positionStart, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(positionStart + 1, count);
        }
    }

    @Override
    public void addPhoto(int accountId, int ownerId) {
        Sources sources = new Sources()
                .with(new LocalPhotosSelectableSource())
                .with(new LocalGallerySelectableSource())
                .with(new LocalVideosSelectableSource())
                .with(new VkPhotosSelectableSource(accountId, ownerId))
                .with(new FileManagerSelectableSource());

        Intent intent = DualTabPhotoActivity.createIntent(requireActivity(), 10, sources);
        startActivityForResult(intent, REQUEST_ADD_VKPHOTO);
    }

    @Override
    public void notifyEntryRemoved(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(index + 1);
        }
    }

    @Override
    public void displaySelectUploadPhotoSizeDialog(List<LocalPhoto> photos) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> getPresenter().fireUploadPhotoSizeSelected(photos, values[j]))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void displaySelectUploadFileSizeDialog(String file) {
        int[] values = {Upload.IMAGE_SIZE_800, Upload.IMAGE_SIZE_1200, Upload.IMAGE_SIZE_FULL};
        new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.select_image_size_title)
                .setItems(R.array.array_image_sizes_names, (dialogInterface, j)
                        -> getPresenter().fireUploadFileSizeSelected(file, values[j]))
                .setNegativeButton(R.string.button_cancel, null)
                .show();
    }

    @Override
    public void changePercentageSmoothly(int dataPosition, int progress) {
        if (nonNull(mAdapter)) {
            mAdapter.changeUploadProgress(dataPosition, progress, true);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemChanged(index + 1);
        }
    }

    @Override
    public void setEmptyViewVisible(boolean visible) {
        if (nonNull(mEmptyView)) {
            mEmptyView.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void requestCameraPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, REQUEST_PERMISSION_CAMERA);
    }

    @Override
    public void startCamera(@NonNull Uri fileUri) {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(requireActivity().getPackageManager()) != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);
            startActivityForResult(takePictureIntent, REQUEST_PHOTO_FROM_CAMERA);
        }
    }

    @Override
    public void syncAccompanyingWithParent(ModelsBundle accompanying) {
        if (nonNull(getTargetFragment())) {
            Intent data = new Intent()
                    .putExtra(Extra.BUNDLE, accompanying);
            getTargetFragment().onActivityResult(getTargetRequestCode(), Activity.RESULT_CANCELED, data);
        }
    }

    @Override
    public void startAddDocumentActivity(int accountId) {
        Intent intent = AttachmentsActivity.createIntent(requireActivity(), accountId, Types.DOC);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void startAddVideoActivity(int accountId, int ownerId) {
        Intent intent = VideoSelectActivity.createIntent(requireActivity(), accountId, ownerId);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void startAddAudioActivity(int accountId) {
        Intent intent = AudioSelectActivity.createIntent(requireActivity(), accountId);
        startActivityForResult(intent, REQUEST_SELECT_ATTACHMENTS);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CAMERA) {
            getPresenter().fireCameraPermissionResolved();
        }
    }

    @Override
    public void onAddPhotoButtonClick() {
        getPresenter().fireAddPhotoButtonClick();
    }

    @Override
    public void onButtonRemoveClick(AttachmenEntry entry) {
        getPresenter().fireRemoveClick(entry);
    }

    @Override
    public void onButtonRetryClick(AttachmenEntry entry) {
        getPresenter().fireRetryClick(entry);
    }

    @Override
    public void showError(String errorText) {
        if (isAdded()) {
            Utils.showRedTopToast(requireActivity(), errorText);
        }
    }

    @Override
    public void showThrowable(Throwable throwable) {
        if (isAdded()) {
            Snackbar.make(requireView(), ErrorLocalizer.localizeThrowable(Injection.provideApplicationContext(), throwable), BaseTransientBottomBar.LENGTH_LONG).setTextColor(Color.WHITE).setBackgroundTint(Color.parseColor("#eeff0000"))
                    .setAction(R.string.more_info, v -> {
                        StringBuilder Text = new StringBuilder();
                        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                            Text.append("    ");
                            Text.append(stackTraceElement);
                            Text.append("\r\n");
                        }
                        MaterialAlertDialogBuilder dlgAlert = new MaterialAlertDialogBuilder(requireActivity());
                        dlgAlert.setIcon(R.drawable.crash_ic_error);
                        dlgAlert.setMessage(Text);
                        dlgAlert.setTitle(R.string.more_info);

                        dlgAlert.setPositiveButton("OK", null);
                        dlgAlert.setCancelable(true);
                        dlgAlert.create().show();
                    }).setActionTextColor(Color.WHITE).show();
        }
    }

    @Override
    public void showError(int titleTes, Object... params) {
        if (isAdded()) {
            showError(getString(titleTes, params));
        }
    }

    @Override
    public PhoenixToast getPhoenixToast() {
        if (isAdded()) {
            return PhoenixToast.CreatePhoenixToast(requireActivity());
        }
        return PhoenixToast.CreatePhoenixToast(null);
    }
}