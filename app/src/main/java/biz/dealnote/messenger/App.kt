package biz.dealnote.messenger

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import biz.dealnote.messenger.domain.Repository.messages
import biz.dealnote.messenger.longpoll.NotificationHelper
import biz.dealnote.messenger.model.PeerUpdate
import biz.dealnote.messenger.model.SentMsg
import biz.dealnote.messenger.picasso.PicassoInstance
import biz.dealnote.messenger.player.util.MusicUtils
import biz.dealnote.messenger.service.ErrorLocalizer
import biz.dealnote.messenger.service.KeepLongpollService
import biz.dealnote.messenger.settings.Settings
import biz.dealnote.messenger.util.PhoenixToast.Companion.CreatePhoenixToast
import biz.dealnote.messenger.util.RxUtils
import com.developer.crashx.config.CrashConfig
import ealvatag.tag.TagOptionSingleton
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.CompositeDisposable

class App : Application() {
    private val compositeDisposable = CompositeDisposable()

    override fun onCreate() {
        sInstanse = this
        AppCompatDelegate.setDefaultNightMode(Settings.get().ui().nightMode)
        CrashConfig.Builder.create()
                .apply()
        TagOptionSingleton.getInstance().isAndroid = true
        MusicUtils.registerBroadcast(this)
        super.onCreate()
        PicassoInstance.init(this, Injection.provideProxySettings())
        if (Settings.get().other().isKeepLongpoll) {
            KeepLongpollService.start(this)
        }
        compositeDisposable.add(messages
                .observePeerUpdates()
                .flatMap { source: List<PeerUpdate> -> Flowable.fromIterable(source) }
                .subscribe({ update: PeerUpdate ->
                    if (update.readIn != null) {
                        NotificationHelper.tryCancelNotificationForPeer(this, update.accountId, update.peerId)
                    }
                }, RxUtils.ignore()))
        compositeDisposable.add(messages
                .observeSentMessages()
                .subscribe({ sentMsg: SentMsg -> NotificationHelper.tryCancelNotificationForPeer(this, sentMsg.accountId, sentMsg.peerId) }, RxUtils.ignore()))
        compositeDisposable.add(messages
                .observeMessagesSendErrors()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ throwable: Throwable? -> CreatePhoenixToast(this).showToastError(ErrorLocalizer.localizeThrowable(this, throwable)) }, RxUtils.ignore()))
    }

    companion object {
        private var sInstanse: App? = null

        @JvmStatic
        val instance: App
            get() {
                checkNotNull(sInstanse) { "App instance is null!!! WTF???" }
                return sInstanse!!
            }
    }
}