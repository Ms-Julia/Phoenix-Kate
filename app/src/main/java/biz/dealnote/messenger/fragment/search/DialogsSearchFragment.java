package biz.dealnote.messenger.fragment.search;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.adapter.DialogPreviewAdapter;
import biz.dealnote.messenger.fragment.search.criteria.DialogsSearchCriteria;
import biz.dealnote.messenger.mvp.presenter.search.DialogsSearchPresenter;
import biz.dealnote.messenger.mvp.view.search.IDialogsSearchView;
import biz.dealnote.mvp.core.IPresenterFactory;

public class DialogsSearchFragment extends AbsSearchFragment<DialogsSearchPresenter, IDialogsSearchView, Object, DialogPreviewAdapter>
        implements IDialogsSearchView, DialogPreviewAdapter.ActionListener {

    public static DialogsSearchFragment newInstance(int accountId, DialogsSearchCriteria criteria) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.CRITERIA, criteria);
        DialogsSearchFragment fragment = new DialogsSearchFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NotNull
    @Override
    public IPresenterFactory<DialogsSearchPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = getArguments().getInt(Extra.ACCOUNT_ID);
            DialogsSearchCriteria criteria = getArguments().getParcelable(Extra.CRITERIA);
            return new DialogsSearchPresenter(accountId, criteria, saveInstanceState);
        };
    }

    @Override
    void setAdapterData(DialogPreviewAdapter adapter, List<Object> data) {
        adapter.setData(data);
    }

    @Override
    void postCreate(View root) {

    }

    @Override
    DialogPreviewAdapter createAdapter(List<Object> data) {
        return new DialogPreviewAdapter(requireActivity(), data, this);
    }

    @Override
    RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity());
    }

    @Override
    public void onEntryClick(Object o) {
        getPresenter().fireEntryClick(o);
    }
}