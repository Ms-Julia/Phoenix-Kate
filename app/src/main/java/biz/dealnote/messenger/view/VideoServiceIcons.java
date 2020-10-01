package biz.dealnote.messenger.view;

import androidx.annotation.DrawableRes;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.model.VideoPlatform;

public class VideoServiceIcons {

    @DrawableRes
    public static Integer getIconByType(String platform) {
        if (platform == null) {
            return null;
        }

        switch (platform) {
            default:
                return null;
            case VideoPlatform.COUB:
                return R.drawable.logo_coub;
            case VideoPlatform.VIMEO:
                return R.drawable.logo_vimeo;
            case VideoPlatform.YOUTUBE:
                return R.drawable.logo_youtube_trans;
            case VideoPlatform.RUTUBE:
                return R.drawable.logo_rutube;
        }
    }
}