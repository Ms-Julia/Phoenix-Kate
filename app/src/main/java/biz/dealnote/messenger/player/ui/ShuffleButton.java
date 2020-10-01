package biz.dealnote.messenger.player.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;

import androidx.appcompat.widget.AppCompatImageButton;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.player.MusicPlaybackService;
import biz.dealnote.messenger.player.util.MusicUtils;

public class ShuffleButton extends AppCompatImageButton implements OnClickListener {
    public ShuffleButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        MusicUtils.cycleShuffle();
        updateShuffleState();
    }

    public void updateShuffleState() {
        switch (MusicUtils.getShuffleMode()) {
            case MusicPlaybackService.SHUFFLE:
                setImageResource(R.drawable.shuffle);
                break;
            case MusicPlaybackService.SHUFFLE_NONE:
                setImageResource(R.drawable.shuffle_disabled);
                break;
            default:
                break;
        }
    }

}
