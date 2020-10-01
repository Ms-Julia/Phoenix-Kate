package biz.dealnote.messenger.fragment.wallattachments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.VideosAdapter;
import biz.dealnote.messenger.fragment.base.PlaceSupportMvpFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.presenter.wallattachments.WallVideosAttachmentsPresenter;
import biz.dealnote.messenger.mvp.view.wallattachments.IWallVideosAttachmentsView;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class WallVideosAttachmentsFragment extends PlaceSupportMvpFragment<WallVideosAttachmentsPresenter, IWallVideosAttachmentsView>
        implements IWallVideosAttachmentsView, VideosAdapter.VideoOnClickListener {

    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private VideosAdapter mAdapter;
    private FloatingActionButton mLoadMore;

    public static WallVideosAttachmentsFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        WallVideosAttachmentsFragment fragment = new WallVideosAttachmentsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_wall_attachments, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.empty);
        mLoadMore = root.findViewById(R.id.goto_button);

        int columns = getContext().getResources().getInteger(R.integer.videos_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);

        RecyclerView recyclerView = root.findViewById(android.R.id.list);
        recyclerView.setLayoutManager(gridLayoutManager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });
        mLoadMore.setOnClickListener(v -> getPresenter().fireScrollToEnd());

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new VideosAdapter(requireActivity(), Collections.emptyList());
        mAdapter.setVideoOnClickListener(this);

        recyclerView.setAdapter(mAdapter);

        resolveEmptyText();
        return root;
    }

    private void resolveEmptyText() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayData(List<Video> videos) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(videos);
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyText();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyText();
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<WallVideosAttachmentsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new WallVideosAttachmentsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void setToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void setToolbarSubtitle(String subtitle) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setSubtitle(subtitle);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void onVideoClick(int position, Video video) {
        getPresenter().fireVideoClick(video);
    }

    @Override
    public void onSetLoadingStatus(int isLoad) {
        switch (isLoad) {
            case 1:
                mLoadMore.setImageResource(R.drawable.audio_died);
                break;
            case 2:
                mLoadMore.setImageResource(R.drawable.view);
                break;
            default:
                mLoadMore.setImageResource(R.drawable.ic_arrow_down);
                break;
        }
    }
}
