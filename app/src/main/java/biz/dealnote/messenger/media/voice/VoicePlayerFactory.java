package biz.dealnote.messenger.media.voice;

import android.content.Context;

import androidx.annotation.NonNull;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.settings.IProxySettings;
import biz.dealnote.messenger.settings.ISettings;

public class VoicePlayerFactory implements IVoicePlayerFactory {

    private final Context app;
    private final IProxySettings proxySettings;
    private final boolean NotSensered;

    public VoicePlayerFactory(Context context, IProxySettings proxySettings, ISettings.IOtherSettings otherSettings) {
        app = context.getApplicationContext();
        this.proxySettings = proxySettings;
        NotSensered = otherSettings.isDisable_sensored_voice();
    }

    @NonNull
    @Override
    public IVoicePlayer createPlayer() {
        ProxyConfig config = proxySettings.getActiveProxy();
        return NotSensered ? new ExoVoicePlayer(app, config) : new ExoVoicePlayerSensored(app, config);
    }
}