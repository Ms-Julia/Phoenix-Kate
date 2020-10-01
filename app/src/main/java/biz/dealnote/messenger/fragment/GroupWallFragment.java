package biz.dealnote.messenger.fragment;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import biz.dealnote.messenger.Constants;
import biz.dealnote.messenger.Extra;
import biz.dealnote.messenger.R;
import biz.dealnote.messenger.activity.ActivityUtils;
import biz.dealnote.messenger.activity.LoginActivity;
import biz.dealnote.messenger.adapter.horizontal.HorizontalOptionsAdapter;
import biz.dealnote.messenger.fragment.search.SearchContentType;
import biz.dealnote.messenger.fragment.search.criteria.PeopleSearchCriteria;
import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.CommunityDetails;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.ParcelableOwnerWrapper;
import biz.dealnote.messenger.model.PostFilter;
import biz.dealnote.messenger.model.Token;
import biz.dealnote.messenger.mvp.presenter.DocsListPresenter;
import biz.dealnote.messenger.mvp.presenter.GroupWallPresenter;
import biz.dealnote.messenger.mvp.view.IGroupWallView;
import biz.dealnote.messenger.picasso.PicassoInstance;
import biz.dealnote.messenger.picasso.transforms.BlurTransformation;
import biz.dealnote.messenger.place.PlaceFactory;
import biz.dealnote.messenger.settings.CurrentTheme;
import biz.dealnote.messenger.settings.Settings;
import biz.dealnote.messenger.util.AssertUtils;
import biz.dealnote.messenger.util.Utils;
import biz.dealnote.mvp.core.IPresenterFactory;

import static biz.dealnote.messenger.util.Objects.isNull;
import static biz.dealnote.messenger.util.Objects.nonNull;
import static biz.dealnote.messenger.util.Utils.nonEmpty;

public class GroupWallFragment extends AbsWallFragment<IGroupWallView, GroupWallPresenter> implements IGroupWallView {

    private static final int REQUEST_LOGIN_COMMUNITY = 14;
    private GroupHeaderHolder mHeaderHolder;

