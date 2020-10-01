package biz.dealnote.messenger.mvp.view;

import java.util.List;

import biz.dealnote.messenger.model.ProxyConfig;
import biz.dealnote.mvp.core.IMvpView;


public interface IProxyManagerView extends IMvpView, IErrorView {
    void displayData(List<ProxyConfig> configs, ProxyConfig active);

    void notifyItemAdded(int position);

    void notifyItemRemoved(int position);

    void setActiveAndNotifyDataSetChanged(ProxyConfig config);

    void goToAddingScreen();
}