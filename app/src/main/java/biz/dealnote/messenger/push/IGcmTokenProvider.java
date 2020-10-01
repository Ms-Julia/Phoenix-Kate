package biz.dealnote.messenger.push;

import java.io.IOException;

public interface IGcmTokenProvider {
    String getToken() throws IOException;
}