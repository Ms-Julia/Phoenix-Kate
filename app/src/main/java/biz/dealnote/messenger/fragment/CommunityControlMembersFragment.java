package biz.dealnote.messenger.fragment;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.mvp.presenter.CommunityMembersPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityMembersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class CommunityControlMembersFragment extends BaseMvpFragment<CommunityMembersPresenter, ICommunityMembersView>
        implements ICommunityMembersView {

    public static CommunityControlMembersFragment newInstance(int accountId, int groupId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        CommunityControlMembersFragment fragment = new CommunityControlMembersFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NotNull
    @Override
    public IPresenterFactory<CommunityMembersPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new CommunityMembersPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.GROUP_ID),
                saveInstanceState
        );
    }
}