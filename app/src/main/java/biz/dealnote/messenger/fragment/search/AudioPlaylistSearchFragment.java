package biz.dealnote.messenger.fragment.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.AudioPlaylistsAdapter;
import biz.dealnote.messenger.fragment.search.criteria.AudioPlaylistSearchCriteria;
import biz.dealnote.messenger.model.AudioPlaylist;
import biz.dealnote.messenger.mvp.presenter.search.AudioPlaylistSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IAudioPlaylistSearchView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

public class AudioPlaylistSearchFragment extends AbsSearchFragment<AudioPlaylistSearchPresenter, IAudioPlaylistSearchView, AudioPlaylist, AudioPlaylistsAdapter>
        implements AudioPlaylistsAdapter.ClickListener, IAudioPlaylistSearchView {

    public static final String ACTION_SELECT = "AudioPlaylistSearchFragment.ACTION_SELECT";
    private boolean isSelectMode;

    public static AudioPlaylistSearchFragment newInstance(int accountId, @Nullable AudioPlaylistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        AudioPlaylistSearchFragment fragment = new AudioPlaylistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AudioPlaylistSearchFragment newInstanceSelect(int accountId, @Nullable AudioPlaylistSearchCriteria initialCriteria) {
        Bundle args = new Bundle();
        args.putParcelable(Extra.CRITERIA, initialCriteria);
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putBoolean(ACTION_SELECT, true);
        AudioPlaylistSearchFragment fragment = new AudioPlaylistSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isSelectMode = requireArguments().getBoolean(ACTION_SELECT);
    }

    @Override
    void setAdapterData(AudioPlaylistsAdapter adapter, List<AudioPlaylist> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    AudioPlaylistsAdapter createAdapter(List<AudioPlaylist> data) {
        AudioPlaylistsAdapter ret = new AudioPlaylistsAdapter(data, requireActivity());
        ret.setClickListener(this);
        return ret;
    }

    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new GridLayoutManager(requireActivity(), 2);
    }

    @NotNull
    @Override
    public IPresenterFactory<AudioPlaylistSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new AudioPlaylistSearchPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                getArguments().getParcelable(Extra.CRITERIA),
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
            if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
                PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
            else
                PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(requireActivity());
        }
    }

    @Override
    public void onOpenClick(int index, AudioPlaylist album) {
        if (Utils.isEmpty(album.getOriginal_access_key()) || album.getOriginal_id() == 0 || album.getOriginal_owner_id() == 0)
            PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOwnerId(), album.getId(), album.getAccess_key()).tryOpenWith(requireActivity());
        else
            PlaceFactory.getAudiosInAlbumPlace(getPresenter().getAccountId(), album.getOriginal_owner_id(), album.getOriginal_id(), album.getOriginal_access_key()).tryOpenWith(requireActivity());
    }

    @Override
    public void onDelete(int index, AudioPlaylist album) {

    }

    @Override
    public void onAdd(int index, AudioPlaylist album) {
        getPresenter().onAdd(album);
    }
}
