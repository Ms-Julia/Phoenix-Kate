package biz.dealnote.messenger.longpoll;

import android.content.Context;

import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.settings.ISettings;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Logger;

import static biz.dealnote.messenger.util.Utils.hasFlag;

public class LongPollNotificationHelper {

    public static final String TAG = LongPollNotificationHelper.class.getSimpleName();

    /**
     * Действие при добавлении нового сообщения в диалог или чат
     *
     * @param message нотификация с сервера
     */
    public static void notifyAbountNewMessage(Context context, Message message) {
        if (message.isOut()) {
            return;
        }

        //if (message.isRead()) {
        //    return;
        //}

        //boolean needSendNotif = needNofinicationFor(message.getAccountId(), message.getPeerId());
        //if(!needSendNotif){
        //    return;
        //}

        notifyAbountNewMessage(context, message.getAccountId(), message);
    }

    private static void notifyAbountNewMessage(Context context, int accountId, Message message) {
        int mask = Settings.get().notifications().getNotifPref(accountId, message.getPeerId());
        if (!hasFlag(mask, ISettings.INotificationSettings.FLAG_SHOW_NOTIF)) {
            return;
        }

        if (Settings.get().accounts().getCurrent() != accountId) {
            Logger.d(TAG, "notifyAbountNewMessage, Attempting to send a notification does not in the current account!!!");
            return;
        }

        NotificationHelper.notifNewMessage(context, accountId, message);
    }
}