package biz.dealnote.messenger.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityFeatures;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.fragment.base.BaseMvpFragment;
import biz.dealnote.messenger.listener.TextWatcherAdapter;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.presenter.CommunityManagerEditPresenter;
import biz.dealnote.messenger.mvp.view.ICommunityManagerEditView;
import biz.dealnote.messenger.picasso.transforms.RoundTransformation;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.util.Objects;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.messenger.util.ViewUtils;
import biz.dealnote.messenger.view.OnlineView;
import biz.dealnote.mvp.core.IPresenterFactory;


public class CommunityManagerEditFragment extends BaseMvpFragment<CommunityManagerEditPresenter, ICommunityManagerEditView> implements ICommunityManagerEditView {

    private ImageView mAvatar;
    private OnlineView mOnlineView;
    private TextView mName;
    private TextView mDomain;
    private RadioButton mButtonModerator;
    private RadioButton mButtonEditor;
    private RadioButton mButtonAdmin;
    private CheckBox mShowAsContact;
    private View mContactInfoRoot;
    private TextInputEditText mPosition;
    private TextInputEditText mEmail;
    private TextInputEditText mPhone;
    private RadioGroup mRadioGroupRoles;
    private RadioGroup mRadioGroupCreator;
    private boolean mOptionDeleteVisible;

    public static CommunityManagerEditFragment newInstance(int accountId, int groupId, ArrayList<User> users) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        args.putParcelableArrayList(Extra.USERS, users);
        CommunityManagerEditFragment fragment = new CommunityManagerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static CommunityManagerEditFragment newInstance(int accountId, int groupId, Manager manager) {
        Bundle args = new Bundle();
        args.putInt(Extra.ACCOUNT_ID, accountId);
        args.putInt(Extra.GROUP_ID, groupId);
        args.putParcelable(Extra.MANAGER, manager);
        CommunityManagerEditFragment fragment = new CommunityManagerEditFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_community_manager_edit, container, false);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(root.findViewById(R.id.toolbar));

        mAvatar = root.findViewById(R.id.avatar);
        mAvatar.setOnClickListener(v -> getPresenter().fireAvatarClick());

        mOnlineView = root.findViewById(R.id.online);
        mName = root.findViewById(R.id.name);
        mDomain = root.findViewById(R.id.domain);

        mButtonModerator = root.findViewById(R.id.button_moderator);
        mButtonEditor = root.findViewById(R.id.button_editor);
        mButtonAdmin = root.findViewById(R.id.button_admin);

        mRadioGroupRoles = root.findViewById(R.id.radio_group_roles);
        mRadioGroupRoles.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.button_moderator:
                    getPresenter().fireModeratorChecked();
                    break;

                case R.id.button_editor:
                    getPresenter().fireEditorChecked();
                    break;

                case R.id.button_admin:
                    getPresenter().fireAdminChecked();
                    break;
            }
        });

        mRadioGroupCreator = root.findViewById(R.id.radio_group_creator);

        mShowAsContact = root.findViewById(R.id.community_manager_show_in_contacts);
        mShowAsContact.setOnCheckedChangeListener((buttonView, checked) -> getPresenter().fireShowAsContactChecked(checked));

        mContactInfoRoot = root.findViewById(R.id.contact_info_root);

        mPosition = root.findViewById(R.id.community_manager_positon);
        mPosition.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().firePositionEdit(s);
            }
        });

        mEmail = root.findViewById(R.id.community_manager_email);
        mEmail.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().fireEmailEdit(s);
            }
        });

        mPhone = root.findViewById(R.id.community_manager_phone);
        mPhone.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                getPresenter().firePhoneEdit(s);
            }
        });
        return root;
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.community_manager_edit, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_save) {
            getPresenter().fireButtonSaveClick();
            return true;
        }

        if (item.getItemId() == R.id.action_delete) {
            getPresenter().fireDeleteClick();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.action_delete).setVisible(mOptionDeleteVisible);
    }

    @NotNull
    @Override
    public IPresenterFactory<CommunityManagerEditPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int groupId = requireArguments().getInt(Extra.GROUP_ID);
            ArrayList<User> users = requireArguments().getParcelableArrayList(Extra.USERS);
            Manager manager = requireArguments().getParcelable(Extra.MANAGER);

            return Objects.nonNull(manager)
                    ? new CommunityManagerEditPresenter(accountId, groupId, manager, saveInstanceState)
                    : new CommunityManagerEditPresenter(accountId, groupId, users, saveInstanceState);
        };
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.edit_manager_title);
        ActivityUtils.setToolbarSubtitle(this, R.string.editing);

        new ActivityFeatures.Builder()
                .begin()
                .setHideNavigationMenu(true)
                .setBarsColored(requireActivity(), true)
                .build()
                .apply(requireActivity());
    }

    @Override
    public void displayUserInfo(User user) {
        if (Objects.nonNull(mAvatar)) {
            ViewUtils.displayAvatar(mAvatar, new RoundTransformation(), user.getMaxSquareAvatar(), null);
        }

        safelySetText(mName, user.getFullName());

        Integer iconRes = ViewUtils.getOnlineIcon(user.isOnline(), user.isOnlineMobile(), user.getPlatform(), user.getOnlineApp());
        if (Objects.nonNull(mOnlineView)) {
            mOnlineView.setVisibility(Objects.nonNull(iconRes) ? View.VISIBLE : View.INVISIBLE);

            if (Objects.nonNull(iconRes)) {
                mOnlineView.setIcon(iconRes);
            }
        }

        if (Utils.nonEmpty(user.getDomain())) {
            safelySetText(mDomain, "@" + user.getDomain());
        } else {
            safelySetText(mDomain, "@id" + user.getId());
        }
    }

    @Override
    public void showUserProfile(int accountId, User user) {
        PlaceFactory.getOwnerWallPlace(accountId, user).tryOpenWith(requireActivity());
    }

    @Override
    public void checkModerator() {
        safelySetCheched(mButtonModerator, true);
    }

    @Override
    public void checkEditor() {
        safelySetCheched(mButtonEditor, true);
    }

    @Override
    public void checkAdmin() {
        safelySetCheched(mButtonAdmin, true);
    }

    @Override
    public void setShowAsContactCheched(boolean cheched) {
        safelySetCheched(mShowAsContact, cheched);
    }

    @Override
    public void setContactInfoVisible(boolean visible) {
        safelySetVisibleOrGone(mContactInfoRoot, visible);
    }

    @Override
    public void displayPosition(String position) {
        safelySetText(mPosition, position);
    }

    @Override
    public void displayEmail(String email) {
        safelySetText(mEmail, email);
    }

    @Override
    public void displayPhone(String phone) {
        safelySetText(mPhone, phone);
    }

    @Override
    public void configRadioButtons(boolean isCreator) {
        safelySetVisibleOrGone(mRadioGroupRoles, !isCreator);
        safelySetVisibleOrGone(mRadioGroupCreator, isCreator);
    }

    @Override
    public void goBack() {
        requireActivity().onBackPressed();
    }

    @Override
    public void setDeleteOptionVisible(boolean visible) {
        mOptionDeleteVisible = visible;
        requireActivity().invalidateOptionsMenu();
    }
}