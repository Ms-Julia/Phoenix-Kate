package biz.dealnote.messenger.domain;

import java.util.Collection;
import java.util.List;

import biz.dealnote.messenger.api.model.VkApiProfileInfo;
import biz.dealnote.messenger.model.Account;
import biz.dealnote.messenger.model.BannedPart;
import biz.dealnote.messenger.model.User;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Single;

public interface IAccountsInteractor {
    Single<BannedPart> getBanned(int accountId, int count, int offset);

    Completable banUsers(int accountId, Collection<User> users);

    Completable unbanUser(int accountId, int userId);

    Completable changeStatus(int accountId, String status);

    Single<Boolean> setOffline(int accountId);

    Single<VkApiProfileInfo> getProfileInfo(int accountId);

    Single<Integer> saveProfileInfo(int accountId, String first_name, String last_name, String maiden_name, String screen_name, String bdate, String home_town, Integer sex);

    Single<List<Account>> getAll();
}