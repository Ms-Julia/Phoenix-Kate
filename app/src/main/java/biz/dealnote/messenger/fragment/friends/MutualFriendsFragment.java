package biz.dealnote.messenger.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.AbsOwnersListFragment;
import biz.dealnote.messenger.mvp.presenter.MutualFriendsPresenter;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class MutualFriendsFragment extends AbsOwnersListFragment<MutualFriendsPresenter, ISimpleOwnersView> {

    private static final String EXTRA_TARGET_ID = "targetId";
    private boolean isRequested;

    public static MutualFriendsFragment newInstance(int accountId, int targetId) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_TARGET_ID, targetId);
        bundle.putInt(Extra.ACCOUNT_ID, accountId);
        MutualFriendsFragment friendsFragment = new MutualFriendsFragment();
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRequested) {
            isRequested = true;
            getPresenter().doLoad();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<MutualFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new MutualFriendsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(EXTRA_TARGET_ID),
                saveInstanceState
        );
    }
}