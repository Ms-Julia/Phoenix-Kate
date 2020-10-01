package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Link;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface ILinksInCatalogView extends IMvpView, IErrorView, IAccountDependencyView {
    void displayList(List<Link> links);

    void notifyListChanged();

    void displayRefreshing(boolean refresing);
}
