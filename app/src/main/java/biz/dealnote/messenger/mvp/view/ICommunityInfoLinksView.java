package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.api.model.VKApiCommunity;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface ICommunityInfoLinksView extends IAccountDependencyView, IErrorView, IMvpView {

    void displayRefreshing(boolean loadingNow);

    void notifyDataSetChanged();

    void displayData(List<VKApiCommunity.Link> links);

    void openLink(String link);
}
