package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;

import java.util.ArrayList;

import biz.dealnote.messenger.model.Message;

public interface IImportantMessagesView extends IBasicMessageListView, IErrorView {
    void showRefreshing(boolean refreshing);

    void notifyDataAdded(int position, int count);

    void forwardMessages(int accountId, @NonNull ArrayList<Message> messages);

    void goToMessagesLookup(int accountId, int peerId, int messageId);
}
