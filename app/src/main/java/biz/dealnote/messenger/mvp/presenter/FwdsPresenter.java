package biz.dealnote.messenger.mvp.presenter;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.List;

import biz.dealnote.messenger.domain.Repository;
import biz.dealnote.messenger.model.Message;
import biz.dealnote.messenger.mvp.view.IFwdsView;
import biz.dealnote.messenger.util.RxUtils;
import biz.dealnote.messenger.util.Utils;

public class FwdsPresenter extends AbsMessageListPresenter<IFwdsView> {

    public FwdsPresenter(int accountId, List<Message> messages, @Nullable Bundle savedInstanceState) {
        super(accountId, savedInstanceState);
        if (!Utils.isEmpty(messages)) {
            getData().addAll(messages);
        }
    }

    public void fireTranscript(String voiceMessageId, int messageId) {
        appendDisposable(Repository.INSTANCE.getMessages().recogniseAudioMessage(getAccountId(), messageId, voiceMessageId)
                .compose(RxUtils.applySingleIOToMainSchedulers())
                .subscribe(v -> {
                }, t -> {
                }));
    }
}