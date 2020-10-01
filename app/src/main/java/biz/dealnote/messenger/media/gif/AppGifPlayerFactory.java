package biz.dealnote.messenger.media.gif;

import androidx.annotation.NonNull;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.messenger.settings.IProxySettings;

public class AppGifPlayerFactory implements IGifPlayerFactory {

    private final IProxySettings proxySettings;

    public AppGifPlayerFactory(IProxySettings proxySettings) {
        this.proxySettings = proxySettings;
    }

    @Override
    public IGifPlayer createGifPlayer(@NonNull String url, boolean isRepeat) {
        ProxyConfig config = proxySettings.getActiveProxy();
        return new ExoGifPlayer(url, config, isRepeat);
    }
}