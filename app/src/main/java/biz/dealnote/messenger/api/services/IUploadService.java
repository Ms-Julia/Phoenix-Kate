package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.upload.UploadAudioDto;
import biz.dealnote.messenger.api.model.upload.UploadChatPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadDocDto;
import biz.dealnote.messenger.api.model.upload.UploadOwnerPhotoDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToAlbumDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToMessageDto;
import biz.dealnote.messenger.api.model.upload.UploadPhotoToWallDto;
import biz.dealnote.messenger.api.model.upload.UploadVideoDto;
import io.reactivex.rxjava3.core.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Url;

public interface IUploadService {

    @Multipart
    @POST
    Single<UploadDocDto> uploadDocumentRx(@Url String server, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Single<UploadAudioDto> uploadAudioRx(@Url String server, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Single<UploadVideoDto> uploadVideoRx(@Url String server, @Part MultipartBody.Part file);

    @Multipart
    @POST
    Single<UploadOwnerPhotoDto> uploadOwnerPhotoRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadChatPhotoDto> uploadChatPhotoRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToWallDto> uploadPhotoToWallRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToMessageDto> uploadPhotoToMessageRx(@Url String server, @Part MultipartBody.Part photo);

    @Multipart
    @POST
    Single<UploadPhotoToAlbumDto> uploadPhotoToAlbumRx(@Url String server, @Part MultipartBody.Part file1);
}