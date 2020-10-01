package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.AppChatUser;
import biz.dealnote.messenger.model.Owner;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IChatUsersDomainView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AppChatUser> users);

    void notifyItemRemoved(int position);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void openUserWall(int accountId, Owner user);

    void displayRefreshing(boolean refreshing);
}
