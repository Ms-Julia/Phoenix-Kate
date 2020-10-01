package biz.dealnote.messenger.util;

import biz.dealnote.messenger.BuildConfig;


public class Analytics {

    public static void logUnexpectedError(Throwable throwable) {
        if (BuildConfig.DEBUG) {
            throwable.printStackTrace();
        }

        //FirebaseCrash.report(throwable);
    }
}