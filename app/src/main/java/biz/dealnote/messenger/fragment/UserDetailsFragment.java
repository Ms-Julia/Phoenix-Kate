package biz.dealnote.messenger.fragment;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.adapter.RecyclerMenuAdapter;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.OnSectionResumeCallback;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UserDetails;
import biz.dealnote.messenger.model.menu.AdvancedItem;
import biz.dealnote.messenger.mvp.presenter.UserDetailsPresenter;
import biz.dealnote.messenger.mvp.view.IUserDetailsView;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.PhoenixToast;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.nonNull;

public class UserDetailsFragment extends BaseMvpFragment<UserDetailsPresenter, IUserDetailsView> implements IUserDetailsView, RecyclerMenuAdapter.ActionListener {

    private RecyclerMenuAdapter menuAdapter;

    public static UserDetailsFragment newInstance(int accountId, @NonNull User user, @NonNull UserDetails details) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putParcelable(Extra.USER, user);
        args.putParcelable("details", details);
        UserDetailsFragment fragment = new UserDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_details, container, false);

        ((AppCompatActivity) requireActivity()).setSupportActionBar(view.findViewById(R.id.toolbar));

        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));

        menuAdapter = new RecyclerMenuAdapter(Collections.emptyList());
        menuAdapter.setActionListener(this);

        recyclerView.setAdapter(menuAdapter);
        return view;
    }

    @Override
    public void displayData(@NonNull List<AdvancedItem> items) {
        menuAdapter.setItems(items);
    }

    @Override
    public void displayToolbarTitle(String title) {
        ActionBar actionBar = ActivityUtils.supportToolbarFor(this);
        if (nonNull(actionBar)) {
            actionBar.setTitle(title);
        }
    }

    @Override
    public void openOwnerProfile(int accountId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getOwnerWallPlace(accountId, ownerId, owner).tryOpenWith(requireActivity());
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requireActivity() instanceof OnSectionResumeCallback) {
            ((OnSectionResumeCallback) requireActivity()).onClearSelection();
        }

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(false)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @NotNull
    @Override
    public IPresenterFactory<UserDetailsPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new UserDetailsPresenter(
                requireArguments().getInt(Extra.ACCOUNT_ID),
                requireArguments().getParcelable(Extra.USER),
                requireArguments().getParcelable("details"),
                saveInstanceState
        );
    }

    @Override
    public void onLongClick(AdvancedItem item) {
        ClipboardManager clipboard = (ClipboardManager) requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            String title = item.getTitle().getText(requireContext());
            String subtitle = nonNull(item.getSubtitle()) ? item.getSubtitle().getText(requireContext()) : null;
            String details = Utils.joinNonEmptyStrings("\n", title, subtitle);

            ClipData clip = ClipData.newPlainText("Details", details);
            clipboard.setPrimaryClip(clip);

            PhoenixToast.CreatePhoenixToast(requireActivity()).showToast(R.string.copied_to_clipboard);
        }
    }

    @Override
    public void onClick(AdvancedItem item) {
        getPresenter().fireItemClick(item);
    }
}