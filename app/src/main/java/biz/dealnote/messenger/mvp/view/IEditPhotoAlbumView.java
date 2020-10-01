package biz.dealnote.messenger.mvp.view;

import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView;
import biz.dealnote.messenger.mvp.view.base.ISteppersView;
import biz.dealnote.messenger.view.steppers.impl.CreatePhotoAlbumStepsHost;

public interface IEditPhotoAlbumView extends IAccountDependencyView, ISteppersView<CreatePhotoAlbumStepsHost>, IErrorView {

}
