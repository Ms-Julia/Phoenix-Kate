package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.AudioCatalog;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IAudioCatalogView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<AudioCatalog> pages);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);
}
