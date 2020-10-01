package biz.dealnote.messenger.api.interfaces;

public interface INetworker {

    IAccountApis vkDefault(int accountId);

    IAccountApis vkManual(int accountId, String accessToken);

    IAuthApi vkDirectAuth();

    IAuthApi vkAuth();

    IAudioCoverApi amazonAudioCover();

    ILongpollApi longpoll();

    IUploadApi uploads();
}