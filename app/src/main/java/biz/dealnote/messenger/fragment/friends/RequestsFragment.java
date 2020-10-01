package biz.dealnote.messenger.fragment.friends;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.FriendsRecycleAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UsersPart;
import biz.dealnote.messenger.mvp.presenter.RequestsPresenter;
import biz.dealnote.messenger.mvp.view.IRequestsView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.MySearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class RequestsFragment extends BaseMvpFragment<RequestsPresenter, IRequestsView>
        implements FriendsRecycleAdapter.Listener, IRequestsView {

    private FriendsRecycleAdapter mAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private boolean isRequested;

    public static RequestsFragment newInstance(int accountId, int userId) {
        Bundle args = new Bundle();
        args.putInt(Extra.USER_ID, userId);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        RequestsFragment allFriendsFragment = new RequestsFragment();
        allFriendsFragment.setArguments(args);
        return allFriendsFragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRequested) {
            isRequested = true;
            getPresenter().doLoad();
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        requireActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        View root = inflater.inflate(R.layout.fragment_friends, container, false);
        RecyclerView mRecyclerView = root.findViewById(R.id.list);
        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());

        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        LinearLayoutManager manager = new LinearLayoutManager(requireActivity(), RecyclerView.VERTICAL, false);
        mRecyclerView.setLayoutManager(manager);
        mRecyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        mRecyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        MySearchView mySearchView = root.findViewById(R.id.searchview);
        mySearchView.setRightButtonVisibility(false);
        mySearchView.setLeftIcon(R.drawable.magnify);
        mySearchView.setOnQueryTextListener(new MySearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getPresenter().fireSearchRequestChanged(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                getPresenter().fireSearchRequestChanged(newText);
                return false;
            }
        });

        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mySearchView.getWindowToken(), 0);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            root.findViewById(R.id.appbar).setElevation(0);
        }

        mAdapter = new FriendsRecycleAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setListener(this);
        mRecyclerView.setAdapter(mAdapter);
        return root;
    }

    @NotNull
    @Override
    public IPresenterFactory<RequestsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new RequestsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.USER_ID), saveInstanceState
        );
    }

    @Override
    public void notifyDatasetChanged(boolean enabled) {
        if (nonNull(mAdapter)) {
            mAdapter.setGroup(enabled);
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setSwipeRefreshEnabled(boolean enabled) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setEnabled(enabled);
        }
    }

    @Override
    public void displayData(List<UsersPart> data, boolean grouping) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(data, grouping);
        }
    }

    @Override
    public void notifyItemRangeInserted(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void showUserWall(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @Override
    public void onUserClick(User user) {
        getPresenter().fireUserClick(user);
    }
}
