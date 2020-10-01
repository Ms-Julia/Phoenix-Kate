package biz.dealnote.messenger.mvp.view;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import biz.dealnote.messenger.media.gif.IGifPlayer;
import biz.dealnote.messenger.model.Story;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;

public interface IStoryPagerView extends IMvpView, IErrorView, IToastView, IAccountDependencyView {

    void displayData(int pageCount, int selectedIndex);

    void setAspectRatioAt(int position, int w, int h);

    void setPreparingProgressVisible(int position, boolean preparing);

    void attachDisplayToPlayer(int adapterPosition, IGifPlayer gifPlayer);

    void setToolbarTitle(@StringRes int titleRes, Object... params);

    void setToolbarSubtitle(@NonNull Story story, int account_id);

    void configHolder(int adapterPosition, boolean progress, int aspectRatioW, int aspectRatioH);

    void requestWriteExternalStoragePermission();
}