    @Override
    public void displayBaseCommunityData(Community community, CommunityDetails details) {
        if (isNull(mHeaderHolder)) return;

        mHeaderHolder.tvName.setText(community.getFullName());

        if (details.getCover() != null && !Utils.isEmpty(details.getCover().getImages())) {
            int def = 0;
            String url = null;
            for (CommunityDetails.CoverImage i : details.getCover().getImages()) {
                if (i.getWidth() * i.getHeight() > def) {
                    def = i.getWidth() * i.getHeight();
                    url = i.getUrl();
                }
            }
            displayCommunityCover(url);
        } else {
            displayCommunityCover(community.getMaxSquareAvatar());
        }

        String statusText;
        if (nonNull(details.getStatusAudio())) {
            statusText = details.getStatusAudio().getArtistAndTitle();
        } else {
            statusText = details.getStatus();
        }

        mHeaderHolder.tvStatus.setText(statusText);
        mHeaderHolder.tvAudioStatus.setVisibility(nonNull(details.getStatusAudio()) ? View.VISIBLE : View.GONE);

        String screenName = nonEmpty(community.getScreenName()) ? "@" + community.getScreenName() : null;
        mHeaderHolder.tvScreenName.setText(screenName);

        if (!details.isCanMessage())
            mHeaderHolder.fabMessage.setImageResource(R.drawable.close);
        else
            mHeaderHolder.fabMessage.setImageResource(R.drawable.email);

        String photoUrl = community.getMaxSquareAvatar();
        if (nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl).transform(CurrentTheme.createTransformationForAvatar(requireActivity()))
                    .tag(Constants.PICASSO_TAG)
                    .into(mHeaderHolder.ivAvatar);
        }
        mHeaderHolder.ivAvatar.setOnClickListener(v -> {
            Community cmt = Objects.requireNonNull(getPresenter()).getCommunity();
            PlaceFactory.getSingleURLPhotoPlace(cmt.getOriginalAvatar(), cmt.getFullName(), "club" + Math.abs(cmt.getId())).tryOpenWith(requireActivity());
        });
    }

    private void displayCommunityCover(String resource) {
        if (!Settings.get().other().isShow_wall_cover())
            return;
        if (!Utils.isEmpty(resource)) {
            PicassoInstance.with()
                    .load(resource)
                    .transform(new BlurTransformation(6, 1, requireActivity()))
                    .into(mHeaderHolder.vgCover);
        }
    }

    @NotNull
    @Override
    public IPresenterFactory<GroupWallPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> {
            int accountId = requireArguments().getInt(Extra.ACCOUNT_ID);
            int ownerId = requireArguments().getInt(Extra.OWNER_ID);

            ParcelableOwnerWrapper wrapper = requireArguments().getParcelable(Extra.OWNER);
            AssertUtils.requireNonNull(wrapper);

            return new GroupWallPresenter(accountId, ownerId, (Community) wrapper.get(), saveInstanceState);
        };
    }

    @Override
    protected int headerLayout() {
        return R.layout.header_group;
    }

    @Override
    protected void onHeaderInflated(View headerRootView) {
        mHeaderHolder = new GroupHeaderHolder(headerRootView);
    }

    @Override
    public void setupPrimaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.primaryActionButton.setText(title);
                mHeaderHolder.primaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.primaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void setupSecondaryButton(@StringRes Integer title) {
        if (nonNull(mHeaderHolder)) {
            if (nonNull(title)) {
                mHeaderHolder.secondaryActionButton.setText(title);
                mHeaderHolder.secondaryActionButton.setVisibility(View.VISIBLE);
            } else {
                mHeaderHolder.secondaryActionButton.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public void openTopics(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getTopicsPlace(accoundId, ownerId)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void openCommunityMembers(int accoundId, int groupId) {
        PeopleSearchCriteria criteria = new PeopleSearchCriteria("")
                .setGroupId(groupId);

        PlaceFactory.getSingleTabSearchPlace(accoundId, SearchContentType.PEOPLE, criteria).tryOpenWith(requireActivity());
    }

    @Override
    public void openDocuments(int accoundId, int ownerId, @Nullable Owner owner) {
        PlaceFactory.getDocumentsPlace(accoundId, ownerId, DocsListPresenter.ACTION_SHOW)
                .withParcelableExtra(Extra.OWNER, owner)
                .tryOpenWith(requireActivity());
    }

    @Override
    public void displayWallFilters(List<PostFilter> filters) {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.setItems(filters);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_community_wall, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_community_control) {
            getPresenter().fireCommunityControlClick();
            return true;
        }

        if (item.getItemId() == R.id.action_community_messages) {
            getPresenter().fireCommunityMessagesClick();
            return true;
        }

        if (item.getItemId() == R.id.action_add_to_bookmarks) {
            getPresenter().fireAddToBookmarksClick();
            return true;
        }

        if (item.getItemId() == R.id.action_show_qr) {
            getPresenter().fireShowQR(requireActivity());
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void notifyWallFiltersChanged() {
        if (nonNull(mHeaderHolder)) {
            mHeaderHolder.mFiltersAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        ActivityUtils.setToolbarTitle(this, R.string.community);
        ActivityUtils.setToolbarSubtitle(this, null);
    }

    @Override
    public void goToCommunityControl(int accountId, Community community, GroupSettings settings) {
        PlaceFactory.getCommunityControlPlace(accountId, community, settings).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowComunityInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void goToShowComunityLinksInfo(int accountId, Community community) {
        PlaceFactory.getShowComunityLinksInfoPlace(accountId, community).tryOpenWith(requireActivity());
    }

    @Override
    public void startLoginCommunityActivity(int groupId) {
        Intent intent = LoginActivity.createIntent(requireActivity(), String.valueOf(Constants.API_ID), "messages,photos,docs,manage", Collections.singletonList(groupId));
        startActivityForResult(intent, REQUEST_LOGIN_COMMUNITY);
    }

    @Override
    public void openCommunityDialogs(int accountId, int groupId, String subtitle) {
        PlaceFactory.getDialogsPlace(accountId, -groupId, subtitle, 0).tryOpenWith(requireActivity());
    }

    @Override
    public void displayCounters(int members, int topics, int docs, int photos, int audio, int video) {
        if (isNull(mHeaderHolder)) return;

        setupCounter(mHeaderHolder.bTopics, topics);
        setupCounter(mHeaderHolder.bMembers, members);
        setupCounter(mHeaderHolder.bDocuments, docs);
        setupCounter(mHeaderHolder.bPhotos, photos);
        setupCounter(mHeaderHolder.bAudios, audio);
        setupCounter(mHeaderHolder.bVideos, video);
    }

    @Override
    public void onPrepareOptionsMenu(@NotNull Menu menu) {
        super.onPrepareOptionsMenu(menu);

        OptionMenuView optionMenuView = new OptionMenuView();
        getPresenter().fireOptionMenuViewCreated(optionMenuView);
        menu.findItem(R.id.action_community_control).setVisible(optionMenuView.controlVisible);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_LOGIN_COMMUNITY && resultCode == Activity.RESULT_OK) {
            ArrayList<Token> tokens = LoginActivity.extractGroupTokens(data);
            getPresenter().fireGroupTokensReceived(tokens);
        }
    }

    private static final class OptionMenuView implements IOptionMenuView {

        boolean controlVisible;

        @Override
        public void setControlVisible(boolean visible) {
            controlVisible = visible;
        }
    }

    private class GroupHeaderHolder {
        ImageView vgCover;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvStatus;
        ImageView tvAudioStatus;
        TextView tvScreenName;

        TextView bTopics;
        TextView bMembers;
        TextView bDocuments;
        TextView bPhotos;
        TextView bAudios;
        TextView bVideos;
        MaterialButton primaryActionButton;
        MaterialButton secondaryActionButton;

        FloatingActionButton fabMessage;
        HorizontalOptionsAdapter<PostFilter> mFiltersAdapter;

        GroupHeaderHolder(@NonNull View root) {
            vgCover = root.findViewById(R.id.cover);
            ivAvatar = root.findViewById(R.id.header_group_avatar);
            tvName = root.findViewById(R.id.header_group_name);
            tvStatus = root.findViewById(R.id.header_group_status);
            tvAudioStatus = root.findViewById(R.id.fragment_group_audio);
            tvScreenName = root.findViewById(R.id.header_group_id);
            bTopics = root.findViewById(R.id.header_group_btopics);
            bMembers = root.findViewById(R.id.header_group_bmembers);
            bDocuments = root.findViewById(R.id.header_group_bdocuments);
            bPhotos = root.findViewById(R.id.header_group_bphotos);
            bAudios = root.findViewById(R.id.header_group_baudios);
            bVideos = root.findViewById(R.id.header_group_bvideos);
            primaryActionButton = root.findViewById(R.id.header_group_primary_button);
            secondaryActionButton = root.findViewById(R.id.header_group_secondary_button);
            fabMessage = root.findViewById(R.id.header_group_fab_message);

            RecyclerView filterList = root.findViewById(R.id.post_filter_recyclerview);
            filterList.setLayoutManager(new LinearLayoutManager(requireActivity(), LinearLayoutManager.HORIZONTAL, false));
            mFiltersAdapter = new HorizontalOptionsAdapter<>(Collections.emptyList());
            mFiltersAdapter.setListener(entry -> getPresenter().fireFilterEntryClick(entry));

            filterList.setAdapter(mFiltersAdapter);

            tvStatus.setOnClickListener(v -> getPresenter().fireHeaderStatusClick());
            fabMessage.setOnClickListener(v -> getPresenter().fireChatClick());
            secondaryActionButton.setOnClickListener(v -> getPresenter().fireSecondaryButtonClick());
            primaryActionButton.setOnClickListener(v -> getPresenter().firePrimaryButtonClick());

            root.findViewById(R.id.header_group_photos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderPhotosClick());
            root.findViewById(R.id.header_group_videos_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderVideosClick());
            root.findViewById(R.id.header_group_members_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderMembersClick());
            root.findViewById(R.id.header_group_topics_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderTopicsClick());
            root.findViewById(R.id.header_group_documents_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderDocsClick());
            root.findViewById(R.id.header_group_audios_container)
                    .setOnClickListener(v -> getPresenter().fireHeaderAudiosClick());
            root.findViewById(R.id.header_group_contacts_container)
                    .setOnClickListener(v -> getPresenter().fireShowComunityInfoClick());
            root.findViewById(R.id.header_group_links_container)
                    .setOnClickListener(v -> getPresenter().fireShowComunityLinksInfoClick());
        }
    }
}