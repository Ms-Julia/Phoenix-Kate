package biz.dealnote.messenger.fragment.fave;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.fave.FaveArticlesAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Article;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.mvp.presenter.FaveArticlesPresenter;
import biz.dealnote.messenger.mvp.view.IFaveArticlesView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class FaveArticlesFragment extends BaseMvpFragment<FaveArticlesPresenter, IFaveArticlesView>
        implements IFaveArticlesView, SwipeRefreshLayout.OnRefreshListener, FaveArticlesAdapter.ClickListener {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FaveArticlesAdapter mAdapter;
    private TextView mEmpty;
    private boolean isRequestLast;

    public static FaveArticlesFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        FaveArticlesFragment fragment = new FaveArticlesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_fave_articles, container, false);
        RecyclerView recyclerView = root.findViewById(android.R.id.list);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mEmpty = root.findViewById(R.id.empty);

        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new FaveArticlesAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);

        resolveEmptyTextVisibility();
        return root;
    }

    @Override
    public void onRefresh() {
        getPresenter().fireRefresh();
    }

    @Override
    public void displayData(List<Article> articles) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(articles);
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataSetChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
            resolveEmptyTextVisibility();
        }
    }

    @Override
    public void notifyDataAdded(int position, int count) {
        if (nonNull(mAdapter)) {
            mAdapter.notifyItemRangeInserted(position, count);
            resolveEmptyTextVisibility();
        }
    }

    private void resolveEmptyTextVisibility() {
        if (nonNull(mEmpty) && nonNull(mAdapter)) {
            mEmpty.setVisibility(mAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void showRefreshing(boolean refreshing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.post(() -> mSwipeRefreshLayout.setRefreshing(refreshing));
        }
    }

    @Override
    public void goToArticle(int accountId, String url) {
        LinkHelper.openLinkInBrowser(requireActivity(), url);
    }

    @Override
    public void goToPhoto(int accountId, Photo photo) {
        ArrayList<Photo> temp = new ArrayList<>(Collections.singletonList(photo));
        PlaceFactory.getSimpleGalleryPlace(accountId, temp, 0, false).tryOpenWith(requireActivity());
    }

    @NotNull
    @Override
    public IPresenterFactory<FaveArticlesPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new FaveArticlesPresenter(getArguments().getInt(Extra.ACCOUNT_ID), saveInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isRequestLast) {
            isRequestLast = true;
            getPresenter().LoadTool();
        }
    }

    @Override
    public void onUrlClick(String url) {
        getPresenter().fireArticleClick(url);
    }

    @Override
    public void onPhotosOpen(Photo photo) {
        getPresenter().firePhotoClick(photo);
    }

    @Override
    public void onDelete(int index, Article article) {
        getPresenter().fireArticleDelete(index, article);
    }
}
