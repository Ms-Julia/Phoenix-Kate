package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.ThemeValue;
import biz.dealnote.mvp.core.IMvpView;


public interface IThemeView extends IMvpView {
    void displayData(List<ThemeValue> data);
}
