package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.AudioPlaylistsAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.presenter.AudioPlaylistsPresenter;
import biz.dealnote.messenger.mvp.view.IAudioPlaylistsView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.MySearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class AudioPlaylistsFragment extends BaseMvpFragment<AudioPlaylistsPresenter, IAudioPlaylistsView> implements IAudioPlaylistsView, AudioPlaylistsAdapter.ClickListener {

    public static final String EXTRA_IN_TABS_CONTAINER = "in_tabs_container";
    public static final String ACTION_SELECT = "AudioPlaylistsFragment.ACTION_SELECT";
    private TextView mEmpty;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioPlaylistsAdapter mAdapter;
    private boolean doAudioLoadTabs;
    private boolean inTabsContainer;
    private boolean isSelectMode;

    public static AudioPlaylistsFragment newInstance(int accountId, int ownerId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, ownerId);
        AudioPlaylistsFragment fragment = new AudioPlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioPlaylistsFragment newInstanceSelect(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.OWNER_ID, accountId);
        args.putBoolean(ACTION_SELECT, true);
        AudioPlaylistsFragment fragment = new AudioPlaylistsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        inTabsContainer = requireArguments().getBoolean(EXTRA_IN_TABS_CONTAINER);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_audio_playlist, container, false);
        Toolbar toolbar = root.findViewById(R.id.toolbar);
        if (!inTabsContainer) {
            toolbar.setVisibility(View.VISIBLE);
            ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        } else {
            toolbar.setVisibility(View.GONE);
        }
        mEmpty = root.findViewById(R.id.fragment_audio_playlist_empty_text);

        RecyclerView recyclerView = root.findViewById(R.id.recycleView);
        recyclerView.setLayoutManager(new GridLayoutManager(requireActivity(), 2));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
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

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh(false));
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new AudioPlaylistsAdapter(Collections.emptyList(), requireActivity());
        mAdapter.setClickListener(this);

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
    public void onResume() {
        super.onResume();
        if (!inTabsContainer) {
            Settings.get().ui().notifyPlaceResumed(Place.AUDIOS);
            ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
            if (actionBar != null) {
                actionBar.setTitle(R.string.playlists);
                actionBar.setSubtitle(null);
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

    @Override
    public void displayData(List<AudioPlaylist> playlists) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(playlists);
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
    public IPresenterFactory<AudioPlaylistsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioPlaylistsPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getInt(Extra.OWNER_ID),
                saveInstanceState
        );
    }

    @Override
    public void onAlbumClick(int index, AudioPlaylist album) {
        if (isSelectMode) {
            Intent intent = new Intent();
            intent.putParcelableArrayListExtra(Extra.ATTACHMENTS, new ArrayList<>(Collections.singleton(album)));
            requireActivity().setResult(Activity.RESULT_OK, intent);
            requireActivity().finish();
        } else {
            PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
        }
    }

    @Override
    public void onOpenClick(int index, AudioPlaylist album) {
        PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onDelete(int index, AudioPlaylist album) {
        getPresenter().onDelete(index, album);
    }

    @Override
    public void onAdd(int index, AudioPlaylist album) {
        getPresenter().onAdd(album);
    }
}
