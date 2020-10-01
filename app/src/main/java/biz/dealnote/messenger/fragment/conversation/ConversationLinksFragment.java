package biz.dealnote.messenger.fragment.conversation;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.LinksAdapter;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.mvp.presenter.conversations.ChatAttachmentLinksPresenter;
import biz.dealnote.messenger.mvp.view.conversations.IChatAttachmentLinksView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class ConversationLinksFragment extends AbsChatAttachmentsFragment<Link, ChatAttachmentLinksPresenter, IChatAttachmentLinksView>
        implements LinksAdapter.ActionListener, IChatAttachmentLinksView {

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        LinksAdapter simpleDocRecycleAdapter = new LinksAdapter(Collections.emptyList());
        simpleDocRecycleAdapter.setActionListener(this);
        return simpleDocRecycleAdapter;
    }

    @Override
    public void displayAttachments(List<Link> data) {
        if (getAdapter() instanceof LinksAdapter) {
            ((LinksAdapter) getAdapter()).setItems(data);
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<ChatAttachmentLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatAttachmentLinksPresenter(
                getArguments().getInt(Extra.PEER_ID),
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onLinkClick(int index, @NonNull Link link) {
        getPresenter().fireLinkClick(link);
    }
}
