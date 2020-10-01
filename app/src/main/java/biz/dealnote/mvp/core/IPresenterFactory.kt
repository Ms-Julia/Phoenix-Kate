package biz.dealnote.mvp.core

interface IPresenterFactory<T : IPresenter<*>> {
    fun create(): T
}