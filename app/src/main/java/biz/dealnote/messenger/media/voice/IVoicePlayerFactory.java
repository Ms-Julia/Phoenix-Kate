package biz.dealnote.messenger.media.voice;

import androidx.annotation.NonNull;

public interface IVoicePlayerFactory {
    @NonNull
    IVoicePlayer createPlayer();
}