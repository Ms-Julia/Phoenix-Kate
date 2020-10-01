package biz.dealnote.messenger.db.interfaces;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.IOException;
import java.util.List;

import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.model.LocalImageAlbum;
import biz.dealnote.messenger.model.LocalPhoto;
import biz.dealnote.messenger.model.LocalVideo;
import biz.dealnote.messenger.picasso.Content_Local;
import io.reactivex.rxjava3.core.Single;


public interface ILocalMediaStorage extends IStorage {

    Single<List<LocalPhoto>> getPhotos(long albumId);

    Single<List<LocalPhoto>> getPhotos();

    Single<List<LocalImageAlbum>> getImageAlbums();

    Single<List<LocalVideo>> getVideos();

    Single<List<Audio>> getAudios(int accountId);

    Bitmap getOldThumbnail(@Content_Local int type, long content_Id);

    Bitmap getThumbnail(Uri uri, int x, int y) throws IOException;
}
