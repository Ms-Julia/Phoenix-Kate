package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.database.Country;
import biz.dealnote.mvp.core.IMvpView;


public interface ICountriesView extends IMvpView, IErrorView {
    void displayData(List<Country> countries);

    void notifyDataSetChanged();

    void displayLoading(boolean loading);

    void returnSelection(Country country);
}