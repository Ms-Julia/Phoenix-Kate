package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.model.Commented;
import biz.dealnote.messenger.model.Video;
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.mvp.core.IMvpView;


public interface IVideoPreviewView extends IAccountDependencyView, IMvpView, IErrorView {

    void displayLoading();

    void displayLoadingError();

    void displayVideoInfo(Video video);

    void displayLikes(int count, boolean userLikes);

    void setCommentButtonVisible(boolean visible);

    void displayCommentCount(int count);

    void showSuccessToast();

    void showOwnerWall(int accountId, int ownerId);

    void showSubtitle(String subtitle);

    void showComments(int accountId, Commented commented);

    void displayShareDialog(int accountId, Video video, boolean canPostToMyWall);

    void showVideoPlayMenu(int accountId, Video video);

    interface IOptionView {
        void setCanAdd(boolean can);

        void setIsMy(boolean my);
    }
}