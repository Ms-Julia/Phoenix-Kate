package biz.dealnote.messenger.mvp.view;


public interface IPostEditView extends IBasePostEditView, IToastView, IToolbarView {

    void closeAsSuccess();

    void showConfirmExitDialog();


}
