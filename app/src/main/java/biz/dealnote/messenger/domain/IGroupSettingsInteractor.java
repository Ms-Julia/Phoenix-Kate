package biz.dealnote.messenger.domain;

import java.util.List;

import biz.dealnote.messenger.fragment.search.nextfrom.IntNextFrom;
import biz.dealnote.messenger.model.Banned;
import biz.dealnote.messenger.model.ContactInfo;
import biz.dealnote.messenger.model.GroupSettings;
import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.util.Pair;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IGroupSettingsInteractor {

    Single<GroupSettings> getGroupSettings(int accountId, int groupId);

    Completable ban(int accountId, int groupId, int ownerId, Long endDateUnixtime, int reason, String comment, boolean commentVisible);

    Completable editManager(int accountId, int groupId, User user, String role, boolean asContact, String position, String email, String phone);

    Completable unban(int accountId, int groupId, int ownerId);

    Single<Pair<List<Banned>, IntNextFrom>> getBanned(int accountId, int groupId, IntNextFrom startFrom, int count);

    Single<List<Manager>> getManagers(int accountId, int groupId);

    Single<List<ContactInfo>> getContacts(int accountId, int groupId);
}