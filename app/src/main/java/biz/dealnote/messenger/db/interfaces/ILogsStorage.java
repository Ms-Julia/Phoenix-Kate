package biz.dealnote.messenger.db.interfaces;

import java.util.List;

import biz.dealnote.messenger.model.LogEvent;
import io.reactivex.rxjava3.core.Single;


public interface ILogsStorage {

    Single<LogEvent> add(int type, String tag, String body);

    Single<List<LogEvent>> getAll(int type);

    void Clear();
}
