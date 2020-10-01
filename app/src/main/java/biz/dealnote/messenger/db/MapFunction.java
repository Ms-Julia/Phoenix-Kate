package biz.dealnote.messenger.db;

import android.database.Cursor;


public interface MapFunction<T> {
    T map(Cursor cursor);
}
