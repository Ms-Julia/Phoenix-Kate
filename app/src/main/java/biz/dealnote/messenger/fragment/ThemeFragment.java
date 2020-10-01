package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.ThemeAdapter;
import biz.dealnote.messenger.model.ThemeValue;
import biz.dealnote.messenger.mvp.presenter.ThemePresenter;
import biz.dealnote.messenger.mvp.view.IThemeView;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.mvp.compat.AbsMvpFragment;
import biz.dealnote.mvp.core.IPresenterFactory;

public class ThemeFragment extends AbsMvpFragment<ThemePresenter, IThemeView> implements IThemeView, ThemeAdapter.ClickListener {

    private ThemeAdapter mAdapter;

    public static ThemeFragment newInstance() {
        Bundle args = new Bundle();
        ThemeFragment fragment = new ThemeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_theme, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        int columns = getContext().getResources().getInteger(R.integer.photos_column_count);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(requireActivity(), columns);
        recyclerView.setLayoutManager(gridLayoutManager);

        mAdapter = new ThemeAdapter(Collections.emptyList());
        mAdapter.setClickListener(this);
        recyclerView.setAdapter(mAdapter);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (Objects.nonNull(actionBar)) {
            actionBar.setTitle(R.string.theme_edit_title);
            actionBar.setSubtitle(null);
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<ThemePresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ThemePresenter(saveInstanceState);
    }

    @Override
    public void displayData(List<ThemeValue> data) {
        if (Objects.nonNull(mAdapter)) {
            mAdapter.setData(data);
        }
    }

    @Override
    public void onClick(int index, ThemeValue value) {
        Settings.get().ui().setMainTheme(value.id);
        requireActivity().recreate();
        mAdapter.notifyDataSetChanged();
    }
}
