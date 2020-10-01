package biz.dealnote.messenger.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.view.materialplaypausedrawable.MaterialPlayPauseDrawable;
import biz.dealnote.messenger.view.materialplaypausedrawable.MaterialPlayPauseFab;

public class PlayPauseButton extends MaterialPlayPauseFab implements OnClickListener {

    public PlayPauseButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MusicUtils.playOrPause();
        updateState();
    }

    public void updateState() {
        if (MusicUtils.isPlaying()) {
            setState(MaterialPlayPauseDrawable.State.Pause);
        } else {
            setState(MaterialPlayPauseDrawable.State.Play);
        }
    }

}
