package biz.dealnote.messenger.db.impl;

import android.content.ContentUris;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.db.interfaces.ILocalMediaStorage;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.LocalVideo;
import biz.dealnote.messenger.picasso.Content_Local;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.util.Objects;
import io.reactivex.rxjava3.core.Single;

import static biz.dealnote.messenger.util.Utils.safeCountOf;

class LocalMediaStorage extends AbsStorage implements ILocalMediaStorage {

    private static final String[] PROJECTION = {BaseColumns._ID, MediaStore.MediaColumns.DATA};
    private static final String[] VIDEO_PROJECTION = {BaseColumns._ID, MediaStore.MediaColumns.DURATION, MediaStore.MediaColumns.SIZE, MediaStore.MediaColumns.DISPLAY_NAME};

    @RequiresApi(api = Build.VERSION_CODES.Q)
    private static final String[] AUDIO_PROJECTION = {BaseColumns._ID, MediaStore.MediaColumns.DURATION, MediaStore.MediaColumns.DISPLAY_NAME};

    LocalMediaStorage(@NonNull AppStorages mRepositoryContext) {
        super(mRepositoryContext);
    }

    private static LocalVideo mapVideo(Cursor cursor) {
        return new LocalVideo(cursor.getLong(cursor.getColumnIndex(BaseColumns._ID)), PicassoInstance.buildUriForPicassoNew(Content_Local.VIDEO, cursor.getLong(cursor.getColumnIndex(BaseColumns._ID))))
                .setDuration(cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.DURATION)))
                .setSize(cursor.getLong(cursor.getColumnIndex(MediaStore.MediaColumns.SIZE)))
                .setTitle(cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)));
    }

    private static Audio mapAudio(int accountId, Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
        String data = PicassoInstance.buildUriForPicassoNew(Content_Local.AUDIO, id).toString();


        String TrackName = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)).replace(".mp3", "");
        String Artist = "";
        String[] arr = TrackName.split(" - ");
        if (arr.length > 1) {
            Artist = arr[0];
            TrackName = TrackName.replace(Artist + " - ", "");
        }

        int dur = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns.DURATION));
        if (dur != 0) {
            dur /= 1000;
        }

        Audio ret = new Audio().setId(data.hashCode()).setOwnerId(accountId).setDuration(dur)
                .setUrl(data).setTitle(TrackName).setArtist(Artist);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return ret.setThumb_image_big(data).setThumb_image_little(data);
        } else {
            String uri = PicassoInstance.buildUriForPicasso(Content_Local.AUDIO, id).toString();
            return ret.setThumb_image_big(uri).setThumb_image_little(uri);
        }
    }

    @Override
    public Single<List<LocalVideo>> getVideos() {
        return Single.create(e -> {
            Cursor cursor = getContentResolver().query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    VIDEO_PROJECTION, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

            ArrayList<LocalVideo> data = new ArrayList<>(safeCountOf(cursor));
            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    data.add(mapVideo(cursor));
                }
                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public Single<List<Audio>> getAudios(int accountId) {
        return Single.create(e -> {
            Cursor cursor = getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    AUDIO_PROJECTION, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

            ArrayList<Audio> data = new ArrayList<>(safeCountOf(cursor));
            if (Objects.nonNull(cursor)) {
                while (cursor.moveToNext()) {
                    data.add(mapAudio(accountId, cursor));
                }
                cursor.close();
            }

            e.onSuccess(data);
        });
    }

    @Override
    public Single<List<LocalPhoto>> getPhotos(long albumId) {
        return Single.create(e -> {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, MediaStore.MediaColumns.BUCKET_ID + " = ?",
                    new String[]{String.valueOf(albumId)},
                    MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

            ArrayList<LocalPhoto> result = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) break;

                    long imageId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

                    result.add(new LocalPhoto()
                            .setImageId(imageId)
                            .setFullImageUri(Uri.parse(data)));
                }

                cursor.close();
            }

            e.onSuccess(result);
        });
    }

    @Override
    public Single<List<LocalPhoto>> getPhotos() {
        return Single.create(e -> {
            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    PROJECTION, null, null,
                    MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

            ArrayList<LocalPhoto> result = new ArrayList<>(safeCountOf(cursor));
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) break;

                    long imageId = cursor.getLong(cursor.getColumnIndex(BaseColumns._ID));
                    String data = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));

                    result.add(new LocalPhoto()
                            .setImageId(imageId)
                            .setFullImageUri(Uri.parse(data)));
                }

                cursor.close();
            }

            e.onSuccess(result);
        });
    }

    private boolean hasAlbumById(int albumId, List<LocalImageAlbum> albums) {
        for (LocalImageAlbum i : albums) {
            if (i.getId() == albumId) {
                i.setPhotoCount(i.getPhotoCount() + 1);
                return true;
            }
        }
        return false;
    }

    @Override
    public Single<List<LocalImageAlbum>> getImageAlbums() {
        return Single.create(e -> {
            final String album = MediaStore.MediaColumns.BUCKET_DISPLAY_NAME;
            final String albumId = MediaStore.MediaColumns.BUCKET_ID;
            final String data = MediaStore.MediaColumns.DATA;
            final String coverId = BaseColumns._ID;
            String[] projection = {album, albumId, data, coverId};

            Cursor cursor = getContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null, MediaStore.MediaColumns.DATE_MODIFIED + " DESC");

            List<LocalImageAlbum> albums = new ArrayList<>(safeCountOf(cursor));

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    if (e.isDisposed()) break;

                    if (!hasAlbumById(cursor.getInt(1), albums)) {
                        albums.add(new LocalImageAlbum()
                                .setId(cursor.getInt(1))
                                .setName(cursor.getString(0))
                                .setCoverPath(cursor.getString(2))
                                .setCoverId(cursor.getLong(3))
                                .setPhotoCount(1));
                    }
                }

                cursor.close();
            }

            e.onSuccess(albums);
        });
    }

    @Override
    public Bitmap getOldThumbnail(@Content_Local int type, long content_Id) {
        if (type == Content_Local.PHOTO) {
            return MediaStore.Images.Thumbnails.getThumbnail(getContext().getContentResolver(),
                    content_Id, MediaStore.Images.Thumbnails.MINI_KIND, null);
        } else if (type == Content_Local.AUDIO) {
            MediaMetadataRetriever mediaMetadataRetriever = new MediaMetadataRetriever();
            Uri oo = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, content_Id);
            mediaMetadataRetriever.setDataSource(getContext(), oo);
            InputStream is = new ByteArrayInputStream(mediaMetadataRetriever.getEmbeddedPicture());
            try {
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                bitmap = Bitmap.createScaledBitmap(bitmap, 256, 256, false);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
        return MediaStore.Video.Thumbnails.getThumbnail(getContext().getContentResolver(),
                content_Id, MediaStore.Video.Thumbnails.MINI_KIND, null);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public Bitmap getThumbnail(Uri uri, int x, int y) throws IOException {
        return getContext().getContentResolver().loadThumbnail(uri, new Size(x, y), null);
    }
}
