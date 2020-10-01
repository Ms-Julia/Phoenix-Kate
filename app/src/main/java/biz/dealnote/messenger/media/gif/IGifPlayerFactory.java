package biz.dealnote.messenger.media.gif;

import androidx.annotation.NonNull;

public interface IGifPlayerFactory {
    IGifPlayer createGifPlayer(@NonNull String url, boolean isRepeat);
}