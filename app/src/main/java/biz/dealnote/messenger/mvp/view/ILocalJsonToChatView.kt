package biz.dealnote.messenger.mvp.view

import biz.dealnote.messenger.model.Message
import biz.dealnote.messenger.model.Peer
import biz.dealnote.messenger.mvp.view.base.IAccountDependencyView
import biz.dealnote.mvp.core.IMvpView

interface ILocalJsonToChatView : IAccountDependencyView, IMvpView, IErrorView, IAttachmentsPlacesView {
    fun displayData(posts: List<Message>)
    fun notifyDataSetChanged()
    fun notifyDataAdded(position: Int, count: Int)
    fun showRefreshing(refreshing: Boolean)
    fun setToolbarTitle(title: String?)
    fun setToolbarSubtitle(subtitle: String?)
    fun scroll_pos(pos: Int)
    fun displayToolbarAvatar(peer: Peer)
    fun attachments_mode(accountId: Int, last_selected: Int)
}