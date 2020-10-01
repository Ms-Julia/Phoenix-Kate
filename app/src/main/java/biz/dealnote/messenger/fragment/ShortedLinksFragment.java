package biz.dealnote.messenger.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.ShortedLinksAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.EndlessRecyclerOnScrollListener;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.listener.PicassoPauseOnScrollListener;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.model.ShortLink;
import biz.dealnote.messenger.mvp.presenter.ShortedLinksPresenter;
import biz.dealnote.messenger.mvp.view.IShortedLinksView;
import biz.dealnote.messenger.place.Place;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class ShortedLinksFragment extends BaseMvpFragment<ShortedLinksPresenter, IShortedLinksView> implements IShortedLinksView, ShortedLinksAdapter.ClickListener {

    private TextView mEmpty;
    private TextInputEditText mLink;
    private MaterialButton do_Short;
    private MaterialButton do_Validate;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ShortedLinksAdapter mAdapter;

    public static ShortedLinksFragment newInstance(int accountId) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        ShortedLinksFragment fragment = new ShortedLinksFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_shorted_links, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));
        mEmpty = root.findViewById(R.id.fragment_shorted_links_empty_text);

        RecyclerView.LayoutManager manager = new LinearLayoutManager(requireActivity());
        RecyclerView recyclerView = root.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(manager);
        recyclerView.addOnScrollListener(new PicassoPauseOnScrollListener(Constants.PICASSO_TAG));
        recyclerView.addOnScrollListener(new EndlessRecyclerOnScrollListener() {
            @Override
            public void onScrollToLastElement() {
                getPresenter().fireScrollToEnd();
            }
        });

        mLink = root.findViewById(R.id.input_url);
        do_Short = root.findViewById(R.id.do_short);
        do_Validate = root.findViewById(R.id.do_validate);

        do_Short.setEnabled(false);
        do_Validate.setEnabled(false);
        mLink.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                do_Validate.setEnabled(!Utils.isEmpty(s));
                do_Short.setEnabled(!Utils.isEmpty(s));
                getPresenter().fireInputEdit(s);
            }
        });

        do_Short.setOnClickListener(v -> getPresenter().fireShort());
        do_Validate.setOnClickListener(v -> getPresenter().fireValidate());

        mSwipeRefreshLayout = root.findViewById(R.id.refresh);
        mSwipeRefreshLayout.setOnRefreshListener(() -> getPresenter().fireRefresh());
        ViewUtils.setupSwipeRefreshLayoutWithCurrentTheme(requireActivity(), mSwipeRefreshLayout);

        mAdapter = new ShortedLinksAdapter(Collections.emptyList(), requireActivity());
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
    public void displayData(List<ShortLink> links) {
        if (nonNull(mAdapter)) {
            mAdapter.setData(links);
            resolveEmptyText();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Settings.get().ui().notifyPlaceResumed(Place.DIALOGS);

        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (actionBar != null) {
            actionBar.setTitle(R.string.short_link);
            actionBar.setSubtitle(null);
        }

        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onSectionResume(AdditionalNavigationFragment.SECTION_ITEM_DIALOGS);
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
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

    @Override
    public void updateLink(String url) {
        mLink.setText(url);
        mLink.setSelection(mLink.getText().length());
        do_Short.setEnabled(false);
        do_Validate.setEnabled(false);
    }

    @Override
    public void showLinkStatus(String status) {
        String stat = "";
        int color = Color.parseColor("#ff0000");

        switch (status) {
            case "not_banned":
                stat = getString(R.string.link_not_banned);
                color = Color.parseColor("#cc00aa00");
                break;
            case "banned":
                stat = getString(R.string.link_banned);
                color = Color.parseColor("#ccaa0000");
                break;
            case "processing":
                stat = getString(R.string.link_processing);
                color = Color.parseColor("#cc0000aa");
                break;
        }
        int text_color = Utils.isColorDark(color)
                ? Color.parseColor("#ffffff") : Color.parseColor("#000000");

        Snackbar.make(mLink, stat, BaseTransientBottomBar.LENGTH_LONG)
                .setBackgroundTint(color).setTextColor(text_color).setAnchorView(R.id.recycler_view).show();
    }

    @NotNull
    @Override
    public IPresenterFactory<ShortedLinksPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ShortedLinksPresenter(
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }

    @Override
    public void onCopy(int index, ShortLink link) {
        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("response", link.getShort_url());
        clipboard.setPrimaryClip(clip);
        PhoenixToast.CreatePhoenixToast(getContext()).showToast(R.string.copied);
    }

    @Override
    public void onDelete(int index, ShortLink link) {
        getPresenter().fireDelete(index, link);
    }
}
