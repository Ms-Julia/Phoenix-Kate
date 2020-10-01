package biz.dealnote.messenger.api.services;

import biz.dealnote.messenger.api.model.AudioCoverAmazon;
import io.reactivex.rxjava3.core.Single;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface IAudioCoverService {

    @GET("dev")
    Single<AudioCoverAmazon> getAudioCover(@Query("track") String track,
                                           @Query("artist") String artist);

}