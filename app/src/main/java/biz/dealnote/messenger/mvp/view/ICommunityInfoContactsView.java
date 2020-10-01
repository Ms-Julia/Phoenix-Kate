package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Manager;
import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface ICommunityInfoContactsView extends IAccountDependencyView, IErrorView, IMvpView, IToastView {

    void notifyDataSetChanged();

    void displayRefreshing(boolean loadingNow);

    void displayData(List<Manager> managers);

    void showUserProfile(int accountId, User user);

    void notifyItemRemoved(int index);

    void notifyItemChanged(int index);

    void notifyItemAdded(int index);
}
