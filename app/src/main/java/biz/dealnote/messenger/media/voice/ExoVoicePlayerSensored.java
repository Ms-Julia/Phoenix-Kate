package biz.dealnote.messenger.media.voice;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;

import org.jetbrains.annotations.NotNull;

import biz.dealnote.messenger.Account_Types;
import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.media.exo.ExoUtil;
import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.model.VoiceMessage;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.util.Logger;
import biz.dealnote.messenger.util.Optional;
import biz.dealnote.messenger.util.Utils;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;

public class ExoVoicePlayerSensored implements IVoicePlayer, SensorEventListener {

    private final Context app;
    private final ProxyConfig proxyConfig;
    private final SensorManager sensorManager;
    private final Sensor proxym;
    private final PowerManager.WakeLock proximityWakelock;
    private final MusicIntentReceiver headset;
    private SimpleExoPlayer exoPlayer;
    private int status;
    private AudioEntry playingEntry;
    private boolean supposedToBePlaying;
    private IPlayerStatusListener statusListener;
    private IErrorListener errorListener;
    private boolean isProximityNear;
    private boolean isPlaying;
    private boolean HasPlaying;

    private boolean Registered;
    private boolean ProximitRegistered;
    private boolean isHeadset;

    public ExoVoicePlayerSensored(Context context, ProxyConfig config) {
        app = context.getApplicationContext();
        proxyConfig = config;
        status = STATUS_NO_PLAYBACK;
        headset = new MusicIntentReceiver();

        sensorManager = (SensorManager) app.getSystemService(Context.SENSOR_SERVICE);
        proxym = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        proximityWakelock = ((PowerManager) app.getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK, "phoenix:voip=proxim");
        Registered = false;
        ProximitRegistered = false;
        HasPlaying = false;
    }

    @Override
    public boolean toggle(int id, VoiceMessage audio) {
        if (nonNull(playingEntry) && playingEntry.getId() == id) {
            setSupposedToBePlaying(!isSupposedToPlay());
            return false;
        }

        release();

        playingEntry = new AudioEntry(id, audio);
        supposedToBePlaying = true;

        preparePlayer();
        return true;
    }

    private void RegisterCallBack() {
        if (Registered)
            return;
        try {
            Registered = true;
            if (MusicUtils.isPlaying() || MusicUtils.isPreparing()) {
                MusicUtils.notifyForegroundStateChanged(app, true);
                MusicUtils.playOrPause();
                HasPlaying = true;
            }
            isProximityNear = false;
            isHeadset = false;
            exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
            sensorManager.registerListener(this, proxym, SensorManager.SENSOR_DELAY_NORMAL);
            IntentFilter filter = new IntentFilter(Intent.ACTION_HEADSET_PLUG);
            app.registerReceiver(headset, filter);
        } catch (Exception ignored) {
        }
    }

    private void UnRegisterCallBack() {
        if (!Registered)
            return;
        try {
            Registered = false;
            sensorManager.unregisterListener(this);
            app.unregisterReceiver(headset);
            if (HasPlaying) {
                MusicUtils.playOrPause();
                MusicUtils.notifyForegroundStateChanged(app, false);
            }
            HasPlaying = false;
            if (ProximitRegistered) {
                ProximitRegistered = false;
                proximityWakelock.release();
            }
            isProximityNear = false;
            isHeadset = false;
            isPlaying = false;
        } catch (Exception ignored) {
        }
    }

    private void setStatus(int status) {
        if (this.status != status) {
            this.status = status;

            if (nonNull(statusListener)) {
                statusListener.onPlayerStatusChange(status);
            }
        }
    }

    private void preparePlayer() {
        isProximityNear = false;
        isHeadset = false;
        isPlaying = false;
        setStatus(STATUS_PREPARING);

        exoPlayer = new SimpleExoPlayer.Builder(app).build();
        exoPlayer.setWakeMode(C.WAKE_MODE_NETWORK);

        // DefaultBandwidthMeter bandwidthMeterA = new DefaultBandwidthMeter();
        // Produces DataSource instances through which media data is loaded.
        // DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, Util.getUserAgent(this, "exoplayer2example"), bandwidthMeterA);
        // DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(App.getInstance(), Util.getUserAgent(App.getInstance(), "exoplayer2example"), bandwidthMeterA);

        String userAgent = Constants.USER_AGENT(Account_Types.BY_TYPE);

        // This is the MediaSource representing the media to be played:
        // FOR SD CARD SOURCE:
        // MediaSource videoSource = new ExtractorMediaSource(mp4VideoUri, dataSourceFactory, extractorsFactory, null, null);
        // FOR LIVESTREAM LINK:

        String url = playingEntry.getAudio().getLinkMp3();

        MediaSource mediaSource = new ProgressiveMediaSource.Factory(Utils.getExoPlayerFactory(userAgent, proxyConfig)).createMediaSource(Utils.makeMediaItem(url));
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);
        exoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlaybackStateChanged(@Player.State int state) {
                onInternalPlayerStateChanged(state);
            }

