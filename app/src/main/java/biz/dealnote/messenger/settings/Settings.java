package biz.dealnote.messenger.settings;

import biz.dealnote.messenger.Injection;

public class Settings {

    public static ISettings get() {
        return Injection.provideSettings();
    }

}
