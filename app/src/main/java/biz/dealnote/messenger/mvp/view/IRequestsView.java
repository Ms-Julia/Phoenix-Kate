package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.User;
import biz.dealnote.messenger.model.UsersPart;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IRequestsView extends IMvpView, IErrorView, IAccountDependencyView {
    void notifyDatasetChanged(boolean grouping);

    void setSwipeRefreshEnabled(boolean enabled);

    void displayData(List<UsersPart> data, boolean grouping);

    void notifyItemRangeInserted(int position, int count);

    void showUserWall(int accountId, User user);

    void showRefreshing(boolean refreshing);
}
