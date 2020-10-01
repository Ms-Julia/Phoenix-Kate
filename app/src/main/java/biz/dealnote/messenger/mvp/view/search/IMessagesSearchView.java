package biz.dealnote.messenger.mvp.view.search;

import biz.dealnote.messenger.model.Message;


public interface IMessagesSearchView extends IBaseSearchView<Message> {

    void goToMessagesLookup(int accountId, int peerId, int messageId);
}
