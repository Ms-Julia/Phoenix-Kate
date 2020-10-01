package biz.dealnote.messenger.fragment.friends;

import android.os.Bundle;

import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.fragment.AbsOwnersListFragment;
import biz.dealnote.messenger.mvp.presenter.RecommendationsFriendsPresenter;
import biz.dealnote.messenger.mvp.view.ISimpleOwnersView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class RecommendationsFriendsFragment extends AbsOwnersListFragment<RecommendationsFriendsPresenter, ISimpleOwnersView> {

    private boolean isRequested;

    public static RecommendationsFriendsFragment newInstance(int accoutnId, int userId) {
        Bundle bundle = new Bundle();
        bundle.putInt(Extra.USER_ID, userId);
        bundle.putInt(Extra.ACCOUNT_ID, accoutnId);
        RecommendationsFriendsFragment friendsFragment = new RecommendationsFriendsFragment();
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
    public IPresenterFactory<RecommendationsFriendsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RecommendationsFriendsPresenter(
                getArguments().getInt(Extra.USER_ID),
                saveInstanceState);
    }
}
