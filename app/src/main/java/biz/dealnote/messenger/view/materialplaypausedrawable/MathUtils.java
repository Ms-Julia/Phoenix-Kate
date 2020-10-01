/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package biz.dealnote.messenger.view.materialplaypausedrawable;

class MathUtils {

    private MathUtils() {
    }

    public static float lerp(float start, float end, float fraction) {
        return start + (end - start) * fraction;
    }
}
