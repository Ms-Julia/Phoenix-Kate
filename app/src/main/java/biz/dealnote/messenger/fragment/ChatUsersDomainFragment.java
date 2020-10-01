package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.ChatMembersListDomainAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpBottomSheetDialogFragment;
import biz.dealnote.messenger.model.AppChatUser;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.presenter.ChatUsersDomainPresenter;
import biz.dealnote.messenger.mvp.view.IChatUsersDomainView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class ChatUsersDomainFragment extends BaseMvpBottomSheetDialogFragment<ChatUsersDomainPresenter, IChatUsersDomainView>
        implements IChatUsersDomainView, ChatMembersListDomainAdapter.ActionListener {

    private ChatMembersListDomainAdapter mAdapter;
    private Listener listener;

    private static Bundle buildArgs(int accountId, int chatId) {
        Bundle args = new Bundle();
        args.putInt(Extra.CHAT_ID, chatId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        return args;
    }

    public static ChatUsersDomainFragment newInstance(int accountId, int chatId, Listener listener) {
        ChatUsersDomainFragment fragment = new ChatUsersDomainFragment();
        fragment.listener = listener;
        fragment.setArguments(buildArgs(accountId, chatId));
        return fragment;
    }

    @NotNull
    public BottomSheetDialog onCreateDialog(Bundle savedInstanceState) {
        BottomSheetDialog dialog = new BottomSheetDialog(requireActivity(), getTheme());
        BottomSheetBehavior<FrameLayout> behavior = dialog.getBehavior();
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);
        return dialog;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup root = (ViewGroup) inflater.inflate(R.layout.fragment_chat_users_domain, container, false);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        mAdapter = new ChatMembersListDomainAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setActionListener(this);
        recyclerView.setAdapter(mAdapter);

        return root;
    }

    @Override
    public void displayData(List<AppChatUser> users) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(users);
        }
    }

    @Override
    public void notifyItemRemoved(int position) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void openUserWall(int accountId, Owner user) {
        if (nonNull(listener)) {
            listener.onSelected(user);
        }
    }

    @Override
    public void displayRefreshing(boolean refreshing) {

    }

    @NotNull
    @Override
    public IPresenterFactory<ChatUsersDomainPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatUsersDomainPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getInt(Extra.CHAT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onUserClick(AppChatUser user) {
        getPresenter().fireUserClick(user);
    }

    interface Listener {
        void onSelected(Owner user);
    }
}
