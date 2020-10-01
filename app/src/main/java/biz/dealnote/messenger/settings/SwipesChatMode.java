package biz.dealnote.messenger.settings;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({SwipesChatMode.DISABLED, SwipesChatMode.V1, SwipesChatMode.V2})
@Retention(RetentionPolicy.SOURCE)
public @interface SwipesChatMode {
    int DISABLED = 0;
    int V1 = 1;
    int V2 = 2;
}
