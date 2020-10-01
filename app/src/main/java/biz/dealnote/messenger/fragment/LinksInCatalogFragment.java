package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.CatalogLinksAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.link.LinkHelper;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.mvp.presenter.LinksInCatalogPresenter;
import biz.dealnote.messenger.mvp.view.ILinksInCatalogView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class LinksInCatalogFragment extends BaseMvpFragment<LinksInCatalogPresenter, ILinksInCatalogView>
        implements ILinksInCatalogView, CatalogLinksAdapter.ActionListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private CatalogLinksAdapter mAdapter;
    private String Header;
    private boolean inTabsContainer;
    private boolean doAudioLoadTabs;

    public static LinksInCatalogFragment newInstance(int accountId, String block_id, String title) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putString(Extra.ID, block_id);
        args.putString(Extra.TITLE, title);
        LinksInCatalogFragment fragment = new LinksInCatalogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        Header = requireArguments().getString(Extra.TITLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_catalog_block, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);

        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 3));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });
        mAdapter = new CatalogLinksAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setActionListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(Header);
                actionBar.setSubtitle(R.string.links);
            }

            if (requireActivity() instanceof OnSectionResumeCallback) {
                ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_AUDIOS);
            }

            new ActivityFeatures.Builder()
                    .begin()
                    .setHideNavigationMenu(false)
                    .setBarsColored(requireActivity(), true)
                    .build()
                    .apply(requireActivity());
        }
        if (!doAudioLoadTabs) {
            doAudioLoadTabs = true;
            getPresenter().LoadAudiosTool();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<LinksInCatalogPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new LinksInCatalogPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getString(Extra.ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Link> links) {
        if (nonNull(mAdapter)) {
            mAdapter.setItems(links);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAdapter)) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void onLinkClick(int index, @NonNull Link doc) {
        LinkHelper.openLinkInBrowser(requireActivity(), doc.getUrl());
    }
}
