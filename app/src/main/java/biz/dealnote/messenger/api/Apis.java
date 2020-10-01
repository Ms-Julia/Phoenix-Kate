package biz.dealnote.messenger.api;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.api.interfaces.INetworker;

public class Apis {

    public static INetworker get() {
        return Injection.provideNetworkInterfaces();
    }

}
