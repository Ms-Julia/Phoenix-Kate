package biz.dealnote.messenger.realtime;

import static biz.dealnote.messenger.util.Objects.isNull;

public final class Processors {

    private static IRealtimeMessagesProcessor realtimeMessagesProcessor;

    public static IRealtimeMessagesProcessor realtimeMessages() {
        if (isNull(realtimeMessagesProcessor)) {
            synchronized (Processors.class) {
                if (isNull(realtimeMessagesProcessor)) {
                    realtimeMessagesProcessor = new RealtimeMessagesProcessor();
                }
            }
        }
        return realtimeMessagesProcessor;
    }
}