            @Override
            public void onPlayWhenReadyChanged(boolean playWhenReady, @Player.PlayWhenReadyChangeReason int reason) {
                if (isPlaying != playWhenReady) {
                    isPlaying = playWhenReady;
                    if (isPlaying) {
                        RegisterCallBack();
                    } else {
                        UnRegisterCallBack();
                    }
                }
            }

            @Override
            public void onPlayerError(@NotNull ExoPlaybackException error) {
                onExoPlayerException(error);
                UnRegisterCallBack();
            }
        });

        exoPlayer.setPlayWhenReady(supposedToBePlaying);
        exoPlayer.setMediaSource(mediaSource);
        exoPlayer.prepare();
    }

    private void onExoPlayerException(ExoPlaybackException e) {
        if (nonNull(errorListener)) {
            errorListener.onPlayError(new PrepareException(e));
        }
    }

    private void onInternalPlayerStateChanged(@Player.State int state) {
        Logger.d("ExoVoicePlayer", "onInternalPlayerStateChanged, state: " + state);

        switch (state) {
            case Player.STATE_READY:
                setStatus(STATUS_PREPARED);
                break;
            case Player.STATE_ENDED:
                setSupposedToBePlaying(false);
                exoPlayer.seekTo(0);
                UnRegisterCallBack();
                break;
            case Player.STATE_BUFFERING:
            case Player.STATE_IDLE:
                break;
        }
    }

    private void setSupposedToBePlaying(boolean supposedToBePlaying) {
        this.supposedToBePlaying = supposedToBePlaying;

        if (supposedToBePlaying) {
            ExoUtil.startPlayer(exoPlayer);
        } else {
            ExoUtil.pausePlayer(exoPlayer);
        }
    }

    @Override
    public float getProgress() {
        if (isNull(exoPlayer)) {
            return 0f;
        }

        if (status != STATUS_PREPARED) {
            return 0f;
        }

        //long duration = playingEntry.getAudio().getDuration() * 1000;
        long duration = exoPlayer.getDuration();
        long position = exoPlayer.getCurrentPosition();
        return (float) position / (float) duration;
    }

    @Override
    public void setCallback(@Nullable IPlayerStatusListener listener) {
        statusListener = listener;
    }

    @Override
    public void setErrorListener(@Nullable IErrorListener errorListener) {
        this.errorListener = errorListener;
    }

    @Override
    public Optional<Integer> getPlayingVoiceId() {
        return isNull(playingEntry) ? Optional.empty() : Optional.wrap(playingEntry.getId());
    }

    @Override
    public boolean isSupposedToPlay() {
        return supposedToBePlaying;
    }

    @Override
    public void release() {
        try {
            if (nonNull(exoPlayer)) {
                exoPlayer.stop();
                exoPlayer.release();
                UnRegisterCallBack();
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (isHeadset)
            return;
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            boolean newIsNear = event.values[0] < Math.min(event.sensor.getMaximumRange(), 3);
            if (newIsNear != isProximityNear) {
                isProximityNear = newIsNear;
                try {
                    if (isProximityNear) {
                        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_SPEECH).setUsage(C.USAGE_VOICE_COMMUNICATION).build(), false);
                        if (!ProximitRegistered) {
                            ProximitRegistered = true;
                            proximityWakelock.acquire(10 * 60 * 1000L /*10 minutes*/);
                        }
                    } else {
                        if (ProximitRegistered) {
                            ProximitRegistered = false;
                            proximityWakelock.release(1); // this is non-public API before L
                        }
                        exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private class MusicIntentReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (java.util.Objects.equals(intent.getAction(), Intent.ACTION_HEADSET_PLUG)) {
                int state = intent.getIntExtra("state", -1);
                switch (state) {
                    case 0:
                        if (isHeadset) {
                            isHeadset = false;
                        }
                        break;
                    case 1:
                        if (!isHeadset) {
                            isHeadset = true;
                            isProximityNear = false;
                            try {
                                if (ProximitRegistered) {
                                    ProximitRegistered = false;
                                    proximityWakelock.release(1); // this is non-public API before L
                                }
                                exoPlayer.setAudioAttributes(new AudioAttributes.Builder().setContentType(C.CONTENT_TYPE_MUSIC).setUsage(C.USAGE_MEDIA).build(), true);
                            } catch (Exception ignored) {
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        }
    }
}