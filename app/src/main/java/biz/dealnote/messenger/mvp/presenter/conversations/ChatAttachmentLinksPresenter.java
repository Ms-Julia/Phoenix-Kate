package biz.dealnote.messenger.mvp.presenter.conversations;

import android.os.Bundle;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.api.Apis;
import biz.dealnote.messenger.api.model.VKApiAttachment;
import biz.dealnote.messenger.api.model.VKApiLink;
import biz.dealnote.messenger.api.model.response.AttachmentsHistoryResponse;
import biz.dealnote.messenger.domain.mappers.Dto2Model;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.mvp.view.conversations.IChatAttachmentLinksView;
import biz.dealnote.messenger.util.Pair;
import biz.dealnote.mvp.reflect.OnGuiCreated;
import io.reactivex.rxjava3.core.Single;

import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.safeCountOf;

public class ChatAttachmentLinksPresenter extends BaseChatAttachmentsPresenter<Link, IChatAttachmentLinksView> {

    public ChatAttachmentLinksPresenter(int peerId, int accountId, @Nullable Bundle savedInstanceState) {
        super(peerId, accountId, savedInstanceState);
    }

    @Override
    void onDataChanged() {
        super.onDataChanged();
        resolveToolbar();
    }

    @Override
    Single<Pair<String, List<Link>>> requestAttachments(int peerId, String nextFrom) {
        return Apis.get().vkDefault(getAccountId())
                .messages()
                .getHistoryAttachments(peerId, VKApiAttachment.TYPE_LINK, nextFrom, 50, null)
                .map(response -> {
                    List<Link> docs = new ArrayList<>(safeCountOf(response.items));

                    if (nonNull(response.items)) {
                        for (AttachmentsHistoryResponse.One one : response.items) {
                            if (nonNull(one) && nonNull(one.entry) && one.entry.attachment instanceof VKApiLink) {
                                VKApiLink dto = (VKApiLink) one.entry.attachment;
                                docs.add(Dto2Model.transform(dto));
                            }
                        }
                    }

                    return Pair.Companion.create(response.next_from, docs);
                });
    }

    @OnGuiCreated
    private void resolveToolbar() {
        if (isGuiReady()) {
            getView().setToolbarTitle(getString(R.string.attachments_in_chat));
            getView().setToolbarSubtitle(getString(R.string.links_count, safeCountOf(data)));
        }
    }
}
