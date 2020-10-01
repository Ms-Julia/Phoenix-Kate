package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.Article;
import biz.dealnote.messenger.model.Photo;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IFaveArticlesView extends IAccountDependencyView, IMvpView, IErrorView {
    void displayData(List<Article> articles);

    void notifyDataSetChanged();

    void notifyDataAdded(int position, int count);

    void showRefreshing(boolean refreshing);

    void goToArticle(int accountId, String url);

    void goToPhoto(int accountId, Photo photo);
}
