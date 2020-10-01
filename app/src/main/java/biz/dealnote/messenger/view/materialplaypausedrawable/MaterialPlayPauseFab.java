/*
 * Copyright (c) 2018 Zhang Hai <Dreaming.in.Code.ZH@Gmail.com>
 * All Rights Reserved.
 */

package biz.dealnote.messenger.view.materialplaypausedrawable;

import android.content.Context;
import android.util.AttributeSet;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class MaterialPlayPauseFab extends FloatingActionButton {

    private MaterialPlayPauseDrawable mDrawable;

    public MaterialPlayPauseFab(Context context) {
        super(context);

        init();
    }

    public MaterialPlayPauseFab(Context context, AttributeSet attrs) {
        super(context, attrs);

        init();
    }

    public MaterialPlayPauseFab(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init();
    }

    private void init() {
        mDrawable = new MaterialPlayPauseDrawable(getContext());
        setImageDrawable(mDrawable);
        // Otherwise ImageView tries to scale the drawable and it gets blurred.
        setScaleType(ScaleType.FIT_XY);
    }

    public void setAnimationDuration(long duration) {
        mDrawable.setAnimationDuration(duration);
    }

    public MaterialPlayPauseDrawable.State getState() {
        return mDrawable.getPlayPauseState();
    }

    public void setState(MaterialPlayPauseDrawable.State state) {
        mDrawable.setState(state);
    }

    public void jumpToState(MaterialPlayPauseDrawable.State state) {
        mDrawable.jumpToState(state);
    }
}
