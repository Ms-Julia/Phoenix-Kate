package biz.dealnote.messenger.util;

import android.annotation.SuppressLint;

import java.io.PrintWriter;
import java.io.StringWriter;

import biz.dealnote.messenger.Injection;
import biz.dealnote.messenger.db.interfaces.ILogsStorage;
import biz.dealnote.messenger.model.LogEvent;
import biz.dealnote.messenger.settings.Settings;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class PersistentLogger {

    @SuppressLint("CheckResult")
    public static void logThrowable(String tag, Throwable throwable) {
        if (!Settings.get().other().isDeveloper_mode())
            return;
        ILogsStorage store = Injection.provideLogsStore();
        Throwable cause = Utils.getCauseIfRuntime(throwable);

        getStackTrace(cause)
                .flatMapCompletable(s -> store.add(LogEvent.Type.ERROR, tag, s)
                        .ignoreElement())
                .onErrorComplete()
                .subscribeOn(Schedulers.io())
                .subscribe(() -> {
                }, ignore -> {
                });
    }

    private static Single<String> getStackTrace(Throwable throwable) {
        return Single.fromCallable(() -> {
            try (StringWriter sw = new StringWriter();
                 PrintWriter pw = new PrintWriter(sw)) {
                throwable.printStackTrace(pw);
                return sw.toString();
            }
        });
    }
}