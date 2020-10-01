package biz.dealnote.messenger.mvp.view;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.util.List;

import biz.dealnote.messenger.model.Community;
import biz.dealnote.messenger.model.CommunityDetails;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.model.PostFilter;


public interface IGroupWallView extends IWallView {

    void displayBaseCommunityData(Community community, CommunityDetails details);

    void setupPrimaryButton(@StringRes Integer title);

    void setupSecondaryButton(@StringRes Integer title);

    void openTopics(int accoundId, int ownerId, @Nullable Owner owner);

    void openCommunityMembers(int accoundId, int groupId);

    void openDocuments(int accoundId, int ownerId, @Nullable Owner owner);

    void displayWallFilters(List<PostFilter> filters);

    void notifyWallFiltersChanged();

    void goToCommunityControl(int accountId, Community community, GroupSettings settings);

    void goToShowComunityInfo(int accountId, Community community);

    void goToShowComunityLinksInfo(int accountId, Community community);

    void startLoginCommunityActivity(int groupId);

    void openCommunityDialogs(int accountId, int groupId, String subtitle);

    void displayCounters(int members, int topics, int docs, int photos, int audio, int video);

    interface IOptionMenuView {
        void setControlVisible(boolean visible);
    }
}