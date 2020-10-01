package biz.dealnote.messenger.mvp.view.search;

import biz.dealnote.messenger.model.User;


public interface IPeopleSearchView extends IBaseSearchView<User> {
    void openUserWall(int accountId, User user);
}