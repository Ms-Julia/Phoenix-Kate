package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.ShortLink;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IShortedLinksView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<ShortLink> links);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void updateLink(String url);

    void showLinkStatus(String status);
}
