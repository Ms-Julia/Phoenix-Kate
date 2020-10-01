package biz.dealnote.messenger.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.Document;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.model.PhotoSize;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AppTextUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.view.AspectRatioImageView;
import biz.dealnote.messenger.view.mozaik.MozaikLayout;

import static biz.dealnote.messenger.util.Utils.firstNonEmptyString;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class PhotosViewHelper {

    @PhotoSize
    private final int mPhotoPreviewSize;
    private final Context context;
    private final AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback;
    private final int mIconColorActive;

    PhotosViewHelper(Context context, @NonNull AttachmentsViewBinder.OnAttachmentsActionCallback attachmentsActionCallback) {
        this.context = context;
        this.attachmentsActionCallback = attachmentsActionCallback;
        mIconColorActive = CurrentTheme.getColorPrimary(context);
        mPhotoPreviewSize = Settings.get().main().getPrefPreviewImageSize();
    }

    @SuppressLint("SetTextI18n")
    public void displayVideos(List<PostImage> videos, ViewGroup container) {
        container.setVisibility(videos.isEmpty() ? View.GONE : View.VISIBLE);
        if (videos.isEmpty()) {
            return;
        }
        int i = videos.size() - container.getChildCount();

        for (int j = 0; j < i; j++) {
            View root = LayoutInflater.from(context).inflate(R.layout.item_video_attachment, container, false);
            VideoHolder holder = new VideoHolder(root);
            root.setTag(holder);
            Utils.setColorFilter(holder.ivPlay.getBackground(), mIconColorActive);
            container.addView(root);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            View tmpV = container.getChildAt(g);
            VideoHolder holder = (VideoHolder) tmpV.getTag();

            if (g < videos.size()) {
                PostImage image = videos.get(g);

                holder.vgPhoto.setOnClickListener(v -> {
                    if (image.getType() == PostImage.TYPE_VIDEO) {
                        Video video = (Video) image.getAttachment();
                        attachmentsActionCallback.onVideoPlay(video);
                    }
                });

                String url = image.getPreviewUrl(mPhotoPreviewSize);

                if (image.getType() == PostImage.TYPE_VIDEO) {
                    Video video = (Video) image.getAttachment();
                    holder.tvDelay.setText(AppTextUtils.getDurationString(video.getDuration()));
                    holder.tvTitle.setText(firstNonEmptyString(video.getTitle(), " "));
                }

                if (nonEmpty(url)) {
                    PicassoInstance.with()
                            .load(url)
                            .placeholder(R.drawable.background_gray)
                            .tag(Constants.PICASSO_TAG)
                            .into(holder.vgPhoto);

                    tmpV.setVisibility(View.VISIBLE);
                } else {
                    PicassoInstance.with().cancelRequest(holder.vgPhoto);
                    tmpV.setVisibility(View.GONE);
                }
            } else {
                tmpV.setVisibility(View.GONE);
            }
        }
    }

    public void displayPhotos(List<PostImage> photos, ViewGroup container) {
        container.setVisibility(photos.isEmpty() ? View.GONE : View.VISIBLE);

        if (photos.isEmpty()) {
            return;
        }

        int RoundedMode = Settings.get().main().getPhotoRoundMode();

        if (RoundedMode == 1)
            container.removeAllViews();

        int i = photos.size() - container.getChildCount();

        for (int j = 0; j < i; j++) {
            View root;
            switch (RoundedMode) {
                case 1:
                    root = photos.size() > 1 ? LayoutInflater.from(context).inflate(R.layout.item_photo_gif_not_round, container, false) :
                            LayoutInflater.from(context).inflate(R.layout.item_photo_gif, container, false);
                    break;
                case 2:
                    root = LayoutInflater.from(context).inflate(R.layout.item_photo_gif_not_round, container, false);
                    break;
                default:
                    root = LayoutInflater.from(context).inflate(R.layout.item_photo_gif, container, false);
                    break;
            }
            Holder holder = new Holder(root);
            root.setTag(holder);
            Utils.setColorFilter(holder.ivPlay.getBackground(), mIconColorActive);
            container.addView(root);
        }

        if (container instanceof MozaikLayout) {
            if (photos.size() > 10) {
                ArrayList<PostImage> images = new ArrayList<>(10);
                for (int s = 0; s < 10; s++)
                    images.add(photos.get(s));
                ((MozaikLayout) container).setPhotos(images);
            } else
                ((MozaikLayout) container).setPhotos(photos);
        }

        for (int g = 0; g < container.getChildCount(); g++) {
            View tmpV = container.getChildAt(g);
            Holder holder = (Holder) tmpV.getTag();

            if (g < photos.size()) {
                PostImage image = photos.get(g);

                holder.ivPlay.setVisibility(image.getType() == PostImage.TYPE_IMAGE ? View.GONE : View.VISIBLE);
                holder.tvTitle.setVisibility(image.getType() == PostImage.TYPE_IMAGE ? View.GONE : View.VISIBLE);

                int position = g;

                holder.vgPhoto.setOnClickListener(v -> {
                    switch (image.getType()) {
                        case PostImage.TYPE_IMAGE:
                            openImages(photos, position);
                            break;
                        case PostImage.TYPE_VIDEO:
                            Video video = (Video) image.getAttachment();
                            attachmentsActionCallback.onVideoPlay(video);
                            break;
                        case PostImage.TYPE_GIF:
                            Document document = (Document) image.getAttachment();
                            attachmentsActionCallback.onDocPreviewOpen(document);
                            break;
                    }
                });

                String url = image.getPreviewUrl(mPhotoPreviewSize);

                switch (image.getType()) {
                    case PostImage.TYPE_VIDEO:
                        Video video = (Video) image.getAttachment();
                        holder.tvTitle.setText(AppTextUtils.getDurationString(video.getDuration()));
                        break;
                    case PostImage.TYPE_GIF:
                        Document document = (Document) image.getAttachment();
                        holder.tvTitle.setText(context.getString(R.string.gif, AppTextUtils.getSizeString(document.getSize())));
                        break;
                }

                if (nonEmpty(url)) {
                    PicassoInstance.with()
                            .load(url)
                            .placeholder(R.drawable.background_gray)
                            .tag(Constants.PICASSO_TAG)
                            .into(holder.vgPhoto);

                    tmpV.setVisibility(View.VISIBLE);
                } else {
                    PicassoInstance.with().cancelRequest(holder.vgPhoto);
                    tmpV.setVisibility(View.GONE);
                }
            } else {
                tmpV.setVisibility(View.GONE);
            }
        }
    }

    private void openImages(List<PostImage> photos, int index) {
        ArrayList<Photo> models = new ArrayList<>();

        for (PostImage postImage : photos) {
            if (postImage.getType() == PostImage.TYPE_IMAGE) {
                models.add((Photo) postImage.getAttachment());
            }
        }

        attachmentsActionCallback.onPhotosOpen(models, index, true);
    }

    private static class Holder {

        final ShapeableImageView vgPhoto;
        final ImageView ivPlay;
        final TextView tvTitle;

        Holder(View itemView) {
            vgPhoto = itemView.findViewById(R.id.item_video_image);
            ivPlay = itemView.findViewById(R.id.item_video_play);
            tvTitle = itemView.findViewById(R.id.item_video_title);
        }
    }

    private static class VideoHolder {

        final AspectRatioImageView vgPhoto;
        final ImageView ivPlay;
        final TextView tvTitle;
        final TextView tvDelay;

        VideoHolder(View itemView) {
            vgPhoto = itemView.findViewById(R.id.item_video_album_image);
            ivPlay = itemView.findViewById(R.id.item_video_play);
            tvTitle = itemView.findViewById(R.id.item_video_album_title);
            tvDelay = itemView.findViewById(R.id.item_video_album_count);
        }
    }
}
