package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.adapter.AudioLocalRecyclerAdapter;
import biz.dealnote.messenger.adapter.DocsUploadAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.model.Audio;
import biz.dealnote.messenger.mvp.presenter.AudiosLocalPresenter;
import biz.dealnote.messenger.mvp.view.IAudiosLocalView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.player.util.MusicUtils;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.upload.Upload;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.MySearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class AudiosLocalFragment extends BaseMvpFragment<AudiosLocalPresenter, IAudiosLocalView>
        implements MySearchView.OnQueryTextListener, DocsUploadAdapter.ActionListener, AudioLocalRecyclerAdapter.ClickListener, IAudiosLocalView {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AudioLocalRecyclerAdapter mAudioRecyclerAdapter;
    private boolean doAudioLoadTabs;
    private DocsUploadAdapter mUploadAdapter;
    private View mUploadRoot;

    public static AudiosLocalFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudiosLocalFragment fragment = new AudiosLocalFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_local_music, container, false);

        MySearchView searchView = root.findViewById(R.id.searchview);
        searchView.setOnQueryTextListener(this);
        searchView.setRightButtonVisibility(false);
        searchView.setLeftIcon(R.drawable.magnify);
        searchView.setQuery("", true);

        RecyclerView uploadRecyclerView = root.findViewById(R.id.uploads_recycler_view);
        uploadRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
        mUploadAdapter = new DocsUploadAdapter(Collections.emptyList(), this);
        uploadRecyclerView.setAdapter(mUploadAdapter);
        mUploadRoot = root.findViewById(R.id.uploads_root);

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        FloatingActionButton Goto = root.findViewById(R.id.goto_button);
        Goto.setImageResource(R.drawable.audio_player);

        Goto.setOnLongClickListener(v -> {
            Audio curr = MusicUtils.getCurrentAudio();
            if (curr != null) {
                PlaceFactory.getPlayerPlace(Settings.get().accounts().getCurrent()).tryOpenWith(requireActivity());
            } else
                PhoenixToast.CreatePhoenixToast(requireActivity()).showToastError(R.string.null_audio);
            return false;
        });
        Goto.setOnClickListener(v -> {
            Audio curr = MusicUtils.getCurrentAudio();
            if (curr != null) {
                int index = getPresenter().getAudioPos(curr);
                if (index >= 0) {
                    if (Settings.get().other().isShow_audio_cover())
                        recyclerView.scrollToPosition(index);
                    else
                        recyclerView.smoothScrollToPosition(index);
                } else
                    PhoenixToast.CreatePhoenixToast(requireActivity()).showToast(R.string.audio_not_found);
            } else
                PhoenixToast.CreatePhoenixToast(requireActivity()).showToastError(R.string.null_audio);
        });
        mAudioRecyclerAdapter = new AudioLocalRecyclerAdapter(requireActivity(), Collections.emptyList());
        mAudioRecyclerAdapter.setClickListener(this);
        recyclerView.setAdapter(mAudioRecyclerAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!doAudioLoadTabs) {
            doAudioLoadTabs = true;
            getPresenter().LoadAudiosTool();
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<AudiosLocalPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudiosLocalPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void displayList(List<Audio> audios) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.setItems(audios);
        }
    }

    @Override
    public void notifyListChanged() {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void displayRefreshing(boolean refresing) {
        if (nonNull(mSwipeRefreshLayout)) {
            mSwipeRefreshLayout.setRefreshing(refresing);
        }
    }

    @Override
    public void setUploadDataVisible(boolean visible) {
        if (nonNull(mUploadRoot)) {
            mUploadRoot.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void displayUploads(List<Upload> data) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.setData(data);
        }
    }

    @Override
    public void notifyItemChanged(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemChanged(index);
        }
    }

    @Override
    public void notifyItemRemoved(int index) {
        if (nonNull(mAudioRecyclerAdapter)) {
            mAudioRecyclerAdapter.notifyItemRemoved(index);
        }
    }

    @Override
    public void notifyUploadDataChanged() {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void notifyUploadItemsAdded(int position, int count) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRangeInserted(position, count);
        }
    }

    @Override
    public void notifyUploadItemChanged(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemChanged(position);
        }
    }

    @Override
    public void notifyUploadItemRemoved(int position) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.notifyItemRemoved(position);
        }
    }

    @Override
    public void notifyUploadProgressChanged(int position, int progress, boolean smoothly) {
        if (nonNull(mUploadAdapter)) {
            mUploadAdapter.changeUploadProgress(position, progress, smoothly);
        }
    }

    @Override
    public void onRemoveClick(Upload upload) {
        getPresenter().fireRemoveClick(upload);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        getPresenter().fireQuery(query);
        return true;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        getPresenter().fireQuery(newText);
        return false;
    }

    @Override
    public void onClick(int position, Audio audio) {
        getPresenter().playAudio(requireActivity(), position);
    }

    @Override
    public void onDelete(int position) {
        getPresenter().fireDelete(position);
    }

    @Override
    public void onUpload(int position, Audio audio) {
        getPresenter().fireFileForUploadSelected(audio.getUrl());
    }
}
